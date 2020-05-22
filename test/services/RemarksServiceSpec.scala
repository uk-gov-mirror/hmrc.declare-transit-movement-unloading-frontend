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
import models.messages.{ResultsOfControl, _}
import models.{Index, Seals, UnloadingPermission, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import org.mockito.Mockito.when

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

        service.build(emptyUserAnswers, unloadingPermission) mustBe Left(FailedToFindUnloadingDate)
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

            service.build(userAnswers, unloadingPermissionWithSeals) mustBe Right(
              RemarksNonConform(stateOfSeals    = Some(1),
                                unloadingRemark = None,
                                unloadingDate   = dateGoodsUnloaded,
                                resultOfControl = Seq(resultsOfControlValues))
            )
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

            service.build(userAnswers, unloadingPermissionWithNoSeals) mustBe Right(
              RemarksNonConform(stateOfSeals = None, unloadingRemark = None, unloadingDate = dateGoodsUnloaded, resultOfControl = Seq(resultsOfControlValues))
            )
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

              service.build(userAnswers, unloadingPermissionWithNoSeals) mustBe Right(
                RemarksNonConform(stateOfSeals    = None,
                                  unloadingRemark = Some("changes reported"),
                                  unloadingDate   = dateGoodsUnloaded,
                                  resultOfControl = resultsOfControlValues)
              )
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

              service.build(userAnswers, unloadingPermissionWithSeals) mustBe Right(
                RemarksNonConform(stateOfSeals    = Some(1),
                                  unloadingRemark = Some("changes reported"),
                                  unloadingDate   = dateGoodsUnloaded,
                                  resultOfControl = resultsOfControlValues)
              )
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

          service.build(userAnswersUpdated, unloadingPermissionWithSeals) mustBe Right(RemarksConformWithSeals(dateGoodsUnloaded))
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

              service.build(userAnswersUpdated, unloadingPermissionWithSeals) mustBe Right(
                RemarksNonConform(
                  stateOfSeals    = Some(0),
                  unloadingRemark = userAnswersUpdated.get(ChangesToReportPage),
                  unloadingDate   = dateGoodsUnloaded,
                  resultOfControl = resultsOfControlValues
                )
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

              service.build(userAnswersUpdated, unloadingPermissionWithSeals) mustBe Right(
                RemarksNonConform(
                  stateOfSeals    = Some(0),
                  unloadingRemark = userAnswersUpdated.get(ChangesToReportPage),
                  unloadingDate   = dateGoodsUnloaded,
                  resultOfControl = resultsOfControlValues
                )
              )
          }
        }

        "don't exist in unloading permission and user hasn't changed anything" in {

          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)

          val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value

          when(mockResultOfControlService.build(userAnswers)).thenReturn(Nil)

          service.build(userAnswers, unloadingPermissionWithNoSeals) mustBe Right(RemarksConform(dateGoodsUnloaded))
        }

        "exist in unloading permission and user hasn't changed anything" in {

          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

          val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value

          when(mockResultOfControlService.build(userAnswers)).thenReturn(Nil)

          service.build(userAnswers, unloadingPermissionWithSeals) mustBe Right(RemarksConformWithSeals(dateGoodsUnloaded))
        }

        //TODO: See comments below, need clarification
        "don't exist in unloading permission and user has added a new seal" in {

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = None)

              val userAnswersUpdated =
                userAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value.set(NewSealNumberPage(Index(0)), "new seal").success.value

              when(mockResultOfControlService.build(userAnswersUpdated)).thenReturn(Nil)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals) mustBe
                Right(
                  RemarksNonConform(
                    stateOfSeals    = None, // Should this be Some(0)? (no seals existed but we've added new ones)
                    unloadingRemark = userAnswers.get(ChangesToReportPage),
                    unloadingDate   = dateGoodsUnloaded,
                    resultOfControl = Nil // Do we need to send a result of control when adding new seals?
                  )
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

              service.build(userAnswersUpdated, unloadingPermissionWithSeals) mustBe
                Right(
                  RemarksNonConform(
                    stateOfSeals    = Some(0),
                    unloadingRemark = userAnswers.get(ChangesToReportPage),
                    unloadingDate   = dateGoodsUnloaded,
                    resultOfControl = resultsOfControlValues
                  )
                )
          }

        }

      }
    }
  }
}
