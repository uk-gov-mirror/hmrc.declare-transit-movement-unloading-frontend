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
import java.time.LocalDateTime

import base.SpecBase
import generators.MessagesModelGenerators
import models.messages._
import models.{Index, Seals, UnloadingPermission}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.NewSealNumberPage

class UnloadingRemarksRequestServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  import UnloadingRemarksRequestServiceSpec._

  val unloadingRemarksRequestService = app.injector.instanceOf[UnloadingRemarksRequestService]

  "UnloadingRemarksRequestServiceSpec" - {

    "return UnloadingRemarksRequest" - {

      "when unloading remarks conform with no seals" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksConform(localDateTime.toLocalDate)

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.trader,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                seals = None,
                unloadingPermission.goodsItems
              )
        }
      }

      "when unloading remarks conform with seals" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksConformWithSeals(localDateTime.toLocalDate)

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.trader,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                seals = None,
                unloadingPermission.goodsItems
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is None" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              None,
              Some("unloading remarks"),
              localDateTime.toLocalDate,
              Nil
            )

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.trader,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                seals = None,
                unloadingPermission.goodsItems
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is OK" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              stateOfSeals = Some(1),
              Some("unloading remarks"),
              localDateTime.toLocalDate,
              Nil
            )

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.trader,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                seals = None,
                unloadingPermission.goodsItems
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is NOT OK" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              stateOfSeals = Some(0),
              Some("unloading remarks"),
              localDateTime.toLocalDate,
              Nil
            )

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.trader,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                seals = unloadingPermission.seals,
                unloadingPermission.goodsItems
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is NOT OK and seals exist in UserAnswers" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              stateOfSeals = Some(0),
              Some("unloading remarks"),
              localDateTime.toLocalDate,
              Nil
            )

            val userAnswersUpdated =
              emptyUserAnswers
                .set(NewSealNumberPage(Index(0)), "seal 2")
                .success
                .value
                .set(NewSealNumberPage(Index(1)), "seal 1")
                .success
                .value
                .set(NewSealNumberPage(Index(2)), "seal 3")
                .success
                .value

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswersUpdated) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.trader,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                seals = Some(Seals(3, Seq("seal 2", "seal 1", "seal 3"))),
                unloadingPermission.goodsItems
              )
        }
      }
    }
  }
}

object UnloadingRemarksRequestServiceSpec {

  val header: UnloadingPermission => Header = unloadingPermission =>
    Header(
      movementReferenceNumber = unloadingPermission.movementReferenceNumber,
      transportIdentity       = unloadingPermission.transportIdentity,
      transportCountry        = unloadingPermission.transportCountry,
      numberOfItems           = unloadingPermission.numberOfItems,
      numberOfPackages        = unloadingPermission.numberOfPackages,
      grossMass               = unloadingPermission.grossMass
  )

}
