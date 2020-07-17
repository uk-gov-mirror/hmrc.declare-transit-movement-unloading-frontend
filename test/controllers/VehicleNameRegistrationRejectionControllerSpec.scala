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
import forms.VehicleNameRegistrationReferenceFormProvider
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, UnloadingRemarksRejectionMessage, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.VehicleNameRegistrationReferencePage
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

class VehicleNameRegistrationRejectionControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers with MessagesModelGenerators {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new VehicleNameRegistrationReferenceFormProvider()
  val form         = formProvider()

  lazy val vehicleNameRegistrationRejectionRoute: String = routes.VehicleNameRegistrationRejectionController.onPageLoad(arrivalId).url

  "VehicleNameRegistrationRejectionController Controller" - {

    "must populate the value from the rejection service original value attribute" in {

      val mockRejectionService  = mock[UnloadingRemarksRejectionService]
      val mockSessionRepository = mock[SessionRepository]
      val originalValue         = "some reference"
      val errors                = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
      val rejectionMessage      = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any(), any())).thenReturn(Future.successful(Some(rejectionMessage)))
      when(mockSessionRepository.get(any(), any())).thenReturn(Future.successful(None))
      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      val request        = FakeRequest(GET, vehicleNameRegistrationRejectionRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])
      val result         = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> originalValue))
      val expectedJson = Json.obj(
        "form"      -> filledForm,
        "arrivalId" -> arrivalId
      )

      templateCaptor.getValue mustEqual "vehicleNameRegistrationReference.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must go to technical difficulties when there is no rejection message" in {

      val mockRejectionService = mock[UnloadingRemarksRejectionService]

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any(), any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService))
        .build()
      val request = FakeRequest(GET, vehicleNameRegistrationRejectionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request        = FakeRequest(POST, vehicleNameRegistrationRejectionRoute).withFormUrlEncodedBody(("value", ""))
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

      templateCaptor.getValue mustEqual "vehicleNameRegistrationReference.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to check your answers page for a POST" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockRejectionService  = mock[UnloadingRemarksRejectionService]

      val originalValue     = "some reference"
      val errors            = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
      val rejectionMessage  = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any(), any())).thenReturn(Future.successful(Some(rejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
        )
        .build()

      val request =
        FakeRequest(POST, vehicleNameRegistrationRejectionRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())

      userAnswersCaptor.getValue.data mustBe Json.obj("vehicleNameRegistrationReference" -> "answer")
      userAnswersCaptor.getValue.id mustBe arrivalId
      userAnswersCaptor.getValue.mrn mustBe mrn
      application.stop()
    }

    "must redirect to technical difficulties page for a POST" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockRejectionService  = mock[UnloadingRemarksRejectionService]

      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
        )
        .build()

      val request =
        FakeRequest(POST, vehicleNameRegistrationRejectionRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }

    "getRejectedValue" - {
      "must return Some(value) when there is no previously saved answers than fetch it from UnloadingRemarksRejectionMessage" in {
        val mockRejectionService  = mock[UnloadingRemarksRejectionService]
        val mockSessionRepository = mock[SessionRepository]
        val originalValue         = "some reference"
        val errors                = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
        val rejectionMessage      = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
        when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any(), any())).thenReturn(Future.successful(Some(rejectionMessage)))
        when(mockSessionRepository.get(any(), any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(
            bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        val controller = application.injector.instanceOf[VehicleNameRegistrationRejectionController]

        val result = controller.getRejectedValue(arrivalId, eoriNumber)
        result.futureValue mustBe Some("some reference")

      }
      "must return Some(value) when there is a previously saved answers" in {
        val mockSessionRepository = mock[SessionRepository]
        val originalValue         = "some reference"
        val userAnswers           = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, originalValue).get

        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
        when(mockSessionRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        val controller = application.injector.instanceOf[VehicleNameRegistrationRejectionController]

        val result = controller.getRejectedValue(arrivalId, eoriNumber)
        result.futureValue mustBe Some("some reference")
      }
      "must return None when there is no previously saved answers and UnloadingRemarksRejectionMessage returns 'None'" in {
        val mockSessionRepository = mock[SessionRepository]
        val mockRejectionService  = mock[UnloadingRemarksRejectionService]

        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
        when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockSessionRepository.get(any(), any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
          )
          .build()

        val controller = application.injector.instanceOf[VehicleNameRegistrationRejectionController]

        val result = controller.getRejectedValue(arrivalId, eoriNumber)
        result.futureValue mustBe None
      }
      "must return None when there is no previously saved answers and UnloadingRemarksRejectionMessage.originalAttributeValue is 'None'" in {
        val mockSessionRepository = mock[SessionRepository]
        val mockRejectionService  = mock[UnloadingRemarksRejectionService]
        val errors                = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, None))
        val rejectionMessage      = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
        when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any(), any())).thenReturn(Future.successful(Some(rejectionMessage)))
        when(mockSessionRepository.get(any(), any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService)
          )
          .build()

        val controller = application.injector.instanceOf[VehicleNameRegistrationRejectionController]

        val result = controller.getRejectedValue(arrivalId, eoriNumber)
        result.futureValue mustBe None
      }
    }
  }
}
