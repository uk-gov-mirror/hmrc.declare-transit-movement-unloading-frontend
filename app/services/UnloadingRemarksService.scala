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
import config.FrontendAppConfig
import connectors.UnloadingConnector
import models.messages.{Meta, UnloadingRemarksRequest}
import models.{ArrivalId, UnloadingPermission, UserAnswers}
import play.api.Logger
import play.api.http.Status._
import repositories.InterchangeControlReferenceIdRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksService @Inject()(config: FrontendAppConfig,
                                        metaService: MetaService,
                                        remarksService: RemarksService,
                                        unloadingRemarksRequestService: UnloadingRemarksRequestService,
                                        interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository,
                                        unloadingConnector: UnloadingConnector)(implicit ec: ExecutionContext) {

  def submit(arrivalId: ArrivalId, eori: String, userAnswers: UserAnswers, unloadingPermission: UnloadingPermission)(
    implicit hc: HeaderCarrier): Future[Option[Int]] =
    interchangeControlReferenceIdRepository
      .nextInterchangeControlReferenceId()
      .flatMap {
        interchangeControlReference =>
          {
            remarksService
              .build(userAnswers, unloadingPermission)
              .flatMap {
                unloadingRemarks =>
                  val meta: Meta = metaService.build(eori, interchangeControlReference)

                  val unloadingRemarksRequest: UnloadingRemarksRequest =
                    unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswers)

                  unloadingConnector
                    .post(arrivalId, unloadingRemarksRequest)
                    .flatMap(response => Future.successful(Some(response.status)))
                    .recover {
                      case ex =>
                        Logger.error(s"$ex")
                        Some(SERVICE_UNAVAILABLE)
                    }
              }
          }
      }
      .recover {
        case ex =>
          Logger.error(s"$ex")
          None
      }

}
