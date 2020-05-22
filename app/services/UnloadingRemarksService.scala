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
import models.{UnloadingPermission, UserAnswers}
import play.api.Logger
import play.api.http.Status._
import repositories.InterchangeControlReferenceIdRepository

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksService @Inject()(config: FrontendAppConfig,
                                        metaService: MetaService,
                                        remarksService: RemarksService,
                                        interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository)(implicit ec: ExecutionContext) {

  def submit(eori: String, userAnswers: UserAnswers, unloadingPermission: UnloadingPermission) =
    interchangeControlReferenceIdRepository
      .nextInterchangeControlReferenceId()
      .flatMap {
        interchangeControlReference =>
          {
            val meta = metaService.build(eori, interchangeControlReference)
            // set RemarksService
            // populate UnloadingRemarksRequest
            // set Meta (internal data) - consider Meta.apply
            // set Header (from unloading permission)
            // set trader (from unloading permission)
            // set presentationOffice (from unloading permission)
            // set unloadingRemarks
            // set seals (either useranswers or unloading permission)
            // set goods items (from unloading permission)
            Future.successful(ACCEPTED)

          }
      }
      .recover {
        case ex =>
          Logger.error(s"${ex.getMessage}")
          None
      }

}
