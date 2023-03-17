package model

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import org.mindrot.jbcrypt.BCrypt
import Tables._
import model.DBModel.{LoginData, Task, TaskForm}
import play.api.libs.json.Json

class DBModel(db: Database)(implicit ec: ExecutionContext){
  def validateUser(username: String, password: String): Future[Option[Int]] = {
    val res = db.run(Users.filter(_.username === username).result)
    res.map(_.headOption.flatMap { row =>
      if(BCrypt.checkpw(password, row.password)) Some(row.id)
      else None
    })
  }

  //returns userId in the option
  //will be used for session instead of username
  def createUser(username: String, password: String): Future[Option[Int]] = {
    val res = db.run(Users.filter(_.username === username).result)
    res.flatMap { rows =>
      if(rows.isEmpty) {
        db.run(Users += UsersRow(-1, username, BCrypt.hashpw(password, BCrypt.gensalt())))
          .flatMap { count =>
            if(count > 0) {
              db.run(Users.filter(_.username === username).result).map { rows =>
                rows.headOption.map(_.id)
              }
            }
            else Future.successful(Option.empty[Int])
          }
      } else {
        Future.successful(Option.empty[Int])
      }
    }
  }

  def getTasks(username: String): Future[Seq[Task]] = {
    db.run((for {
      user <- Users if user.username === username
      item <- Items if item.userId === user.id
    } yield item).result).map { tasks => tasks.map { task => Task(task.itemId, task.text.getOrElse(""), task.marked)} }
  }

  def addTask(id: Int, task: TaskForm): Future[Boolean] = {
    db.run( Items += ItemsRow(-1, id, Some(task.text), task.marked)).map(_ > 0)
  }

  def markTask(userId: Int, taskId: Int): Future[Boolean] = {
    val query = for {
      item <- Items if item.userId === userId && item.itemId === taskId
    } yield item.marked

    db.run(query.result).flatMap { result =>
      result.headOption match {
        case Some(marked) => db.run(query.update(!marked)).map(_ > 0)
        case None => Future.successful(false)
      }
    }
  }

  def removeTask(userId: Int, taskId: Int): Future[Boolean] = {
    db.run((for{
      item <- Items if item.userId === userId && item.itemId === taskId
    } yield item).delete).map(_ > 0)
  }
}

object DBModel {
  def apply(db: Database)(implicit ec: ExecutionContext): DBModel = new DBModel(db)

  case class LoginData(username: String, password: String)
  implicit val loginDataReads = Json.reads[LoginData]

  case class Task(taskId: Int, text: String, marked: Boolean)
  implicit val taskWrites = Json.writes[Task]
  implicit val taskOrdering: Ordering[Task] = Ordering.by(_.taskId)

  case class TaskForm(text: String, marked: Boolean)
  implicit val taskDataReads= Json.reads[TaskForm]
}