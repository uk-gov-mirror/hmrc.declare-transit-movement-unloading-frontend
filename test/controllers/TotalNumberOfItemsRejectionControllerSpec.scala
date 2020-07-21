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

import java.time.LocalDate

import base.SpecBase
import forms.TotalNumberOfItemsFormProvider
import matchers.JsonMatchers
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.TotalNumberOfItemsPage
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class TotalNumberOfItemsRejectionControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  val formProvider = new TotalNumberOfItemsFormProvider()
  val form         = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 1

  lazy val totalNumberOfItemsRoute = routes.TotalNumberOfItemsRejectionController.onPageLoad(arrivalId).url

  "TotalNumberOfItems Controller" - {

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val mockRejectionService  = mock[UnloadingRemarksRejectionService]
      val mockSessionRepository = mock[SessionRepository]

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockRejectionService.getRejectedValueAsInt(any(), any())(any())(any())).thenReturn(Future.successful(Some(validAnswer)))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val request        = FakeRequest(GET, totalNumberOfItemsRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])
      val result         = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> validAnswer.toString))

      val expectedJson = Json.obj(
        "form"      -> filledForm,
        "arrivalId" -> arrivalId
      )

      templateCaptor.getValue mustEqual "totalNumberOfItems.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to Technical Difficulties page when get rejected value is None" in {
      val mockRejectionService = mock[UnloadingRemarksRejectionService]

      when(mockRejectionService.getRejectedValueAsInt(any(), any())(any())(any())).thenReturn(Future.successful(None))

      val userAnswers = emptyUserAnswers.set(TotalNumberOfItemsPage, validAnswer).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
        )
        .build()
      val request = FakeRequest(GET, totalNumberOfItemsRoute)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url
      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockRejectionService  = mock[UnloadingRemarksRejectionService]
      val originalValue         = "some reference"
      val errors                = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
      val rejectionMessage      = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(rejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
        )
        .build()

      val request =
        FakeRequest(POST, totalNumberOfItemsRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request        = FakeRequest(POST, totalNumberOfItemsRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm      = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"      -> boundForm,
        "arrivalId" -> arrivalId
      )

      templateCaptor.getValue mustEqual "totalNumberOfItems.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }
    "must redirect to Technical Difficulties when there is no rejection message on submission" in {

      val mockRejectionService = mock[UnloadingRemarksRejectionService]

      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
        )
        .build()

      val request =
        FakeRequest(POST, totalNumberOfItemsRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }
  }
}
