package connectors

import java.time.LocalDate

import com.github.tomakehurst.wiremock.client.WireMock._
import generators.MessagesModelGenerators
import models.messages.UnloadingRemarksRequest
import models.{ArrivalId, ErrorPointer, ErrorType, FunctionalError, MessagesLocation, MessagesSummary, UnloadingRemarksRejectionMessage}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier

import scala.xml.NodeSeq

class UnloadingConnectorSpec extends FreeSpec
  with ScalaFutures
  with IntegrationPatience
  with WireMockSuite
  with MustMatchers
  with OptionValues
  with MessagesModelGenerators
  with ScalaCheckPropertyChecks {

  import UnloadingConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.arrivals-backend.port"

  private lazy val connector: UnloadingConnector = app.injector.instanceOf[UnloadingConnectorImpl]

  implicit val hc = HeaderCarrier()

  "UnloadingConnectorSpec" - {

    "POST" - {

      "should handle an ACCEPTED response" in {
        server.stubFor(
          post(postUri)
            .willReturn(status(ACCEPTED)))

        val unloadingRemarksRequestObject = arbitrary[UnloadingRemarksRequest]

        val unloadingRemarksRequest: UnloadingRemarksRequest = unloadingRemarksRequestObject .sample.get

        val result = connector.post(arrivalId, unloadingRemarksRequest).futureValue

        result.status mustBe ACCEPTED
      }

      "should handle client and server errors" in {

        val errorResponsesCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

        forAll(arbitrary[UnloadingRemarksRequest], errorResponsesCodes) {
          (unloadingRemarksRequest, errorResponseCode) =>

            server.stubFor(
              post(postUri)
                .willReturn(aResponse().withStatus(errorResponseCode)))

            connector.post(arrivalId, unloadingRemarksRequest).futureValue.status mustBe errorResponseCode
        }
      }
    }

    "GET" - {

      "should handle a 200 response" - {

        "containing single message" in {
          server.stubFor(
            get(getUri)
              .willReturn(okJson(unloadingJson)
              ))

          val movement = connector.get(arrivalId).futureValue
          movement.get.messages.length mustBe 1
          movement.get.messages.head.messageType mustBe "IE043E"
          movement.get.messages.head.message mustBe "<CC043A></CC043A>"
        }

        "containing multiple messages" in {
          server.stubFor(
            get(getUri)
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
          get(getUri)
            .willReturn(notFound)
        )
        connector.get(arrivalId).futureValue mustBe None
      }

      "should handle client and server errors" in {
        forAll(responseCodes) {
          code =>
            server.stubFor(
              get(getUri)
                .willReturn(aResponse().withStatus(code))
            )
            connector.get(arrivalId).futureValue mustBe None
        }
      }

      "should return None when empty object is returned" in {

        server.stubFor(
          get(getUri)
            .willReturn(okJson(emptyObject))
        )
        connector.get(arrivalId).futureValue mustBe None
      }

      "should return None when json is malformed" in {

        server.stubFor(
          get(getUri)
            .willReturn(okJson(malFormedJson))
        )
        connector.get(arrivalId).futureValue mustBe None
      }
    }

    "getSummary" - {

      "must be return summary of messages" in {
        val json = Json.obj(
          "arrivalId" -> arrivalId.value,
          "messages" -> Json.obj(
            "IE044" -> s"/movements/arrivals/${arrivalId.value}/messages/3",
            "IE058" -> s"/movements/arrivals/${arrivalId.value}/messages/5"
          )
        )

        val messageAction =
          MessagesSummary(arrivalId,
            MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some(s"/movements/arrivals/${arrivalId.value}/messages/5")))

        server.stubFor(
          get(urlEqualTo(summaryUri))
            .willReturn(
              okJson(json.toString)
            )
        )
        connector.getSummary(arrivalId).futureValue mustBe Some(messageAction)
      }

      "must return 'None' when an error response is returned from getSummary" in {
        forAll(responseCodes) {
          code: Int =>
            server.stubFor(
              get(summaryUri)
                .willReturn(aResponse().withStatus(code))
            )

            connector.getSummary(ArrivalId(1)).futureValue mustBe None
        }
      }
    }

    "getRejectionMessage" - {
      "must return valid 'rejection message'" in {
        val genRejectionError     = arbitrary[ErrorType].sample.value
        val rejectionXml: NodeSeq =             <CC058A>
          <HEAHEA>
            <DocNumHEA5>19IT021300100075E9</DocNumHEA5>
            <UnlRemRejDatHEA218>20191018</UnlRemRejDatHEA218>
          </HEAHEA>
          <FUNERRER1>
            <ErrTypER11>{genRejectionError.code}</ErrTypER11>
            <ErrPoiER12>Message type</ErrPoiER12>
            <OriAttValER14>GB007A</OriAttValER14>
          </FUNERRER1>
        </CC058A>

        val json = Json.obj("message" -> rejectionXml.toString())

        server.stubFor(
          get(urlEqualTo(rejectionUri))
            .willReturn(
              okJson(json.toString)
            )
        )
        val expectedResult = Some(
          UnloadingRemarksRejectionMessage(
            "19IT021300100075E9",
            LocalDate.of(2019, 10, 18),
            None,
            List(FunctionalError(genRejectionError, ErrorPointer("Message type"), None, Some("GB007A")))
          ))
        val result = connector.getRejectionMessage(rejectionUri).futureValue
        result mustBe expectedResult
      }

      "must return None for malformed xml'" in {
        val rejectionXml: NodeSeq = <CC058A>
          <HEAHEA><DocNumHEA5>19IT021300100075E9</DocNumHEA5>
          </HEAHEA>
        </CC058A>

        val json = Json.obj("message" -> rejectionXml.toString())

        server.stubFor(
          get(urlEqualTo(rejectionUri))
            .willReturn(
              okJson(json.toString)
            )
        )

        connector.getRejectionMessage(rejectionUri).futureValue mustBe None
      }

      "must return None when an error response is returned from getRejectionMessage" in {
        forAll(responseCodes) {
          code: Int =>
            server.stubFor(
              get(rejectionUri)
                .willReturn(aResponse().withStatus(code))
            )

            connector.getRejectionMessage(rejectionUri).futureValue mustBe None
        }
      }
    }

    "getUnloadingRemarksMessage" - {
      "must return valid 'unloading remarks message'" in {
        val sampleXml: NodeSeq = <sample>test</sample>

        val json = Json.obj("message" -> sampleXml.toString())

        server.stubFor(
          get(urlEqualTo(unloadingRemarksUri))
            .willReturn(
              okJson(json.toString)
            )
        )

        val result = connector.getUnloadingRemarksMessage(unloadingRemarksUri).futureValue
        result mustBe Some(sampleXml)
      }

      "must return None when an error response is returned from getRejectionMessage" in {
        forAll(responseCodes) {
          code: Int =>
            server.stubFor(
              get(unloadingRemarksUri)
                .willReturn(aResponse().withStatus(code))
            )

            connector.getUnloadingRemarksMessage(rejectionUri).futureValue mustBe None
        }
      }
    }
  }

}

object UnloadingConnectorSpec {

  private val unloadingJson =
      Json.obj("movementReferenceNumber" -> "19IT02110010007827", "messages" -> Json.arr(
        Json.obj(
        "messageType" -> "IE043E",
          "message" -> "<CC043A></CC043A>"))).toString()

  private val jsonMultiple =
      Json.obj("movementReferenceNumber" -> "19IT02110010007827", "messages" -> Json.arr(
        Json.obj(
          "messageType" -> "IE015E",
          "message" -> "<CC015A></CC015A>"
          ),
        Json.obj(
          "messageType" -> "IE043E",
          "message" -> "<CC043A></CC043A>"
          ))).toString()

  private val malFormedJson =
    """
      ||{
      |[
      |}
    """.stripMargin

   private val arrivalId = ArrivalId(1)
   private val emptyObject: String = JsObject.empty.toString()
   private val getUri = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/"
   private val postUri = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/"
   private val summaryUri = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/summary"
   private val rejectionUri = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/1"
   private val unloadingRemarksUri = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/1"

  val responseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)
}