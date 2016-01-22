
import akka.testkit.{TestActorRef, TestProbe}
import kz.rio._
import kz.rio.routing._
import org.scalatest.{Matchers, FlatSpec}
import spray.http._
import spray.routing._
import spray.testkit.ScalatestRouteTest


/**
 * Created by irybakov on 1/9/16.
 */
class PingPongRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers {

  val pingService = TestProbe()

  def restRouting = TestActorRef(new RestRouting() {

    override def handleRequest(message: DomainMessage): Route = {
      ctx => perRequest(ctx, pingService.ref, message)
    }

    override def handleLocalRequest(message : DomainMessage): Route =
      ctx => perRequest(ctx, pingService.ref, message)
  })

  "Post to container RestRouting" should " return pong json" in {
    val postPing = Post("/container/ping",
      HttpEntity(
          MediaTypes.`application/json`,
          """{"ping":"container"}"""
      )
    ) ~> restRouting.underlyingActor.route

    pingService.expectMsg(Ping("container"))
    pingService.reply(Pong("container"))

    postPing ~> check {
      responseAs[String] should equal("""{"pong":"container"}""")
    }
  }

  "Post to api RestRouting" should " return pong json" in {
    val postPing = Post("/api/ping",
      HttpEntity(
        MediaTypes.`application/json`,
        """{"ping":"api"}"""
      )
    ) ~> restRouting.underlyingActor.route

    pingService.expectMsg(Ping("api"))
    pingService.reply(Pong("api"))

    postPing ~> check {
      responseAs[String] should equal("""{"pong":"api"}""")
    }
  }
}
