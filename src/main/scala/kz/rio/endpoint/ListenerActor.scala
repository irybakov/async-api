package kz.rio.endpoint

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.{Amqp, ConnectionOwner, Consumer}
import kz.rio.{DomainMessage, Pong, Echo}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

/**
 * Created by irybakov on 1/12/16.
 */
object ListenerActor {
  def props(amqpConnection: ActorRef): Props =  Props(classOf[ListenerActor],amqpConnection)
}

class ListenerActor(amqpConnection: ActorRef) extends Actor with ActorLogging {

  import context._

  implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Pong],classOf[Echo])))

  // create a consumer that will route incoming AMQP messages to our listener
  val queueParams = QueueParameters("replay.queue", passive = false, durable = false, exclusive = false, autodelete = false)
  val consumer = ConnectionOwner.createChildActor(amqpConnection, Consumer.props(Some(self)))

  // wait till everyone is actually connected to the broker

  Amqp.waitForConnection(system, consumer).await()

  consumer ! DeclareQueue(queueParams)

  consumer ! QueueBind(queue = "replay.queue", exchange = "amq.topic", routing_key = "*.replay")

  // tell our consumer to consume from it
  consumer ! AddQueue(queueParams)



  def parseCommand(msgBody: String): DomainMessage = {
    parse(msgBody).extract[DomainMessage]
  }

  override def receive = {
    case d @ Delivery(consumerTag, envelope, properties, body) => {
      val msgBody = new String(body)
      log.debug("got message: {}",msgBody)


      val actorName = d.properties.getCorrelationId

      if(actorName != null) {
        val pingEchoActor = context.actorSelection(s"/user/rest-routing/parent-$actorName/$actorName")
        val command = parseCommand(msgBody)
        pingEchoActor ! command
      } else {
        log.error("Actor with provided ID was not found")
      }

      sender ! Ack(envelope.getDeliveryTag)
    }
  }



}
