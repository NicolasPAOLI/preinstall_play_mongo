package models

import play.api.libs.json.Json

case class Datacenter (name: String)

case class Stockage (name: String, vol_os: Int, vol_stockage: Int)

case class Server(calife: String, datacenter: Datacenter, stockage: Stockage)

object Server {
  implicit val formatterDatacenter = Json.format[Datacenter]
  implicit val formatterStockage = Json.format[Stockage]
  implicit val formatter = Json.format[Server]
}
