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
import java.time.{LocalDate, ZoneOffset}

import base.SpecBase
import generators.Generators
import models.messages._
import models.{Index, Seals, UnloadingPermission, UserAnswers}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class RemarksServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateGoodsUnloaded = LocalDate.now(ZoneOffset.UTC)

  private val mockResultOfControlService: ResultOfControlService = mock[ResultOfControlService]

  private val service = new RemarksServiceImpl(mockResultOfControlService)

  //TODO: What do we set in Remarks when (a) seals are broken and (b) seals can't be read

  "RemarksServiceSpec" - {

    "must handle" - {

      "when unloading date doesn't exist" in {

        when(mockResultOfControlService.build(emptyUserAnswers)).thenReturn(Nil)

        val unloadingPermissionObject = arbitrary[UnloadingPermission]

        val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

        val message = intercept[RuntimeException] {
          service.build(emptyUserAnswers, unloadingPermission).futureValue
        }

        message.getCause.getMessage mustBe "date goods unloaded not found"
      }

      "results of control exist with seals" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[ResultsOfControl]) {
          (unloadingPermission, resultsOfControlValues) =>
            val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

            val userAnswers = emptyUserAnswers
              .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
              .success
              .value

            when(mockResultOfControlService.build(userAnswers)).thenReturn(Seq(resultsOfControlValues))

            service.build(userAnswers, unloadingPermissionWithSeals).futureValue mustBe
              RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
        }
      }

      "results of control exist without seals" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[ResultsOfControl]) {
          (unloadingPermission, resultsOfControlValues) =>
            val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)
            val userAnswers = emptyUserAnswers
              .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
              .success
              .value

            when(mockResultOfControlService.build(userAnswers)).thenReturn(Seq(resultsOfControlValues))

            service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
              RemarksNonConform(stateOfSeals = None, unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

        }
      }

      "when unloading remarks exist" - {

        "without seals" in {

          forAll(arbitrary[UnloadingPermission], listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)) {
            (unloadingPermission, resultsOfControlValues) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)

              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(ChangesToReportPage, "changes reported")
                .success
                .value

              when(mockResultOfControlService.build(userAnswers)).thenReturn(resultsOfControlValues)

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksNonConform(stateOfSeals = None, unloadingRemark = Some("changes reported"), unloadingDate = dateGoodsUnloaded)

          }

        }

        "with seals" in {

          forAll(arbitrary[UnloadingPermission], listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)) {
            (unloadingPermission, resultsOfControlValues) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(ChangesToReportPage, "changes reported")
                .success
                .value

              when(mockResultOfControlService.build(userAnswers)).thenReturn(resultsOfControlValues)

              service.build(userAnswers, unloadingPermissionWithSeals).futureValue mustBe
                RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = Some("changes reported"), unloadingDate = dateGoodsUnloaded)

          }
        }

      }

      "when seals" - {

        "have same values in unloading permission and user answers" in {

          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2", "seal 3"))))

          val userAnswersUpdated =
            emptyUserAnswers
              .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
              .success
              .value
              .set(NewSealNumberPage(Index(0)), "seal 2")
              .success
              .value
              .set(NewSealNumberPage(Index(1)), "seal 1")
              .success
              .value
              .set(NewSealNumberPage(Index(2)), "seal 3")
              .success
              .value

          when(mockResultOfControlService.build(userAnswersUpdated)).thenReturn(Nil)

          service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
            RemarksConformWithSeals(dateGoodsUnloaded)
        }

        "cannot be read" in {
          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission], listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)) {
            (userAnswers, unloadingPermission, resultsOfControlValues) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

              val userAnswersUpdated = userAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(CanSealsBeReadPage, false)
                .success
                .value
                .set(AreAnySealsBrokenPage, false)
                .success
                .value

              when(mockResultOfControlService.build(userAnswersUpdated)).thenReturn(resultsOfControlValues)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
                RemarksNonConform(
                  stateOfSeals    = Some(0),
                  unloadingRemark = userAnswersUpdated.get(ChangesToReportPage),
                  unloadingDate   = dateGoodsUnloaded
                )
          }
        }

        "are broken" in {
          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission], listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)) {
            (userAnswers, unloadingPermission, resultsOfControlValues) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

              val userAnswersUpdated = userAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(CanSealsBeReadPage, true)
                .success
                .value
                .set(AreAnySealsBrokenPage, true)
                .success
                .value

              when(mockResultOfControlService.build(userAnswersUpdated)).thenReturn(resultsOfControlValues)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
                RemarksNonConform(
                  stateOfSeals    = Some(0),
                  unloadingRemark = userAnswersUpdated.get(ChangesToReportPage),
                  unloadingDate   = dateGoodsUnloaded
                )
          }
        }

        "don't exist in unloading permission and user hasn't changed anything" in {

          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)

          val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value

          when(mockResultOfControlService.build(userAnswers)).thenReturn(Nil)

          service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe RemarksConform(dateGoodsUnloaded)
        }

        "exist in unloading permission and user hasn't changed anything" in {

          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

          val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value

          when(mockResultOfControlService.build(userAnswers)).thenReturn(Nil)

          service.build(userAnswers, unloadingPermissionWithSeals).futureValue mustBe RemarksConformWithSeals(dateGoodsUnloaded)
        }

        //TODO: See comments below, need clarification
        "don't exist in unloading permission and user has added a new seal" in {

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = None)

              val userAnswersUpdated =
                userAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value.set(NewSealNumberPage(Index(0)), "new seal").success.value

              when(mockResultOfControlService.build(userAnswersUpdated)).thenReturn(Nil)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
                RemarksNonConform(
                  stateOfSeals    = None, // Should this be Some(0)? (no seals existed but we've added new ones)
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate   = dateGoodsUnloaded
                )
          }

        }

        "have been updated" in {

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission], listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)) {
            (userAnswers, unloadingPermission, resultsOfControlValues) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2", "seal 3"))))

              val userAnswersUpdated =
                userAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(NewSealNumberPage(Index(0)), "seal 1")
                  .success
                  .value
                  .set(NewSealNumberPage(Index(1)), "updated seal")
                  .success
                  .value
                  .set(NewSealNumberPage(Index(2)), "seal 3")
                  .success
                  .value

              when(mockResultOfControlService.build(userAnswersUpdated)).thenReturn(resultsOfControlValues)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
                RemarksNonConform(
                  stateOfSeals    = Some(0),
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate   = dateGoodsUnloaded
                )
          }

        }

      }
    }
  }
}
