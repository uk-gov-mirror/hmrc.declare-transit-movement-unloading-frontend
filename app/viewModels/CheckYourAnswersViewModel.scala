/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.data.NonEmptyList
import controllers.routes
import models.reference.Country
import models.{ArrivalId, UnloadingPermission, UserAnswers}
import pages._
import play.api.i18n.Messages
import queries.SealsQuery
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Row}
import uk.gov.hmrc.viewmodels._
import utils.{CheckYourAnswersHelper, UnloadingSummaryRow}
import viewModels.sections.Section

case class CheckYourAnswersViewModel(sections: Seq[Section])

object CheckYourAnswersViewModel {

  def apply(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission, summaryTransportCountry: Option[Country])(
    implicit messages: Messages): CheckYourAnswersViewModel =
    CheckYourAnswersViewModel(
      Seq(
        goodsUnloadedSection(userAnswers),
        sealsSection(userAnswers, unloadingPermission),
        itemsSection(userAnswers, unloadingPermission, summaryTransportCountry)
      ))

  private def sealsSection(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission)(implicit messages: Messages): Section = {
    val checkYourAnswersRow = new CheckYourAnswersHelper(userAnswers)

    val rowCanSealsBeRead: Option[Row]    = checkYourAnswersRow.canSealsBeRead
    val rowAreAnySealsBroken: Option[Row] = checkYourAnswersRow.areAnySealsBroken

    val seals: Option[Row] = (userAnswers.get(SealsQuery), unloadingPermission.seals) match {
      case (Some(userAnswersSeals), _)            => checkYourAnswersRow.seals(userAnswersSeals)
      case (None, Some(unloadingPermissionSeals)) => checkYourAnswersRow.seals(unloadingPermissionSeals.SealId)
      case (_, _)                                 => None
    }

    Section(msg"checkYourAnswers.seals.subHeading", seals.toSeq ++ rowCanSealsBeRead ++ rowAreAnySealsBroken)
  }

  private def goodsUnloadedSection(userAnswers: UserAnswers)(implicit messages: Messages): Section = {
    val checkYourAnswersRow           = new CheckYourAnswersHelper(userAnswers)
    val rowGoodsUnloaded: Option[Row] = checkYourAnswersRow.dateGoodsUnloaded
    Section(rowGoodsUnloaded.toSeq)
  }

  private def itemsSection(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission, summaryTransportCountry: Option[Country])(
    implicit messages: Messages): Section = {

    val unloadingSummaryRow = new UnloadingSummaryRow(userAnswers)

    val transportIdentityAnswer: Option[String] = userAnswers.get(VehicleNameRegistrationReferencePage)
    val transportIdentityRow: Seq[Row]          = SummaryRow.row(transportIdentityAnswer)(unloadingPermission.transportIdentity)(unloadingSummaryRow.vehicleUsedCYA)

    val transportCountryDescription: Option[String] = summaryTransportCountry match {
      case Some(country) => Some(country.description)
      case None          => unloadingPermission.transportCountry
    }

    val countryAnswer: Option[String] = SummaryRow.userAnswerCountry(userAnswers)(VehicleRegistrationCountryPage)
    val transportCountryRow: Seq[Row] = SummaryRow.row(countryAnswer)(transportCountryDescription)(unloadingSummaryRow.registeredCountryCYA)

    val grossMassAnswer: Option[String] = userAnswers.get(GrossMassAmountPage)
    val grossMassRow: Seq[Row]          = SummaryRow.row(grossMassAnswer)(Some(unloadingPermission.grossMass))(unloadingSummaryRow.grossMassCYA)

    val itemsRow: NonEmptyList[Row] = SummaryRow.rowGoodsItems(unloadingPermission.goodsItems)(userAnswers)(unloadingSummaryRow.items)

    val totalNumberOfItemsAnswer: Option[Int] = SummaryRow.userAnswerInt(userAnswers)(TotalNumberOfItemsPage)
    val totalNumberOfItemsRow: Seq[Row] =
      SummaryRow.rowInt(totalNumberOfItemsAnswer)(Some(unloadingPermission.numberOfItems))(unloadingSummaryRow.totalNumberOfItemsCYA)

    val totalNumberOfPackagesAnswer: Option[Int] = SummaryRow.userAnswerInt(userAnswers)(TotalNumberOfPackagesPage)
    val totalNumberOfPackagesRow: Seq[Row] =
      SummaryRow.rowInt(totalNumberOfPackagesAnswer)(unloadingPermission.numberOfPackages)(unloadingSummaryRow.totalNumberOfPackagesCYA)

    val commentsAnswer: Option[String] = SummaryRow.userAnswerString(userAnswers)(ChangesToReportPage)
    val commentsRow: Seq[Row]          = SummaryRow.row(commentsAnswer)(None)(unloadingSummaryRow.commentsCYA)

    Section(
      msg"checkYourAnswers.subHeading",
      buildRows(
        transportIdentityRow ++ transportCountryRow ++ grossMassRow ++ totalNumberOfItemsRow ++ totalNumberOfPackagesRow ++ itemsRow.toList ++ commentsRow,
        userAnswers.id)
    )
  }

  private def buildRows(rows: Seq[Row], arrivalId: ArrivalId): Seq[Row] = rows match {
    case head :: tail => {
      val changeAction = head.copy(
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.UnloadingSummaryController.onPageLoad(arrivalId).url,
            visuallyHiddenText = Some(msg"checkYourAnswers.changeItems.hidden"),
            attributes         = Map("id" -> s"""change-answers""")
          )))

      Seq(changeAction) ++ tail
    }
    case _ => rows

  }
}
