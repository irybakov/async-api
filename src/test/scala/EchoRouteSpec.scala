
import akka.testkit.{TestActorRef, TestProbe}
import kz.rio._
import kz.rio.core.{EchoActor, PingActor}
import kz.rio.routing._
import org.scalatest.{FlatSpec, Matchers}
import spray.http._
import spray.routing._
import spray.testkit.ScalatestRouteTest


/**
 * Created by irybakov on 1/9/16.
 */
class EchoRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers {

  val echoService = TestProbe()

  def restRouting = TestActorRef(new RestRouting() {

    override def handleEcho(message : RestMessage): Route =
      ctx => perRequest(ctx, echoService.ref, message)
  })

  "Post to RestRouting" should " return echo json" in {
    val postEcho = Post("/echo",
      HttpEntity(
          MediaTypes.`application/json`,
          """{"message":"test"}"""
      )
    ) ~> restRouting.underlyingActor.route

    echoService.expectMsg(EchoActor.Echo("test"))
    echoService.reply(EchoActor.EchoResponse("test echo"))

    postEcho ~> check {
      responseAs[String] should equal("""{"echo":"test echo"}""")
    }
  }
}
