package kz.rio.routing

import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import kz.rio.{DomainMessage, Error, Validation}
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import spray.httpx.Json4sSupport
import scala.concurrent.duration._
import org.json4s.DefaultFormats
import spray.http.StatusCode

import PerRequest._

trait PerRequest extends Actor with Json4sSupport {

  import context._

  val json4sFormats = DefaultFormats

  def r: RequestContext
  def target: ActorRef
  def message: DomainMessage

  setReceiveTimeout(2.seconds)
  target ! message

  def receive = {
    case res: DomainMessage => complete(OK, res)
    case v: Validation    => complete(BadRequest, v)
    case ReceiveTimeout   => complete(GatewayTimeout, Error("Request timeout"))
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T) = {
    r.complete(status, obj)
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, Error(e.getMessage))
        Stop
      }
    }
}

object PerRequest {

  case class WithActorRef(r: RequestContext, target: ActorRef, message: DomainMessage) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, message: DomainMessage, actorName: Option[String] = None) extends PerRequest {
    lazy val target = actorName match {
      case Some(name) => context.actorOf(props, name)
      case _ => context.actorOf(props)
    }
  }
}

trait PerRequestCreator {
  this: Actor =>

  def perRequest(r: RequestContext, target: ActorRef, message: DomainMessage) =
    context.actorOf(Props(new WithActorRef(r, target, message)))

  def perRequest(r: RequestContext, props: Props, message: DomainMessage, actorName: Option[String] = None) = actorName match {
    case Some(name) => context.actorOf(Props(new WithProps(r, props, message, actorName)), s"parent-$name")
    case _ => context.actorOf(Props(new WithProps(r, props, message, None)))
  }

}