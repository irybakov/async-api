package kz.rio.core

import akka.actor._
import akka.actor.SupervisorStrategy.Escalate
import kz.rio._
import kz.rio.endpoint.RequestPublisherActor


/**
 * Created by irybakov on 1/8/16.
 */
object PingEchoActor {
  def props(uuid: String): Props =  Props(classOf[PingEchoActor],uuid)
}

class PingEchoActor(uuid: String) extends AsyncServiceActor {

  def receive = {
    case p @ Ping(ping) => {
      log.debug("PingEchoActor got Ping: {}",p)
      publishAndWaitForReplay(p)
      context.become(waitingResponses)
    }
    case e @ Echo(echo) => {
      log.debug("PingEchoActor got Echo: {}",e)
      publishAndWaitForReplay(e)
      context.become(waitingResponses)
    }

    case _ =>  throw BagRequestException

  }

  /**
   * Publish request and change behavior for "waitingResponse"
   * @param domainMessage
   */
  def publishAndWaitForReplay(domainMessage: DomainMessage) = {
    amqpPublisher ! RequestPublisherActor.PublishToQueue(uuid,domainMessage)
  }

  def waitingResponses: Receive = {

    case p @ Pong(pong) => {
      context.parent ! p
    }
    case e @ Echo(echo) => {
      context.parent ! e
    }
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }

}
