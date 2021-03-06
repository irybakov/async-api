
import akka.testkit.{TestActorRef, TestProbe}
import kz.rio._
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

    override def handleRequest(message: DomainMessage): Route = {
      ctx => perRequest(ctx, echoService.ref, message)
    }

    override def handleLocalRequest(message: DomainMessage): Route = {
      ctx => perRequest(ctx, echoService.ref, message)
    }
  })

  "Post to Container RestRouting" should " return echo json" in {
    val postEcho = Post("/container/echo",
      HttpEntity(
          MediaTypes.`application/json`,
          """{"echo":"container"}"""
      )
    ) ~> restRouting.underlyingActor.route

    echoService.expectMsg(Echo("container"))
    echoService.reply(Echo("container echo"))

    postEcho ~> check {
      responseAs[String] should equal("""{"echo":"container echo"}""")
    }
  }

  "Post to API RestRouting" should " return echo json" in {
    val postEcho = Post("/api/echo",
      HttpEntity(
        MediaTypes.`application/json`,
        """{"echo":"api"}"""
      )
    ) ~> restRouting.underlyingActor.route

    echoService.expectMsg(Echo("api"))
    echoService.reply(Echo("api echo"))

    postEcho ~> check {
      responseAs[String] should equal("""{"echo":"api echo"}""")
    }
  }

}
