package kz.rio.routing

import akka.actor.{Props, Actor}
import kz.rio.core.EchoActor.Echo
import kz.rio.core.{EchoActor, PingActor}
import kz.rio.core.PingActor.Ping
import kz.rio._
import org.json4s.NoTypeHints
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import spray.routing.{Route, HttpService}


class RestRouting extends HttpService with Actor with PerRequestCreator{

  implicit def actorRefFactory = context
  implicit val formats = Serialization.formats(NoTypeHints)

  def receive = runRoute(route)

  val route = {
    post {
      path("ping") {
        entity(as[String]) { body =>
          handlePing {
            val ping = parse(body).extract[Ping]
            ping
          }
        }
      } ~
      path("echo") {
        entity(as[String]) { body =>
          handleEcho {
            val echo = parse(body).extract[Echo]
            echo
          }
        }
      }
    }

  }

  def handlePing(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(new PingActor()), message)

  def handleEcho(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(new EchoActor()), message)

}
