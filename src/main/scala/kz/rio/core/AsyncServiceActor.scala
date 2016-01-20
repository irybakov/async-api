package kz.rio.core

import akka.actor.{ActorLogging, Actor}

/**
 * Created by irybakov on 1/19/16.
 */
trait AsyncServiceActor extends Actor with ActorLogging {

  val amqpPublisher = context.actorSelection(s"/user/amqpPublisher")


}
