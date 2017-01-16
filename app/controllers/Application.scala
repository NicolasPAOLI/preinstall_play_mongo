package controllers

/**
  * Created by nicolas on 16/01/17.
  */

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models._

class Application extends Controller {

  implicit val locationWrites: Writes[Location] = (
    (JsPath \ "lat").write[Double] and
      (JsPath \ "long").write[Double]
    )(unlift(Location.unapply))

  implicit val placeWrites: Writes[Place] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "location").write[Location]
    )(unlift(Place.unapply))


  def listPlaces = Action {
    val json = Json.toJson(Place.list)
    Ok(json)
  }

}
