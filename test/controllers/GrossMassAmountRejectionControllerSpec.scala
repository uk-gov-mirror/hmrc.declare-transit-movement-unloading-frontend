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
import forms.GrossMassAmountFormProvider
import matchers.JsonMatchers
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
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

class GrossMassAmountRejectionControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new GrossMassAmountFormProvider()
  val form         = formProvider()

  lazy val grossMassAmountRejectionRoute = routes.GrossMassAmountRejectionController.onPageLoad(arrivalId).url

  "GrossMassAmount Controller" - {

    "must populate the view correctly on a GET" in {

      val mockRejectionService = mock[UnloadingRemarksRejectionService]
      val originalValue        = "100000.123"
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockRejectionService.getRejectedValueAsString(any(), any())(any())(any())).thenReturn(Future.successful(Some(originalValue)))
      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
        )
        .build()
      val request        = FakeRequest(GET, grossMassAmountRejectionRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"      -> form.fill(originalValue),
        "arrivalId" -> arrivalId
      )

      templateCaptor.getValue mustEqual "grossMassAmount.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must go to technical difficulties when there is no rejection message" in {

      val mockRejectionService = mock[UnloadingRemarksRejectionService]

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.getRejectedValueAsString(any(), any())(any())(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService))
        .build()
      val request = FakeRequest(GET, grossMassAmountRejectionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }

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
      FakeRequest(POST, grossMassAmountRejectionRoute)
        .withFormUrlEncodedBody(("value", "123456.123"))

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url

    application.stop()
  }

  "must redirect to the technical difficulties page when rejection message is None" in {

    val mockSessionRepository = mock[SessionRepository]
    val mockRejectionService  = mock[UnloadingRemarksRejectionService]
    val originalValue         = "some reference"
    val errors                = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
    val rejectionMessage      = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

    when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(None))
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

    val application = applicationBuilder(userAnswers = None)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
      )
      .build()

    val request =
      FakeRequest(POST, grossMassAmountRejectionRoute)
        .withFormUrlEncodedBody(("value", "123456.123"))

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

    application.stop()
  }

  "must return a Bad Request and errors when invalid data is submitted" in {

    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))

    val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    val request        = FakeRequest(POST, grossMassAmountRejectionRoute).withFormUrlEncodedBody(("value", ""))
    val boundForm      = form.bind(Map("value" -> ""))
    val templateCaptor = ArgumentCaptor.forClass(classOf[String])
    val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

    val result = route(application, request).value

    status(result) mustEqual BAD_REQUEST

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

    val expectedJson = Json.obj(
      "form"      -> boundForm,
      "arrivalId" -> arrivalId
    )

    templateCaptor.getValue mustEqual "grossMassAmount.njk"
    jsonCaptor.getValue must containJson(expectedJson)

    application.stop()
  }

}
