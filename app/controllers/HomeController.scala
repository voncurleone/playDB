package controllers

import model.DBModel
import model.DBModel._

import javax.inject._
import play.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
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
        case JsError(errors) => Future.successful(Redirect(routes.HomeController.index))
      }
    }.getOrElse(Future.successful(Redirect(routes.HomeController.index)))
  }

  private def withUserSession(f: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    request.session.get("username").map(f).getOrElse(Future.successful(Redirect(routes.HomeController.index)))
  }

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.page())
  }

  def createUser = Action.async { implicit request =>
    withJson[LoginData] { data =>
      model.createUser(data.username, data.password).map {
        case Some(userid) => Ok(Json.toJson(true))
          .withSession("username" -> data.username, "userid" -> userid.toString, "CsrfToken" -> play.filters.csrf.CSRF.getToken.get.value)
        case None => Ok(Json.toJson(false))
      }
      //Future.successful(Ok(Json.toJson(false)))
    }
  }
}
