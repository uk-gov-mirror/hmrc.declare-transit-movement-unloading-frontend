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
import generators.MessagesModelGenerators
import models.UnloadingPermission
import models.messages._
import org.mockito.Mockito.{when, _}
import org.scalacheck.Arbitrary.arbitrary
import pages.DateGoodsUnloadedPage
import play.api.Application
import play.api.http.Status._
import play.api.inject.bind
import repositories.InterchangeControlReferenceIdRepository
import services.UnloadingRemarksRequestServiceSpec.header

import scala.concurrent.Future

class UnloadingRemarksServiceSpec extends SpecBase with MessagesModelGenerators {

  private val mockRemarksService = mock[RemarksService]

  private val mockMetaService = mock[MetaService]

  private val mockUnloadingRemarksRequestService = mock[UnloadingRemarksRequestService]

  private val mockInterchangeControlReferenceIdRepository = mock[InterchangeControlReferenceIdRepository]

  override lazy val app: Application = applicationBuilder(Some(emptyUserAnswers))
    .overrides(bind[RemarksService].toInstance(mockRemarksService))
    .overrides(bind[MetaService].toInstance(mockMetaService))
    .overrides(bind[UnloadingRemarksRequestService].toInstance(mockUnloadingRemarksRequestService))
    .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControlReferenceIdRepository))
    .build()

  private val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]

  "UnloadingRemarksServiceSpec" - {

    "should return 202 for successful submission" in {

      val localDate = LocalDate.now(ZoneOffset.UTC)

      val unloadingPermissionObject = arbitrary[UnloadingPermission]

      val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

      val metaObject = arbitrary[Meta]

      val meta: Meta = metaObject.sample.get

      val unloadingRemarks = RemarksConform(localDate)

      val userAnswersUpdated =
        emptyUserAnswers
          .set(DateGoodsUnloadedPage, localDate)
          .success
          .value

      when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
        .thenReturn(Future.successful(InterchangeControlReference("dsfsf", 1)))

      when(mockMetaService.build("eori", InterchangeControlReference("date", 1)))
        .thenReturn(meta)

      when(mockRemarksService.build(userAnswersUpdated, unloadingPermission))
        .thenReturn(Right(unloadingRemarks))

      when(mockUnloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswersUpdated))
        .thenReturn(
          UnloadingRemarksRequest(
            meta,
            header(unloadingPermission),
            unloadingPermission.traderAtDestination,
            unloadingPermission.presentationOffice,
            unloadingRemarks,
            seals = None,
            unloadingPermission.goodsItems
          )
        )

      arrivalNotificationService.submit("eori", userAnswersUpdated, unloadingPermission).futureValue mustBe ACCEPTED

      reset(mockInterchangeControlReferenceIdRepository)
      reset(mockMetaService)
      reset(mockRemarksService)
      reset(mockUnloadingRemarksRequestService)
    }

    "should return None when unloading remarks returns FailedToFindUnloadingDate" in {

      val unloadingPermissionObject = arbitrary[UnloadingPermission]

      val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

      val metaObject = arbitrary[Meta]

      val meta: Meta = metaObject.sample.get

      when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
        .thenReturn(Future.successful(InterchangeControlReference("date", 1)))

      when(mockMetaService.build("eori", InterchangeControlReference("date", 1)))
        .thenReturn(meta)

      when(mockRemarksService.build(emptyUserAnswers, unloadingPermission))
        .thenReturn(Left(FailedToFindUnloadingDate))

      arrivalNotificationService.submit("eori", emptyUserAnswers, unloadingPermission).futureValue mustBe None

      reset(mockInterchangeControlReferenceIdRepository)
      reset(mockMetaService)
      reset(mockRemarksService)
    }

    "should return None when failed to generate InterchangeControlReference" in {
      when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
        .thenReturn(Future.failed(new Exception("failed to get InterchangeControlReference")))

      val unloadingPermissionObject = arbitrary[UnloadingPermission]

      val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

      arrivalNotificationService.submit("eori", emptyUserAnswers, unloadingPermission).futureValue mustBe None

      reset(mockInterchangeControlReferenceIdRepository)
    }

  }

}
