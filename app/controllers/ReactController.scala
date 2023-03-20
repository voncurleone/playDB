package controllers

import com.google.inject.{Inject, Singleton}
import model.DBModel
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request, Result}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReactController @Inject()(protected val dbcp: DatabaseConfigProvider, val cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc){

  private val db = dbcp.get[JdbcProfile].db
  private val model = DBModel(db)

  private def withIdSession(f: Int => Future[Result])(implicit request: Request[AnyContent]) = {
    request.session.get("userid").map(id => f(id.toInt)).getOrElse(Future.successful(Ok(Json.toJson(false))))
  }

  private def withJason[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
    request.body.asJson.map { body =>
      Json.fromJson[A](body) match {
        case JsSuccess(value, path) => f(value)
        case JsError(errors) => Future.successful(Ok(Json.toJson(false)))
      }
    }.getOrElse(Future.successful(Ok(Json.toJson(false))))
  }
}
