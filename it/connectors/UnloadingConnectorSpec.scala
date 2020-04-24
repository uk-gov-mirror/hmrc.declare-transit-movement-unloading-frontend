package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{Message, Movement, MovementReferenceNumber}
import org.scalacheck.Gen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class UnloadingConnectorSpec extends FreeSpec with ScalaFutures with
  IntegrationPatience with WireMockSuite with MustMatchers  with ScalaCheckPropertyChecks {

  import UnloadingConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.arrivals-backend.port"

  private lazy val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnectorImpl]

  implicit val hc = HeaderCarrier()

  "UnloadingConnectorSpec" - {

    "GET" - {

      "should handle a 200 response" - {

        "containing single message" in {
          server.stubFor(
            get(uri)
              .willReturn(okJson(unloadingJson)
              ))

          val movement = connector.get(arrivalId).futureValue
          movement.get.messages.length mustBe 1
          movement.get.messages.head.messageType mustBe "IE043E"
          movement.get.messages.head.message mustBe "<CC043A></CC043A>"
        }

        "containing multiple messages" in {
          server.stubFor(
            get(uri)
              .willReturn(okJson(jsonMultiple)
              ))

          val movement = connector.get(arrivalId).futureValue
          movement.get.messages.length mustBe 2
          movement.get.messages(0).messageType mustBe "IE015E"
          movement.get.messages(0).message mustBe "<CC015A></CC015A>"
          movement.get.messages(1).messageType mustBe "IE043E"
          movement.get.messages(1).message mustBe "<CC043A></CC043A>"
        }
      }

      "should handle a 404 response" in {

        server.stubFor(
          get(uri)
            .willReturn(notFound)
        )
        connector.get(arrivalId).futureValue mustBe None
      }

      "should handle client and server errors" in {
        forAll(responseCodes) {
          code =>
            server.stubFor(
              get(uri)
                .willReturn(aResponse().withStatus(code))
            )
            connector.get(arrivalId).futureValue mustBe None
        }
      }

      "should return None when empty object is returned" in {

        server.stubFor(
          get(uri)
            .willReturn(okJson(emptyObject))
        )
        connector.get(arrivalId).futureValue mustBe None
      }

      "should return None when json is malformed" in {

        server.stubFor(
          get(uri)
            .willReturn(okJson(malFormedJson))
        )
        connector.get(arrivalId).futureValue mustBe None
      }
    }
  }

}

object UnloadingConnectorSpec {

  private val unloadingJson =
      Json.obj("messages" -> Json.arr(
        Json.obj(
        "messageType" -> "IE043E",
          "message" -> "<CC043A></CC043A>"))).toString()

  private val jsonMultiple =
      Json.obj("messages" -> Json.arr(
        Json.obj(
          "messageType" -> "IE015E",
          "message" -> "<CC015A></CC015A>"),
        Json.obj(
          "messageType" -> "IE043E",
          "message" -> "<CC043A></CC043A>"))).toString()

  private val malFormedJson =
    """
      ||{
      |[
      |}
    """.stripMargin

  private val emptyObject: String = JsObject.empty.toString()
  private val arrivalId = 1
  private val uri = s"/transit-movements-trader-at-destination/movements/arrivals/$arrivalId/messages/"

  val responseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)
}