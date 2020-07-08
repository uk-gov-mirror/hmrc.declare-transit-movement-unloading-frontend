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
import models.{ArrivalId, CheckMode, UnloadingRemarksRejectionMessage}
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels._
import viewModels.sections.Section

case class UnloadingRemarksRejectionViewModel(page: String, json: JsObject)

object UnloadingRemarksRejectionViewModel {

  def apply(rejectionMessage: UnloadingRemarksRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId)(
    implicit messages: Messages): UnloadingRemarksRejectionViewModel = {

    val section = Seq(Section(Seq(vehicleNameRegistrationReference(arrivalId, rejectionMessage.errors.head.originalAttributeValue.getOrElse("")))))

    def genericJson: JsObject =
      Json.obj(
        "sections"         -> Json.toJson(section),
        "contactUrl"       -> enquiriesUrl,
        "createArrivalUrl" -> routes.IndexController.onPageLoad(arrivalId).url
      )

    val genericRejectionPage = "unloadingRemarksRejection.njk"

    new UnloadingRemarksRejectionViewModel(genericRejectionPage, genericJson)
  }

  private def vehicleNameRegistrationReference(arrivalId: ArrivalId, value: String): Row =
    Row(
      key   = Key(msg"vehicleNameRegistrationReference.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
      value = Value(lit"$value"),
      actions = List(
        Action(
          content            = msg"site.edit",
          href               = routes.VehicleNameRegistrationReferenceController.onPageLoad(arrivalId, CheckMode).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"vehicleNameRegistrationReference.checkYourAnswersLabel"))
        )
      )
    )

}
