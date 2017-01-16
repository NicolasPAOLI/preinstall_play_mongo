package controllers

/**
  * Created by PNPI09801 on 13/01/2017.
  */
import javax.inject._

import models.{Datacenter, Server, Stockage}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}


/**
  * Simple controller that directly stores and retrieves [models.Server] instances into a MongoDB Collection
  * Input is first converted into a server and then the server is converted to JsObject to be stored in MongoDB
  */
@Singleton
class ServerController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  def serversFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("server"))

  def create(calife: String, datacenter: Datacenter, stockage: Stockage) = Action.async {
    for {
      servers <- serversFuture
      lastError <- servers.insert(Server(calife, datacenter, stockage))
    } yield
      Ok("Mongo LastError: %s".format(lastError))
  }

  def createFromJson = Action.async(parse.json) { request =>
    Json.fromJson[Server](request.body) match {
      case JsSuccess(server, _) =>
        for {
          servers <- serversFuture
          lastError <- servers.insert(server)
        } yield {
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created("Created 1 server")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a server from the json provided. " + Errors.show(errors)))
    }
  }

  def createBulkFromJson = Action.async(parse.json) { request =>
    Json.fromJson[Seq[Server]](request.body) match {
      case JsSuccess(newServers, _) =>
        serversFuture.flatMap { servers =>
          val documents = newServers.map(implicitly[servers.ImplicitlyDocumentProducer](_))

          servers.bulkInsert(ordered = true)(documents: _*).map { multiResult =>
            Logger.debug(s"Successfully inserted with multiResult: $multiResult")
            Created(s"Created ${multiResult.n} servers")
          }
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a server from the json provided. " + Errors.show(errors)))
    }
  }

  def findByName(calife: String) = Action.async {
    // let's do our query
    val futureServersList: Future[List[Server]] = serversFuture.flatMap {
      // find all servers with calife `calife`
      _.find(Json.obj("calife" -> calife)).
        // perform the query and get a cursor of JsObject
        cursor[Server](ReadPreference.primary).
        // Coollect the results as a list
        collect[List]()
    }

    // everything's ok! Let's reply with a JsValue
    futureServersList.map { servers =>
      Ok(Json.toJson(servers))
    }
  }

  def all() = Action.async {
    // let's do our query
    val futureServersList: Future[List[Server]] = serversFuture.flatMap {
      // find all servers
      _.find(Json.obj()).
        // perform the query and get a cursor of JsObject
        cursor[Server](ReadPreference.primary).
        // Coollect the results as a list
        collect[List]()
    }

    // everything's ok! Let's reply with a JsValue
    futureServersList.map { servers =>
      Ok(Json.toJson(servers))
    }
  }
}



