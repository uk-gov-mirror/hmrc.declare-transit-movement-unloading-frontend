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
import models.ArrivalId
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels._
import viewModels.sections.Section

case class UnloadingRemarksRejectionViewModel(sections: Seq[Section])

object UnloadingRemarksRejectionViewModel {

  //TODO add logic for multiple rejection errors
  def apply(originalValue: String, arrivalId: ArrivalId)(implicit messages: Messages): UnloadingRemarksRejectionViewModel =
    UnloadingRemarksRejectionViewModel(Seq(Section(Seq(vehicleNameRegistrationReference(arrivalId, originalValue)))))

  private def vehicleNameRegistrationReference(arrivalId: ArrivalId, value: String): Row =
    Row(
      key   = Key(msg"changeVehicle.reference.label", classes = Seq("govuk-!-width-one-half")),
      value = Value(lit"$value"),
      actions = List(
        Action(
          content            = msg"site.edit",
          href               = routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId).url,
          visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"changeVehicle.reference.label")),
          attributes         = Map("id" -> "change-vehicle-registration-rejection")
        )
      )
    )

}
