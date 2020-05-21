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
import models.UserAnswers
import models.messages._
import models.reference.Country
import pages._

class ResultOfControlServiceImpl extends ResultOfControlService {

  //TODO: This needs refactoring (give it a map of pages/pointers and build ResultsOfControlDifferentValues automatically)
  def build(userAnswers: UserAnswers): Seq[ResultsOfControl] = {

    implicit val ua: UserAnswers = userAnswers

    val vehicleRegistrationReference: Seq[ResultsOfControl] = resultsOfControlString(VehicleNameRegistrationReferencePage, TransportIdentity)

    val vehicleRegistrationCountry: Seq[ResultsOfControlDifferentValues] = resultsOfControlCountry(VehicleRegistrationCountryPage, TransportCountry)

    val totalNumberOfItemsPage: Seq[ResultsOfControl] = resultsOfControlInt(TotalNumberOfItemsPage, NumberOfItems)

    val totalNumberOfPackagesPage: Seq[ResultsOfControl] = resultsOfControlInt(TotalNumberOfPackagesPage, NumberOfPackages)

    val grossMassAmount: Seq[ResultsOfControl] = resultsOfControlString(GrossMassAmountPage, GrossMass)

    vehicleRegistrationReference ++ vehicleRegistrationCountry ++ totalNumberOfItemsPage ++ totalNumberOfPackagesPage ++ grossMassAmount
  }

  private def resultsOfControlString(questionPage: QuestionPage[String], pointerIdentity: PointerIdentity)(
    implicit ua: UserAnswers): Seq[ResultsOfControlDifferentValues] =
    ua.get(questionPage)
      .map {
        value =>
          ResultsOfControlDifferentValues(
            PointerToAttribute(pointerIdentity),
            value
          )
      }
      .toSeq

  private def resultsOfControlInt(questionPage: QuestionPage[Int], pointerIdentity: PointerIdentity)(
    implicit ua: UserAnswers): Seq[ResultsOfControlDifferentValues] =
    ua.get(questionPage)
      .map {
        value =>
          ResultsOfControlDifferentValues(
            PointerToAttribute(pointerIdentity),
            value.toString
          )
      }
      .toSeq

  private def resultsOfControlCountry(questionPage: QuestionPage[Country], pointerIdentity: PointerIdentity)(
    implicit ua: UserAnswers): Seq[ResultsOfControlDifferentValues] =
    ua.get(questionPage) match {
      case Some(Country(_, code, _)) =>
        Seq(
          ResultsOfControlDifferentValues(
            PointerToAttribute(pointerIdentity),
            code
          ))
      case _ => Seq.empty
    }
}

trait ResultOfControlService {
  def build(userAnswers: UserAnswers): Seq[ResultsOfControl]
}
