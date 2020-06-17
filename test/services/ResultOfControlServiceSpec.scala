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
import base.SpecBase
import models.messages._
import models.reference.Country
import pages._

class ResultOfControlServiceSpec extends SpecBase {

  val service = new ResultOfControlServiceImpl

  "ResultOfControlServiceSpec" - {

    "handle when UserAnswers" - {

      "are empty" in {
        service.build(emptyUserAnswers) mustBe Nil
      }

      "contains VehicleNameRegistrationReference value" in {

        val userAnswersUpdated = emptyUserAnswers
          .set(VehicleNameRegistrationReferencePage, "reference")
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlDifferentValues(
            PointerToAttribute(TransportIdentity),
            "reference"
          ))
      }

      "contains VehicleRegistrationCountry value" in {

        val userAnswersUpdated = emptyUserAnswers
          .set(VehicleRegistrationCountryPage, Country("state", "FR", "description"))
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlDifferentValues(
            PointerToAttribute(TransportCountry),
            "FR"
          ))

      }

      "contains NumberOfItems value" in {

        val userAnswersUpdated = emptyUserAnswers
          .set(TotalNumberOfItemsPage, 123: Int)
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlDifferentValues(
            PointerToAttribute(NumberOfItems),
            "123"
          ))

      }

      "contains NumberOfPackages value" in {

        val userAnswersUpdated = emptyUserAnswers
          .set(TotalNumberOfPackagesPage, 123: Int)
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlDifferentValues(
            PointerToAttribute(NumberOfPackages),
            "123"
          ))

      }

      "contains GrossMass value" in {
        val userAnswersUpdated = emptyUserAnswers
          .set(GrossMassAmountPage, "12234567")
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlDifferentValues(
            PointerToAttribute(GrossMass),
            "12234567"
          ))
      }

      "contains multiple values" in {

        val userAnswersUpdated = emptyUserAnswers
          .set(VehicleNameRegistrationReferencePage, "reference")
          .success
          .value
          .set(VehicleRegistrationCountryPage, Country("state", "FR", "description"))
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlDifferentValues(
            PointerToAttribute(TransportIdentity),
            "reference"
          ),
          ResultsOfControlDifferentValues(
            PointerToAttribute(TransportCountry),
            "FR"
          )
        )
      }

      "AreAnySealsBroken is true" in {
        val userAnswersUpdated = emptyUserAnswers
          .set(AreAnySealsBrokenPage, true)
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlOther(
            "Some seals are broken"
          )
        )
      }

      "CanSealsBeRead is false" in {
        val userAnswersUpdated = emptyUserAnswers
          .set(CanSealsBeReadPage, false)
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlOther(
            "Some seals not readable"
          )
        )
      }

      "AreAnySealsBroken is true and CanSealsBeRead is false" in {
        val userAnswersUpdated = emptyUserAnswers
          .set(AreAnySealsBrokenPage, true)
          .success
          .value
          .set(CanSealsBeReadPage, false)
          .success
          .value

        service.build(userAnswersUpdated) mustBe Seq(
          ResultsOfControlOther(
            "Some seals are broken"
          ),
          ResultsOfControlOther(
            "Some seals not readable"
          )
        )
      }

    }

  }

}
