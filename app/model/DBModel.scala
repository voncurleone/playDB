package model

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import org.mindrot.jbcrypt.BCrypt
import Tables._
import model.DBModel.LoginData
import play.api.libs.json.Json

class DBModel(db: Database)(implicit ec: ExecutionContext){
  def validateUser(username: String, password: String): Future[Option[Int]] = ???

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
}

object DBModel {
  def apply(db: Database)(implicit ec: ExecutionContext): DBModel = new DBModel(db)

  case class LoginData(username: String, password: String)
  implicit val loginDataReads = Json.reads[LoginData]
}