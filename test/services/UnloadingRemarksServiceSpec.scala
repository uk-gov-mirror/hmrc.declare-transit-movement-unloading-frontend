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
import java.time.LocalDate

import base.SpecBase
import connectors.UnloadingConnector
import generators.MessagesModelGenerators
import models.messages.{InterchangeControlReference, _}
import models.{EoriNumber, UnloadingPermission}
import org.mockito.Matchers.any
import org.mockito.Mockito.{when, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.DateGoodsUnloadedPage
import play.api.Application
import play.api.http.Status._
import play.api.inject.bind
import repositories.InterchangeControlReferenceIdRepository
import services.UnloadingRemarksRequestServiceSpec.header
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class UnloadingRemarksServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  private val mockRemarksService                                                 = mock[RemarksService]
  private val mockMetaService                                                    = mock[MetaService]
  private val mockUnloadingRemarksRequestService                                 = mock[UnloadingRemarksRequestService]
  private val mockInterchangeControlReferenceIdRepository                        = mock[InterchangeControlReferenceIdRepository]
  private val mockUnloadingConnector: UnloadingConnector                         = mock[UnloadingConnector]
  private val mockUnloadingRemarksMessageService: UnloadingRemarksMessageService = mock[UnloadingRemarksMessageService]

  override lazy val app: Application = applicationBuilder(Some(emptyUserAnswers))
    .overrides(bind[RemarksService].toInstance(mockRemarksService))
    .overrides(bind[MetaService].toInstance(mockMetaService))
    .overrides(bind[UnloadingRemarksRequestService].toInstance(mockUnloadingRemarksRequestService))
    .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControlReferenceIdRepository))
    .overrides(bind[UnloadingConnector].toInstance(mockUnloadingConnector))
    .overrides(bind[UnloadingRemarksMessageService].toInstance(mockUnloadingRemarksMessageService))
    .build()

  private val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]

  "UnloadingRemarksServiceSpec" - {

    "Unloading Remarks Submission" - {

      "should return 202 for successful submission" in {

        forAll(
          arbitrary[EoriNumber],
          arbitrary[UnloadingPermission],
          arbitrary[Meta],
          arbitrary[RemarksConform],
          arbitrary[InterchangeControlReference],
          arbitrary[LocalDate]
        ) {
          (eori, unloadingPermission, meta, unloadingRemarks, interchangeControlReference, localDate) =>
            {
              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, localDate)
                  .success
                  .value

              when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
                .thenReturn(Future.successful(interchangeControlReference))

              when(mockMetaService.build(eori, interchangeControlReference))
                .thenReturn(meta)

              when(mockRemarksService.build(userAnswersUpdated, unloadingPermission))
                .thenReturn(Future.successful(unloadingRemarks))

              val unloadingRemarksRequest = UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Nil,
                seals = None,
                unloadingPermission.goodsItems
              )

              when(mockUnloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswersUpdated))
                .thenReturn(
                  unloadingRemarksRequest
                )

              when(mockUnloadingConnector.post(any(), any())(any())).thenReturn(Future.successful(HttpResponse(ACCEPTED)))

              arrivalNotificationService.submit(arrivalId, eori, userAnswersUpdated, unloadingPermission).futureValue mustBe Some(ACCEPTED)

              reset(mockInterchangeControlReferenceIdRepository)
              reset(mockMetaService)
              reset(mockRemarksService)
              reset(mockUnloadingRemarksRequestService)
              reset(mockUnloadingConnector)
            }
        }
      }

      //TODO: Do we need to be more specific for different connector failures?
      "should return 503 when connector fails" in {

        forAll(
          arbitrary[EoriNumber],
          arbitrary[UnloadingPermission],
          arbitrary[Meta],
          arbitrary[RemarksConform],
          arbitrary[InterchangeControlReference],
          arbitrary[LocalDate]
        ) {
          (eori, unloadingPermission, meta, unloadingRemarks, interchangeControlReference, localDate) =>
            {
              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, localDate)
                  .success
                  .value

              when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
                .thenReturn(Future.successful(interchangeControlReference))

              when(mockMetaService.build(eori, interchangeControlReference))
                .thenReturn(meta)

              when(mockRemarksService.build(userAnswersUpdated, unloadingPermission))
                .thenReturn(Future.successful(unloadingRemarks))

              val unloadingRemarksRequest = UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Nil,
                seals = None,
                unloadingPermission.goodsItems
              )

              when(mockUnloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswersUpdated))
                .thenReturn(
                  unloadingRemarksRequest
                )

              when(mockUnloadingConnector.post(any(), any())(any())).thenReturn(Future.failed(new Throwable))

              arrivalNotificationService.submit(arrivalId, eori, userAnswersUpdated, unloadingPermission).futureValue mustBe Some(SERVICE_UNAVAILABLE)

              reset(mockInterchangeControlReferenceIdRepository)
              reset(mockMetaService)
              reset(mockRemarksService)
              reset(mockUnloadingRemarksRequestService)
              reset(mockUnloadingConnector)
            }
        }
      }

      "should return None when unloading remarks returns FailedToFindUnloadingDate" in {

        forAll(arbitrary[EoriNumber], arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[InterchangeControlReference]) {
          (eori, unloadingPermission, meta, interchangeControlReference) =>
            when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
              .thenReturn(Future.successful(interchangeControlReference))

            when(mockMetaService.build(eori, interchangeControlReference))
              .thenReturn(meta)

            when(mockRemarksService.build(emptyUserAnswers, unloadingPermission))
              .thenReturn(Future.failed(new Throwable))

            arrivalNotificationService.submit(arrivalId, eori, emptyUserAnswers, unloadingPermission).futureValue mustBe None

            reset(mockInterchangeControlReferenceIdRepository)
            reset(mockMetaService)
            reset(mockRemarksService)

        }
      }

      "should return None when failed to generate InterchangeControlReference" in {

        forAll(arbitrary[EoriNumber], arbitrary[UnloadingPermission]) {
          (eori, unloadingPermission) =>
            when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
              .thenReturn(Future.failed(new Exception("failed to get InterchangeControlReference")))

            arrivalNotificationService.submit(arrivalId, eori, emptyUserAnswers, unloadingPermission).futureValue mustBe None

            reset(mockInterchangeControlReferenceIdRepository)
        }
      }
    }

    "Unloading Remarks Re-submission" - {

      "should return 202 for successful submission" in {
        val inputXml = <xml>sample</xml>
        when(mockUnloadingRemarksMessageService.unloadingRemarksMessage(any())(any(), any())) thenReturn Future.successful(Some(inputXml))
        when(mockUnloadingConnector.post(any(), any())(any())) thenReturn Future.successful(HttpResponse(ACCEPTED))

        val result = arrivalNotificationService.resubmit(arrivalId, "updatedValue")
        result.futureValue.value mustBe ACCEPTED

        reset(mockUnloadingRemarksMessageService)
        reset(mockUnloadingConnector)
      }

      "should return None if service return unloading remarks value as None" in {
        when(mockUnloadingRemarksMessageService.unloadingRemarksMessage(any())(any(), any())) thenReturn Future.successful(None)

        val result = arrivalNotificationService.resubmit(arrivalId, "updatedValue")
        result.futureValue mustBe None

        reset(mockUnloadingRemarksMessageService)
      }
    }
  }

}
