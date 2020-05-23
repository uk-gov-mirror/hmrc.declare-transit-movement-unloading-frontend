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
import models.{Movement, MovementMessage}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

class UnloadingConnectorImpl @Inject()(val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends UnloadingConnector {

  def post(arrivalId: Int, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] = {

    val url = config.arrivalsBackend ++ s"/movements/arrivals/${arrivalId.toString}/messages/"

    val headers = Seq(("Content-Type", "application/xml"))

    //TODO: Remove the map and use the custom httpReads in package
    http
      .POSTString[HttpResponse](url, unloadingRemarksRequest.toXml.toString, headers)
      .map(x => Some(x))
      .recover {
        case _ => None
      }
  }

  /**
    * Connector SHOULD
    * - Consider returning more meaningful responses on failure (when we write the calling service)
    */
  def get(arrivalId: Int)(implicit headerCarrier: HeaderCarrier): Future[Option[Movement]] = {

    val url = config.arrivalsBackend ++ s"/movements/arrivals/${arrivalId.toString}/messages/"

    http
      .GET[Movement](url)
      .map(x => Some(x))
      .recover {
        case _ => None
      }
  }

}

//TODO: This needs removing and UnloadingConnectorImpl needs injecting once backend is available
class UnloadingConnectorTemporary @Inject()(val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends UnloadingConnector {

  val unloadingPermissionSeals: Elem   = XML.load(getClass.getResourceAsStream("/resources/unloadingPermissionSeals.xml"))
  val unloadingPermissionNoSeals: Elem = XML.load(getClass.getResourceAsStream("/resources/unloadingPermissionNoSeals.xml"))

  def get(arrivalId: Int)(implicit headerCarrier: HeaderCarrier): Future[Option[Movement]] = arrivalId match {
    case 1 => Future.successful(Some(Movement(Seq(MovementMessage(messageType = "IE043A", message = unloadingPermissionSeals.toString())))))
    case 2 => Future.successful(Some(Movement(Seq(MovementMessage(messageType = "IE043A", message = unloadingPermissionNoSeals.toString())))))
    case _ => Future.successful(None)
  }

  def post(arrivalId: Int, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] = ???

}

trait UnloadingConnector {
  def get(arrivalId: Int)(implicit headerCarrier: HeaderCarrier): Future[Option[Movement]]
  def post(arrivalId: Int, unloadingRemarksRequest: UnloadingRemarksRequest)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]]
}
