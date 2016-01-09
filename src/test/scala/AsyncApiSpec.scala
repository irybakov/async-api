
import akka.testkit.{TestActorRef, TestProbe}
import kz.rio._
import kz.rio.core.PingActor
import kz.rio.routing._
import org.scalatest.{Matchers, FlatSpec}
import spray.http._
import spray.routing._
import spray.testkit.ScalatestRouteTest


/**
 * Created by irybakov on 1/9/16.
 */
class AsyncApiSpec extends FlatSpec with ScalatestRouteTest with Matchers {

  val pingService = TestProbe()

  def restRouting = TestActorRef(new RestRouting() {

    override def handlePing(message : RestMessage): Route =
      ctx => perRequest(ctx, pingService.ref, message)
  })

  "Post to RestRouting" should " return pong json" in {
    val postPing = Post("/ping",
      HttpEntity(
          MediaTypes.`application/json`,
          """{"message":"test"}"""
      )
    ) ~> restRouting.underlyingActor.route

    pingService.expectMsg(PingActor.Ping("test"))
    pingService.reply(PingActor.Pong("test"))

    postPing ~> check {
      responseAs[String] should equal("""{"pong":"test"}""")
    }
  }
}
