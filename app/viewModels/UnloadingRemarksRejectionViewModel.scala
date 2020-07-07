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

package viewModels
import controllers.routes
import models.{ArrivalId, CheckMode, FunctionalError, UnloadingRemarksRejectionMessage}
import play.api.libs.json.{JsObject, Json}

case class UnloadingRemarksRejectionViewModel(page: String, json: JsObject)

object UnloadingRemarksRejectionViewModel {

  def errorDetails(errors: Seq[FunctionalError], arrivalId: ArrivalId): Seq[ErrorDetails] =
    errors.map(error => ErrorDetails(error, routes.VehicleNameRegistrationReferenceController.onPageLoad(arrivalId, CheckMode).url))

  def apply(rejectionMessage: UnloadingRemarksRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId): UnloadingRemarksRejectionViewModel = {

    def genericJson: JsObject =
      Json.obj(
        "mrn"              -> rejectionMessage.movementReferenceNumber,
        "errors"           -> errorDetails(rejectionMessage.errors, arrivalId),
        "contactUrl"       -> enquiriesUrl,
        "createArrivalUrl" -> routes.IndexController.onPageLoad(arrivalId).url
      )

    val genericRejectionPage = "unloadingRemarksRejection.njk"

    new UnloadingRemarksRejectionViewModel(genericRejectionPage, genericJson)
  }

}
