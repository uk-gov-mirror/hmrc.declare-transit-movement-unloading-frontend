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
import pages.VehicleNameRegistrationReferencePage
import play.api.inject.bind
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingRemarksService

import scala.concurrent.Future

class RejectionCheckYourAnswersControllerSpec extends SpecBase {

  "return OK and the Rejection view for a GET when unloading rejection message returns a Some" in {

    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val request                = FakeRequest(GET, routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url)
    val templateCaptor         = ArgumentCaptor.forClass(classOf[String])
    val result: Future[Result] = route(application, request).value

    status(result) mustEqual OK

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

    templateCaptor.getValue mustEqual "rejection-check-your-answers.njk"

    application.stop()
  }

  "onSubmit" - {
    "must redirect to Confirmation on valid submission" in {
      val mockUnloadingRemarksService = mock[UnloadingRemarksService]
      val userAnswers                 = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, "updatedValue").toOption

      when(mockUnloadingRemarksService.resubmit(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(ACCEPTED)))

      val application =
        applicationBuilder(userAnswers = userAnswers)
          .overrides(
            bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService)
          )
          .build()

      val request = FakeRequest(POST, routes.RejectionCheckYourAnswersController.onSubmit(arrivalId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(arrivalId).url

      application.stop()
    }

    "must redirect to Technical difficulties page on submission failure" in {
      val mockUnloadingRemarksService = mock[UnloadingRemarksService]

      when(mockUnloadingRemarksService.resubmit(any(), any(), any())(any()))
        .thenReturn(Future.successful(None))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService)
          )
          .build()

      val request = FakeRequest(POST, routes.RejectionCheckYourAnswersController.onSubmit(arrivalId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }

    "return UNAUTHORIZED when backend returns 401" in {

      val mockUnloadingRemarksService = mock[UnloadingRemarksService]
      val userAnswers                 = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, "updatedValue").toOption

      val application = applicationBuilder(userAnswers = userAnswers)
        .overrides(bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService))
        .build()

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockUnloadingRemarksService.resubmit(any(), any(), any())(any())).thenReturn(Future.successful(Some(UNAUTHORIZED)))

      val request = FakeRequest(POST, routes.RejectionCheckYourAnswersController.onSubmit(arrivalId).url)

      val result = route(application, request).value

      status(result) mustEqual UNAUTHORIZED

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR on internal failure" in {

      val mockUnloadingRemarksService = mock[UnloadingRemarksService]
      val userAnswers                 = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, "updatedValue").toOption

      val application = applicationBuilder(userAnswers = userAnswers)
        .overrides(bind[UnloadingRemarksService].toInstance(mockUnloadingRemarksService))
        .build()

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      when(mockUnloadingRemarksService.resubmit(any(), any(), any())(any())).thenReturn(Future.successful(None))

      val request = FakeRequest(POST, routes.RejectionCheckYourAnswersController.onSubmit(arrivalId).url)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

  }
}
