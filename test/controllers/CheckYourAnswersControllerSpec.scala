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
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.http.Status.ACCEPTED
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{UnloadingPermissionService, UnloadingRemarksService}

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  "Check Your Answers Controller" - {

    "onPageLoad must" - {

      "return OK and the correct view for a GET" in {

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(mrn).url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        templateCaptor.getValue mustEqual "check-your-answers.njk"

        application.stop()
      }

      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(mrn).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "return BAD REQUEST when unloading permission does not exist" in {

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(mrn).url)

        val result = route(application, request).value

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])

        status(result) mustEqual BAD_REQUEST

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

        templateCaptor.getValue mustEqual "badRequest.njk"

        application.stop()
      }

    }

    "onSubmit must" - {

      "redirect to Confirmation on valid submission" in {

        val mockUnloadingRemarksService = mock[UnloadingRemarksService]

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService))
          .build()

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockUnloadingRemarksService.submit(any(), any(), any(), any())(any())).thenReturn(Future.successful(Some(ACCEPTED)))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(mrn).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(mrn).url

        application.stop()
      }

      "redirect to Technical Difficulties on failed submission (invalid response code)" in {

        val mockUnloadingRemarksService = mock[UnloadingRemarksService]

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService))
          .build()

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockUnloadingRemarksService.submit(any(), any(), any(), any())(any())).thenReturn(Future.successful(Some(BAD_REQUEST)))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(mrn).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad.url

        application.stop()
      }

      "return UNAUTHORIZED when backend returns 401" in {

        val mockUnloadingRemarksService = mock[UnloadingRemarksService]

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService))
          .build()

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        when(mockUnloadingRemarksService.submit(any(), any(), any(), any())(any())).thenReturn(Future.successful(Some(UNAUTHORIZED)))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(mrn).url)

        val result = route(application, request).value

        status(result) mustEqual UNAUTHORIZED

        application.stop()
      }

      "return INTERNAL_SERVER_ERROR on internal failure" in {

        val mockUnloadingRemarksService = mock[UnloadingRemarksService]

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService))
          .build()

        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(Some(unloadingPermission)))

        when(mockUnloadingRemarksService.submit(any(), any(), any(), any())(any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(mrn).url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

      "return INTERNAL_SERVER_ERROR when UnloadingPermission can't be retrieved" in {

        val mockUnloadingRemarksService = mock[UnloadingRemarksService]

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService))
          .build()

        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        when(mockUnloadingPermissionService.getUnloadingPermission(any())(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(mrn).url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }
    }

  }
}
