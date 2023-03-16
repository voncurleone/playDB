package controllers

import model.DBModel
import model.DBModel._

import javax.inject._
import play.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import play.api.mvc._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(protected val dbcp: DatabaseConfigProvider, val cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  private val db = dbcp.get[JdbcProfile].db
  private val model: DBModel = DBModel(db)

  private def withJson[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
    request.body.asJson.map { body =>
      Json.fromJson[A](body) match {
        case JsSuccess(value, path) => f(value)
        case JsError(errors) => Future.successful(Ok(Json.toJson(false)))
      }
    }.getOrElse(Future.successful(Ok(Json.toJson(false))))
  }

  private def withUserSession(f: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    request.session.get("username").map(f).getOrElse(Future.successful(Redirect(routes.HomeController.index)))
  }

  private def withUserIdSession(f: Int => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    request.session.get("userid").map(id => f(id.toInt)).getOrElse(Future.successful(Redirect(routes.HomeController.index)))
  }

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.page())
  }

  def validateLogin = Action.async { implicit request =>
    withJson[LoginData] { data =>
      model.validateUser(data.username, data.password).map {
        case Some(userid) => Ok(Json.toJson(true))
          .withSession("username" -> data.username, "userid" -> userid.toString, "csrfToken" -> play.filters.csrf.CSRF.getToken.get.value)
        case None => Ok(Json.toJson(false))
      }
    }
  }

  def createUser = Action.async { implicit request =>
    withJson[LoginData] { data =>
      model.createUser(data.username, data.password).map {
        case Some(userid) => Ok(Json.toJson(true))
          .withSession("username" -> data.username, "userid" -> userid.toString, "csrfToken" -> play.filters.csrf.CSRF.getToken.get.value)
        case None => Ok(Json.toJson(false))
      }
    }
  }

  def getTasks = Action.async { implicit request =>
    withUserSession { username =>
      model.getTasks(username).map { tasks =>
        Ok(Json.toJson(tasks))
      }
    }
  }

  def logout = Action { implicit request =>
    Ok(Json.toJson(true)).withSession(request.session - "username" - "id")
  }

  def addTask = Action.async { implicit request =>
    withUserIdSession { id =>
      withJson[TaskForm] { task =>
        model.addTask(id, Task(task.task, task.marked)).map(res => Ok(Json.toJson(res)))
        //Future.successful(Ok(Json.toJson(false)))
      }
    }
  }
}
