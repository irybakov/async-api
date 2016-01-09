package kz.rio.core

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{OneForOneStrategy, ActorLogging, Actor}
import kz.rio.RestMessage
import kz.rio.core.EchoActor._

/**
 * Created by irybakov on 1/8/16.
 */
object EchoActor {
  //Echo
  case class Echo(message: String) extends  RestMessage
  case class EchoResponse(echo: String) extends  RestMessage

}

class EchoActor extends Actor with ActorLogging {

  def receive = {
    case Echo(message) => {
      log.info("echo request")
      //slotsService ! SlotsRepoActor.GetSlots()
      //context.become(waitingResponses)
      context.parent ! EchoResponse(message)
    }
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}
