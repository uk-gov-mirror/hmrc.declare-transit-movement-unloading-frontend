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
import com.google.inject.Inject
import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import connectors.UnloadingConnector
import models.{ArrivalId, Movement, UnloadingPermission, UserAnswers}
import queries.SealsQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.XML

class UnloadingPermissionServiceImpl @Inject()(connector: UnloadingConnector) extends UnloadingPermissionService {

  def getUnloadingPermission(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UnloadingPermission]] =
    connector.get(arrivalId).map {
      case Some(Movement(_, messages)) =>
        messages.reverse match {
          case head :: _ =>
            XmlReader.of[UnloadingPermission].read(XML.loadString(head.message)) match {
              case ParseSuccess(unloadingPermission) => Some(unloadingPermission)
              case _                                 => None //TODO: Consider what happens when the message isn't unloading permission
            }
          case _ => None
        }

      case None => None
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
    }
}

trait UnloadingPermissionService {
  def getUnloadingPermission(arrivalId: ArrivalId)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UnloadingPermission]]
  def convertSeals(userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UserAnswers]]
}
