package kz.rio

import akka.io.IO
import kz.rio.routing.RestRouting
import spray.can.Http

import akka.actor.{Props, ActorSystem}

object Boot extends App {
  implicit val system = ActorSystem("async-api")

  val serviceActor = system.actorOf(Props(new RestRouting), name = "rest-routing")

  system.registerOnTermination {
    system.log.info("Actor per request demo shutdown.")
  }

  IO(Http) ! Http.Bind(serviceActor, "0.0.0.0", port = 8082)
}