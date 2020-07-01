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

package controllers

import base.SpecBase
import cats.data.NonEmptyList
import models.{EoriNumber, UnloadingPermission, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UnloadingPermissionServiceImpl

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  private val onwardRoute: String = routes.UnloadingGuidanceController.onPageLoad(arrivalId).url

  "Index Controller" - {
    "must redirect to onward route for a GET when there are no UserAnswers" in {
      val mockUnloadingPermissionServiceImpl = mock[UnloadingPermissionServiceImpl]
      when(mockUnloadingPermissionServiceImpl.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(unloadingPermission)))

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[UnloadingPermissionServiceImpl].toInstance(mockUnloadingPermissionServiceImpl),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      val request                = FakeRequest(GET, routes.IndexController.onPageLoad(arrivalId).url)
      val result: Future[Result] = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(onwardRoute)

      verify(mockSessionRepository).set(userAnswersCaptor.capture())

      userAnswersCaptor.getValue.mrn.toString mustBe unloadingPermission.movementReferenceNumber
      userAnswersCaptor.getValue.id mustBe arrivalId
      userAnswersCaptor.getValue.eoriNumber mustBe Some(EoriNumber("id"))

      application.stop()
    }

    "must redirect to onward route for a when there are UserAnswers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(arrivalId).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(onwardRoute)

      application.stop()
    }

    "must redirect to session expired when no response for arrivalId" in {
      val mockUnloadingPermissionServiceImpl = mock[UnloadingPermissionServiceImpl]
      when(mockUnloadingPermissionServiceImpl.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[UnloadingPermissionServiceImpl].toInstance(mockUnloadingPermissionServiceImpl)
        )
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(arrivalId).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.SessionExpiredController.onPageLoad().url)

      application.stop()
    }

    "must redirect to session expired when bad mrn received" in {
      val badUnloadingPermission = UnloadingPermission(
        movementReferenceNumber = "",
        transportIdentity       = None,
        transportCountry        = None,
        grossMass               = "1000",
        numberOfItems           = 1,
        numberOfPackages        = 1,
        traderAtDestination     = traderWithoutEori,
        presentationOffice      = "GB000060",
        seals                   = None,
        goodsItems              = NonEmptyList(goodsItemMandatory, Nil)
      )

      val mockUnloadingPermissionServiceImpl = mock[UnloadingPermissionServiceImpl]
      when(mockUnloadingPermissionServiceImpl.getUnloadingPermission(any())(any(), any()))
        .thenReturn(Future.successful(Some(badUnloadingPermission)))

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[UnloadingPermissionServiceImpl].toInstance(mockUnloadingPermissionServiceImpl),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(arrivalId).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.SessionExpiredController.onPageLoad().url)

      application.stop()
    }
  }
}
