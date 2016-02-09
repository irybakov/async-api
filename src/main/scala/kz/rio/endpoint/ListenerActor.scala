package kz.rio.endpoint

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.{Amqp, ConnectionOwner, Consumer}
import kz.rio.{Endpoint, DomainMessage, Pong, Echo}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

/**
 * Created by irybakov on 1/12/16.
 */
object ListenerActor {
  def props(amqpConnection: ActorRef,endpoint: Endpoint, inboundGate: String): Props =  Props(classOf[ListenerActor],amqpConnection,endpoint,inboundGate)
}

class ListenerActor(amqpConnection: ActorRef, endpoint: Endpoint, inboundGate: String) extends Actor with ActorLogging {

  import context._

  implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Pong],classOf[Echo])))

  // create a consumer that will route incoming AMQP messages to our listener
  val  args = Map("x-message-ttl" -> Int.box(500))

  val queueParams = QueueParameters(endpoint.queue, passive = false, durable = false, exclusive = false, autodelete = true, args)
  val consumer = ConnectionOwner.createChildActor(amqpConnection, Consumer.props(Some(self)))

  // wait till everyone is actually connected to the broker

  Amqp.waitForConnection(system, consumer).await()

  consumer ! DeclareQueue(queueParams)

  val replyTo = endpoint.instanceEndpoint
  consumer ! QueueBind(queue = endpoint.queue, exchange = inboundGate, routing_key = s"$replyTo.response.stub.*")
  //consumer ! QueueBind(queue = endpoint.queue, exchange = inboundGate, routing_key = s"response.stub.echo@$replyTo")

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
