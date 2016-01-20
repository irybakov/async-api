package kz.rio

import akka.io.IO
import com.github.sstone.amqp.{ConnectionOwner, Amqp}
import com.rabbitmq.client.ConnectionFactory
import kz.rio.endpoint.{ListenerActor, RequestPublisherActor}

import kz.rio.routing.RestRouting
import spray.can.Http
import scala.concurrent.duration._

import akka.actor.{Props, ActorSystem}

object Boot extends App {

  implicit val system = ActorSystem("async-api")

  val amqpConnection = getAmqpConnection()

  // Create amqp publisher Actor. We will access it via selector
  system.actorOf(RequestPublisherActor.props(amqpConnection),name = "amqpPublisher")

  // This Actor will listen for response
  system.actorOf(ListenerActor.props(amqpConnection),name = "amqpListener")

  system.registerOnTermination {
    system.log.info("Actor per request demo shutdown.")
  }

  val routingActor = system.actorOf(RestRouting.props(), name = "rest-routing")

  IO(Http) ! Http.Bind(routingActor, "0.0.0.0", port = 8082)


  def getAmqpConnection() = {
    // create an AMQP connection
    val connFactory = new ConnectionFactory()
    connFactory.setHost("192.168.33.10")
    connFactory.setPort(5672)
    connFactory.setUsername("admin")
    connFactory.setPassword("admin")

    system.actorOf(ConnectionOwner.props(connFactory, 1 second))
  }


}