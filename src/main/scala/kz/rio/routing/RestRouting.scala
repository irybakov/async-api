package kz.rio.routing

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props, Actor}
import akka.util.Timeout
import kz.rio.core.{PingEchoActor}
import kz.rio._
import org.json4s.NoTypeHints
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import spray.routing.{RequestContext, Route, HttpService}


object RestRouting {

  def props(): Props =  Props(classOf[RestRouting])

}

class RestRouting() extends HttpService with Actor with PerRequestCreator {

  implicit def actorRefFactory = context
  implicit val formats = Serialization.formats(NoTypeHints)

  implicit val timeout = new Timeout(30, TimeUnit.SECONDS)

  var lastId = 0L

  def receive = runRoute(route)

  val route = {
    post {
      path("ping") {
        entity(as[String]) { body =>
          handleRequest {
            val ping = parse(body).extract[Ping]
            ping
          }
        }
      } ~
      path("echo") {
        entity(as[String]) { body =>
          handleRequest {
            val echo = parse(body).extract[Echo]
            echo
          }
        }
      }
    }
  }

  def handleRequest(message : DomainMessage): Route = {
    val uuid = getLastId.toString
    ctx => perRequest(ctx, Props(new PingEchoActor(uuid)), message, Some(uuid))
  }

  def getLastId : Long = {
    if(lastId >= Long.MaxValue) lastId = 0L
    lastId = lastId + 1
    lastId
  }
}
