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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ChangesToReportPage, DateGoodsUnloadedPage, NewSealNumberPage}

class RemarksServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateGoodsUnloaded = LocalDate.now(ZoneOffset.UTC)

  private val service = new RemarksServiceImpl

  //TODO: What do we set in Remarks when (a) seals are broken and (b) seals can't be read

  "RemarksServiceSpec" - {
    /*
      (still to do) SHOULD
      - handle unloading remarks
      - handle values being changed
     */

    "must handle" - {

      "when unloading date doesn't exist" in {

        val unloadingPermissionObject = arbitrary[UnloadingPermission]

        val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

        service.build(emptyUserAnswers, unloadingPermission) mustBe Left(FailedToFindUnloadingDate)
      }

      "when unloading remarks exist" - {

        "without seals" in {
          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)

          val userAnswers = emptyUserAnswers
            .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
            .success
            .value
            .set(ChangesToReportPage, "changes reported")
            .success
            .value

          service.build(userAnswers, unloadingPermissionWithNoSeals) mustBe Right(
            RemarksNonConform(stateOfSeals = None, unloadingRemark = Some("changes reported"), unloadingDate = dateGoodsUnloaded, resultOfControl = Nil)
          )
        }

        "with seals" in {
          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

          val userAnswers = emptyUserAnswers
            .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
            .success
            .value
            .set(ChangesToReportPage, "changes reported")
            .success
            .value

          service.build(userAnswers, unloadingPermissionWithSeals) mustBe Right(
            RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = Some("changes reported"), unloadingDate = dateGoodsUnloaded, resultOfControl = Nil)
          )
        }

      }

      "when seals" - {

        "exist and user hasn't changed anything" in {

          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)

          val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value

          service.build(userAnswers, unloadingPermissionWithNoSeals) mustBe Right(RemarksConform(dateGoodsUnloaded))
        }

        "exist in unloading permission and user hasn't changed anything" in {

          val unloadingPermissionObject = arbitrary[UnloadingPermission]

          val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

          val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

          val userAnswers = emptyUserAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value

          service.build(userAnswers, unloadingPermissionWithSeals) mustBe Right(RemarksConformWithSeals(dateGoodsUnloaded))
        }

        //TODO: See comments below, need clarification
        "don't exist in unloading permission and user has added a new seal" in {

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = None)

              val userAnswersUpdated =
                userAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value.set(NewSealNumberPage(Index(0)), "new seal").success.value

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

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
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

              service.build(userAnswersUpdated, unloadingPermissionWithSeals) mustBe
                Right(
                  RemarksNonConform(
                    stateOfSeals    = Some(0),
                    unloadingRemark = userAnswers.get(ChangesToReportPage),
                    unloadingDate   = dateGoodsUnloaded,
                    resultOfControl = Nil
                  )
                )
          }
        }
      }
    }
  }
}
