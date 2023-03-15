package model

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class DBModel(db: Database)(implicit ec: ExecutionContext){
  def validateUser(username: String, password: String) = ???
}
