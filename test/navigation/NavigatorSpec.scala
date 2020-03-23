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

package navigation

import base.SpecBase
import controllers.routes
import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, NormalMode, answers)
              .mustBe(routes.IndexController.onPageLoad())
        }
      }

      "must go from date goods unloaded page to can seals be read page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(DateGoodsUnloadedPage, NormalMode, answers)
              .mustBe(routes.CanSealsBeReadController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from can seals be read page to are any seals broken page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(CanSealsBeReadPage, NormalMode, answers)
              .mustBe(routes.AreAnySealsBrokenController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go are any seals broken page to unloading summary page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AreAnySealsBrokenPage, NormalMode, answers)
              .mustBe(routes.UnloadingSummaryController.onPageLoad(answers.id))
        }
      }

      "must go from anything else to report page" - {
        "to check your answers page when no is selected " in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(AnythingElseToReportPage, false).success.value

              navigator
                .nextPage(AnythingElseToReportPage, NormalMode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedUserAnswers.id))
          }
        }
        "to changes to report page when yes is selected" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(AnythingElseToReportPage, true).success.value

              navigator
                .nextPage(AnythingElseToReportPage, NormalMode, answers)
                .mustBe(routes.ChangesToReportController.onPageLoad(updatedUserAnswers.id, NormalMode))

          }
        }

      }

      "in Check mode" - {

        "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

          case object UnknownPage extends Page

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(UnknownPage, CheckMode, answers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }
      }
    }
  }
}
