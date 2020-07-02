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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.XMLWrites._
import models.messages.UnloadingRemarksRequest
import models.{ArrivalId, MessagesSummary, Movement, UnloadingRemarksRejectionMessage}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class UnloadingConnectorImpl @Inject()(val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends UnloadingConnector {

  def post(arrivalId: ArrivalId, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val url = config.arrivalsBackend ++ s"/movements/arrivals/${arrivalId.value}/messages/"

    val headers = Seq(("Content-Type", "application/xml"))

    http.POSTString[HttpResponse](url, unloadingRemarksRequest.toXml.toString, headers)
  }

  /**
    * Connector SHOULD
    * - Consider returning more meaningful responses on failure (when we write the calling service)
    */
  def get(arrivalId: ArrivalId)(implicit headerCarrier: HeaderCarrier): Future[Option[Movement]] = {

    val url = config.arrivalsBackend ++ s"/movements/arrivals/${arrivalId.value}/messages/"

    http
      .GET[Movement](url)
      .map(x => Some(x))
      .recover {
        case _ => None
      }
  }
//  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]] = {
//
//    val serviceUrl: String = s"${config.destinationUrl}/movements/arrivals/${arrivalId.value}/messages/summary"
//    http.GET[HttpResponse](serviceUrl) map {
//      case responseMessage if is2xx(responseMessage.status) => Some(responseMessage.json.as[MessagesSummary])
//      case _                                                => None
//    }
//  }

//  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[ArrivalNotificationRejectionMessage]] = {
//    val serviceUrl = s"${config.baseDestinationUrl}$rejectionLocation"
//    http.GET[HttpResponse](serviceUrl) map {
//      case responseMessage if is2xx(responseMessage.status) =>
//        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
//        XmlReader.of[ArrivalNotificationRejectionMessage].read(message).toOption
//      case _ => None
//    }
//  }
  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]]                                = ???
  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRejectionMessage]] = ???
}

trait UnloadingConnector {
  def get(arrivalId: ArrivalId)(implicit headerCarrier: HeaderCarrier): Future[Option[Movement]]
  def post(arrivalId: ArrivalId, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[HttpResponse]
  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]]
  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[UnloadingRemarksRejectionMessage]]

}
