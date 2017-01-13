package models

import play.api.libs.json.Json

case class Server(calife: String)

object Server {
  implicit val formatter = Json.format[Server]
}
