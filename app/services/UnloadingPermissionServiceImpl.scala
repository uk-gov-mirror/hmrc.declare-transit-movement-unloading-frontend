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

package services
import com.google.inject.{Inject, Singleton}
import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import connectors.UnloadingConnector
import models.{MovementReferenceNumber, UnloadingPermission, UserAnswers}
import queries.SealsQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.XML

@Singleton
class UnloadingPermissionServiceImpl @Inject()(connector: UnloadingConnector) extends UnloadingPermissionService {

  //TODO: When uri is updated to arrivalId the getUnloadingPermission argument needs updating
  def getUnloadingPermission(mrn: MovementReferenceNumber)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UnloadingPermission]] = {

    //TODO: Only needed until we call backend through connector and update uri to use arrivalId
    val arrivalID = mrn.toString match {
      case "19IT02110010007827" => 1
      case "99IT9876AB88901209" => 2
      case _                    => 3
    }

    connector.get(arrivalID).map {
      case Some(x) => {
        XmlReader.of[UnloadingPermission].read(XML.loadString(x.messages.head.message)) match {
          case ParseSuccess(unloadingPermission) => Some(unloadingPermission) //Some(unloadingPermissionSeals)
          case _                                 => None
        }
      }
      case None => None
    }

  }

  //TODO: Refactor
  def convertSeals(userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UserAnswers]] =
    getUnloadingPermission(userAnswers.id).flatMap {
      case Some(unloadingPermission) =>
        unloadingPermission.seals match {
          case Some(seals) =>
            Future.successful(userAnswers.set(SealsQuery, seals.SealId).map(ua => ua).toOption)
          case _ => Future.successful(Some(userAnswers))
        }
      case _ => Future.successful(None)
    }

  def convertSeals(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Option[UserAnswers] =
    unloadingPermission.seals match {
      case Some(seals) =>
        userAnswers.set(SealsQuery, seals.SealId).map(ua => ua).toOption
      case _ => Some(userAnswers)
      case _ => None
    }
}

trait UnloadingPermissionService {
  def getUnloadingPermission(mrn: MovementReferenceNumber)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UnloadingPermission]]
  def convertSeals(userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UserAnswers]]
}
