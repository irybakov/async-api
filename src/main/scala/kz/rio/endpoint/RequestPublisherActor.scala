package kz.rio.endpoint

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorRef, Actor, ActorLogging}
import com.github.sstone.amqp.Amqp.Publish
import com.github.sstone.amqp.{Amqp, ChannelOwner, ConnectionOwner}
import com.rabbitmq.client.AMQP.BasicProperties
import kz.rio.endpoint.RequestPublisherActor.PublishToQueue
import kz.rio.{DomainMessage, Echo, Ping}
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._


/**
 * Created by irybakov on 1/19/16.
 */
object RequestPublisherActor {

  case class PublishToQueue(correlationId: String, dm: DomainMessage)

  def props(amqpConnection: ActorRef): Props =  Props(classOf[RequestPublisherActor],amqpConnection)


}

class RequestPublisherActor(amqpConnection: ActorRef) extends Actor with ActorLogging {

  import context._
  implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Ping],classOf[Echo])))

  val producer = ConnectionOwner.createChildActor(amqpConnection, ChannelOwner.props())
  Amqp.waitForConnection(system, amqpConnection, producer).await(5, TimeUnit.SECONDS)

  override def receive: Receive = {
    case p @ PublishToQueue(correlationId,dm)  => publish(write[DomainMessage](p.dm),p.correlationId,"")
  }

  def publish (body: String, correlationId: String, replayTo: String)= {

    val props = new BasicProperties(null,null,null,1,null,correlationId,null,null,null,null,null,null,null,null)
    producer ! Publish("", "request.queue", body.getBytes(), properties = Some(props), mandatory = true, immediate = false)
    log.info("Publeshed {}", body)
  }
}
