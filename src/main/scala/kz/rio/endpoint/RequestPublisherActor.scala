package kz.rio.endpoint

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorRef, Actor, ActorLogging}
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.{Amqp, ChannelOwner, ConnectionOwner}
import com.rabbitmq.client.AMQP.BasicProperties
import kz.rio.endpoint.RequestPublisherActor.PublishToQueue
import kz.rio.{Endpoint, DomainMessage, Echo, Ping}
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._


/**
 * Created by irybakov on 1/19/16.
 */
object RequestPublisherActor {

  case class PublishToQueue(correlationId: String, dm: DomainMessage)

  def props(amqpConnection: ActorRef,endpoint: Endpoint, outboundGate: String): Props =  Props(classOf[RequestPublisherActor],amqpConnection,endpoint,outboundGate)

}

class RequestPublisherActor(amqpConnection: ActorRef,endpoint: Endpoint, outboundGate: String) extends Actor with ActorLogging {

  import context._
  implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Ping],classOf[Echo])))

  val routeKeyTemplate = "request.stub.%"

  val producer = ConnectionOwner.createChildActor(amqpConnection, ChannelOwner.props())

  Amqp.waitForConnection(system, amqpConnection, producer).await(5, TimeUnit.SECONDS)

  //producer ! DeclareExchange(ExchangeParameters(name = endpoint.exchange, passive = false, exchangeType = "fanout", durable = false, autodelete = false))

  val instanceEnd =endpoint.instanceEndpoint
  val replyToTemplate = s"$instanceEnd.response.stub.%"

  override def receive: Receive = {
    case p @ PublishToQueue(correlationId,dm)  => publish(write[DomainMessage](p.dm),p.correlationId,replyTo(dm),outboundGate,routeKey(dm))
  }

  def publish (body: String, correlationId: String, replyTo: String, exchange: String, routeKey: String)= {

    val props = new BasicProperties(null,null,null,1,null,correlationId,replyTo,null,null,null,null,null,null,null)
    producer ! Publish(exchange, routeKey, body.getBytes(), properties = Some(props), mandatory = true, immediate = false)
    log.info("Publeshed {}", body)
  }

  def routeKey(dm: DomainMessage): String = dm match {
    case Ping(_) => routeKeyTemplate.replaceAll("%","ping")
    case Echo(_) => routeKeyTemplate.replaceAll("%","echo")
    case _ => "undefined"
  }

  def replyTo(dm: DomainMessage): String = dm match {
    case Ping(_) => replyToTemplate.replaceAll("%","ping")
    case Echo(_) => replyToTemplate.replaceAll("%","echo")
    case _ => "undefined"
  }

}
