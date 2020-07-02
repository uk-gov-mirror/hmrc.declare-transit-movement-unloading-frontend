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
import models.{ArrivalId, UnloadingRemarksRejectionMessage}
import play.api.libs.json.{JsObject, Json}

case class UnloadingRemarksRejectionViewModel(page: String, json: JsObject)

object UnloadingRemarksRejectionViewModel {

  def apply(rejectionMessage: UnloadingRemarksRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId): UnloadingRemarksRejectionViewModel = {

    def genericJson: JsObject =
      Json.obj(
        "mrn"              -> rejectionMessage.movementReferenceNumber,
        "errors"           -> rejectionMessage.errors,
        "contactUrl"       -> enquiriesUrl,
        "createArrivalUrl" -> routes.IndexController.onPageLoad(arrivalId).url
      )

    val genericRejectionPage = "arrivalGeneralRejection.njk"

    new UnloadingRemarksRejectionViewModel(genericRejectionPage, genericJson)
  }

}
