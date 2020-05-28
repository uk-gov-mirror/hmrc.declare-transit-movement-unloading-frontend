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
import models.{ArrivalId, Movement, MovementMessage, MovementReferenceNumber}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

class UnloadingConnectorImpl @Inject()(val config: FrontendAppConfig, val http: HttpClient) extends UnloadingConnector {

  /**
    * Connector SHOULD
    * - Consider returning more meaningful responses on failure (when we write the calling service)
    */
  def get(arrivalId: ArrivalId)(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Option[Movement]] = {

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
class UnloadingConnectorTemporary @Inject()(val config: FrontendAppConfig, val http: HttpClient) extends UnloadingConnector {

  val unloadingPermissionSeals: Elem   = XML.load(getClass.getResourceAsStream("/resources/unloadingPermissionSeals.xml"))
  val unloadingPermissionNoSeals: Elem = XML.load(getClass.getResourceAsStream("/resources/unloadingPermissionNoSeals.xml"))

  def get(arrivalId: ArrivalId)(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Option[Movement]] = arrivalId match {
    case ArrivalId(1) =>
      Future.successful(Some(Movement(
        Seq(MovementMessage(messageType = "IE043A", message = unloadingPermissionSeals.toString(), mrn = MovementReferenceNumber("19IT02110010007827").get)))))
    case ArrivalId(2) =>
      Future.successful(
        Some(Movement(Seq(
          MovementMessage(messageType = "IE043A", message = unloadingPermissionNoSeals.toString(), mrn = MovementReferenceNumber("19IT02110010007827").get)))))
    case _ => Future.successful(None)
  }

}

trait UnloadingConnector {
  def get(arrivalId: ArrivalId)(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Option[Movement]]
}
