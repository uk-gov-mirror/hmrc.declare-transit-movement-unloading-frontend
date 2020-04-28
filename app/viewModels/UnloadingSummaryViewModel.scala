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
import cats.data.NonEmptyList
import models.reference.Country
import models.{Index, MovementReferenceNumber, NormalMode, UnloadingPermission, UserAnswers}
import pages._
import queries.SealsQuery
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Row}
import uk.gov.hmrc.viewmodels._
import utils.UnloadingSummaryRow
import viewModels.sections.Section

case class UnloadingSummaryViewModel(sections: Seq[Section])

object UnloadingSummaryViewModel {

  def apply(userAnswers: UserAnswers, transportCountry: Option[Country], sealCount: Int)(
    implicit unloadingPermission: UnloadingPermission): UnloadingSummaryViewModel = {

    implicit val unloadingSummaryRow: UnloadingSummaryRow = new UnloadingSummaryRow(userAnswers)

    UnloadingSummaryViewModel(SealsSection(userAnswers, sealCount) ++ TransportSection(userAnswers, transportCountry) ++ ItemsSection(userAnswers))
  }

}

object SealsSection {

  def apply(userAnswers: UserAnswers, sealCount: Int)(implicit unloadingPermission: UnloadingPermission,
                                                      unloadingSummaryRow: UnloadingSummaryRow): Seq[Section] =
    userAnswers.get(SealsQuery) match {
      case Some(seals) =>
        val rows: Seq[Row] = seals.zipWithIndex.map(
          sealNumber => {
            SummaryRow.rowWithIndex(Index(sealNumber._2))(None)(sealNumber._1)(unloadingSummaryRow.seals)
          }
        )

        Seq(Section(msg"changeSeal.title", rows, addLink(userAnswers.id, sealCount)))

      case None =>
        unloadingPermission.seals match {
          case Some(seals) =>
            val rows: Seq[Row] = seals.SealId.zipWithIndex.map(
              unloadingPermissionValue => {
                val sealAnswer = SummaryRow.userAnswerWithIndex(Index(unloadingPermissionValue._2))(userAnswers)(NewSealNumberPage)
                SummaryRow.rowWithIndex(Index(unloadingPermissionValue._2))(sealAnswer)(unloadingPermissionValue._1)(unloadingSummaryRow.seals)
              }
            )
            Seq(Section(msg"changeSeal.title", rows))

          case None =>
            Seq(Section(msg"changeSeal.title", Nil, addLink(userAnswers.id, sealCount = 0)))
        }
    }

  private def addLink(mrn: MovementReferenceNumber, sealCount: Int): Action =
    Action(
      content            = msg"unloadingSummary.addSeal.link.text",
      href               = controllers.routes.NewSealNumberController.onPageLoad(mrn, Index(sealCount), NormalMode).url,
      visuallyHiddenText = Some(msg"unloadingSummary.addSeal.link.text"),
      attributes         = Map("id" -> s"""add-seal""")
    )
}

object TransportSection {

  def apply(userAnswers: UserAnswers, summaryTransportCountry: Option[Country])(implicit unloadingPermission: UnloadingPermission,
                                                                                unloadingSummaryRow: UnloadingSummaryRow): Seq[Section] = {

    val vehicleAnswer: Option[String] = SummaryRow.userAnswerString(userAnswers)(VehicleNameRegistrationReferencePage)
    val transportIdentity: Seq[Row]   = SummaryRow.row(vehicleAnswer)(unloadingPermission.transportIdentity)(unloadingSummaryRow.vehicleUsed)

    val transportCountryDescription: Option[String] = summaryTransportCountry match {
      case Some(country) => Some(country.description)
      case None          => unloadingPermission.transportCountry
    }

    val countryAnswer: Option[String] = SummaryRow.userAnswerCountry(userAnswers)(VehicleRegistrationCountryPage)
    val transportCountry: Seq[Row]    = SummaryRow.row(countryAnswer)(transportCountryDescription)(unloadingSummaryRow.registeredCountry)

    transportIdentity ++ transportCountry match {
      case transport if transport.nonEmpty =>
        Seq(Section(msg"vehicleUsed.title", transport))
      case _ => Nil
    }
  }
}

object ItemsSection {

  def apply(userAnswers: UserAnswers)(implicit unloadingPermission: UnloadingPermission, unloadingSummaryRow: UnloadingSummaryRow): Seq[Section] = {
    val grossMassAnswer: Option[String] = SummaryRow.userAnswerString(userAnswers)(GrossMassAmountPage)
    val grossMassRow: Seq[Row]          = SummaryRow.row(grossMassAnswer)(Some(unloadingPermission.grossMass))(unloadingSummaryRow.grossMass)

    val itemsRow: NonEmptyList[Row] = SummaryRow.rowGoodsItems(unloadingPermission.goodsItems)(userAnswers)(unloadingSummaryRow.items)

    val commentsAnswer: Option[String] = SummaryRow.userAnswerString(userAnswers)(ChangesToReportPage)
    val commentsRow: Seq[Row]          = SummaryRow.row(commentsAnswer)(None)(unloadingSummaryRow.comments)

    Seq(Section(msg"changeItems.title", grossMassRow ++ itemsRow.toList ++ commentsRow))
  }
}
