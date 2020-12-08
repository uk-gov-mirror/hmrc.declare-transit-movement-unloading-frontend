/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.XMLWrites._
import models.{XMLReads, _}
import models.messages.UnloadingRemarksRequest
import play.api.Logger
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class UnloadingConnectorImpl @Inject()(
  val config: FrontendAppConfig,
  val http: HttpClient,
  val ws: WSClient
)(implicit ec: ExecutionContext)
    extends UnloadingConnector
    with HttpErrorFunctions {

  def post(arrivalId: ArrivalId, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val url = s"${config.arrivalsBackend}/movements/arrivals/${arrivalId.value}/messages/"

    val headers = Seq("Content-Type" -> "application/xml", "Channel" -> "web")

    http.POSTString[HttpResponse](url, unloadingRemarksRequest.toXml.toString, headers)
  }

  def getUnloadingPermission(unloadingPermission: String)(implicit headerCarrier: HeaderCarrier): Future[Option[UnloadingPermission]] = {

    val serviceUrl = s"${config.arrivalsBackendBaseUrl}$unloadingPermission"
    http.GET[HttpResponse](serviceUrl) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
        XMLReads.readAs[UnloadingPermission](message)
      case _ =>
        Logger.error(s"Get UnloadingPermission failed to return data")
        None
    }
  }

  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]] = {

    val serviceUrl: String = s"${config.arrivalsBackend}/movements/arrivals/${arrivalId.value}/messages/summary"
    http.GET[HttpResponse](serviceUrl) map {
      case responseMessage if is2xx(responseMessage.status) =>
        Some(responseMessage.json.as[MessagesSummary])
      case _ =>
        Logger.error(s"Get Summary failed to return data")
        None
    }
  }

  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRejectionMessage]] = {
    val serviceUrl = s"${config.arrivalsBackendBaseUrl}$rejectionLocation"

    http.GET[HttpResponse](serviceUrl) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
        XMLReads.readAs[UnloadingRemarksRejectionMessage](message)
      case _ =>
        Logger.error(s"Get Rejection Message failed to return data")
        None
    }
  }

  def getUnloadingRemarksMessage(unloadingRemarksLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRequest]] = {
    val serviceUrl = s"${config.arrivalsBackendBaseUrl}$unloadingRemarksLocation"

    http.GET[HttpResponse](serviceUrl) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
        XMLReads.readAs[UnloadingRemarksRequest](message)
      case _ =>
        Logger.error(s"getUnloadingRemarksMessage failed to return data")
        None
    }
  }

  def getPDF(arrivalId: ArrivalId, bearerToken: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    val serviceUrl: String = s"${config.arrivalsBackend}/movements/arrivals/${arrivalId.value}/unloading-permission"
    ws.url(serviceUrl)
      .withHttpHeaders(("Authorization", bearerToken))
      .get
  }

}

trait UnloadingConnector {
  def getUnloadingPermission(unloadingPermission: String)(implicit headerCarrier: HeaderCarrier): Future[Option[UnloadingPermission]]
  def post(arrivalId: ArrivalId, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[HttpResponse]
  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]]
  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRejectionMessage]]
  def getUnloadingRemarksMessage(unloadinRemarksLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRequest]]
  def getPDF(arrivalId: ArrivalId, bearerToken: String)(implicit hc: HeaderCarrier): Future[WSResponse]
}
