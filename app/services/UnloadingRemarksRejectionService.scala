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
import connectors.UnloadingConnector
import models.{ArrivalId, UnloadingRemarksRejectionMessage}
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksRejectionService @Inject()(connector: UnloadingConnector) {

  def unloadingRemarksRejectionMessage(arrivalId: ArrivalId)(implicit hc: HeaderCarrier,
                                                             ec: ExecutionContext): Future[Option[UnloadingRemarksRejectionMessage]] =
    connector.getSummary(arrivalId) flatMap {
      case Some(summary) =>
        println(s"*********")
        println(s"SUMMARY $summary")

        summary.messagesLocation.arrivalRejection match {
          case Some(rejectionLocation) => {
            println(s"*********")
            println(s"REJECTION MESSAGE $rejectionLocation")
            connector.getRejectionMessage(rejectionLocation)
          }
          case _ => {
            println(s"NO REJECTION")
            Future.successful(None)
          }
        }
      case _ => {

        println(s"NO SUMMARY")

        Future.successful(None)
      }
    }
}
