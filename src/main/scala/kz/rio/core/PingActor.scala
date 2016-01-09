package kz.rio.core

import akka.actor.{ActorLogging, Actor, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate
import kz.rio.core.PingActor._
import kz.rio.{Validation, RestMessage}

/**
 * Created by irybakov on 1/8/16.
 */
object PingActor{
  // Ping-Pong
  case class Ping(message: String) extends RestMessage
  case class Pong(pong: String) extends RestMessage

}

class PingActor extends Actor with ActorLogging {

  def receive = {
    case Ping(message) => {
      log.info("ping request")
      //slotsService ! SlotsRepoActor.GetSlots()
      //context.become(waitingResponses)
      context.parent ! Pong("PONG: "+ message)
    }
  }

  /*
  def waitingResponses: Receive = {

    case SlotsRepoActor.Slots(slotSeq) => {
      log.info("request handler got rsp ")
      slots = Some(slotSeq)
      replyIfReady
    }
    case SlotsRepoActor.SingleSlot(slot) => {
      println(slot)
      context.parent ! SingleSlot(slot)
    }

    case f: Validation => context.parent ! f
  }

  def replyIfReady =
    if (slots.nonEmpty) {
      val res = slots.head
      context.parent ! Slots(res)
    }
  */
  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }

}
