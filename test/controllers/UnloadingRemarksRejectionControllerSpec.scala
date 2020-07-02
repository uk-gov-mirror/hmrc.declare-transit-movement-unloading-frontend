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
import matchers.JsonMatchers
import models.ErrorType.{DuplicateMrn, InvalidMrn, UnknownMrn}
import models.{ErrorPointer, FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingRemarksRejectionService

import scala.concurrent.Future

class UnloadingRemarksRejectionControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with BeforeAndAfterEach {

  private val mockUnloadingRemarksRejectionService = mock[UnloadingRemarksRejectionService]

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUnloadingRemarksRejectionService)
  }

  "UnloadingRemarksRejection Controller" - {

    Seq(
      (UnknownMrn, "Unknown MRN", "movementReferenceNumberRejection.error.unknown"),
      (DuplicateMrn, "duplicate MRN", "movementReferenceNumberRejection.error.duplicate"),
      (InvalidMrn, "Invalid MRN", "movementReferenceNumberRejection.error.invalid")
    ) foreach {
      case (errorType, errorPointer, _) =>
        s"return OK and the correct $errorPointer Rejection view for a GET when unloading rejection message returns a Some " in {

          when(mockRenderer.render(any(), any())(any()))
            .thenReturn(Future.successful(Html("")))

          val errors = Seq(FunctionalError(errorType, ErrorPointer(errorPointer), None, None))

          when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any(), any()))
            .thenReturn(Future.successful(Some(UnloadingRemarksRejectionMessage(mrn.toString, LocalDate.now, None, None, errors))))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[UnloadingRemarksRejectionService].toInstance(mockUnloadingRemarksRejectionService)
            )
            .build()

          val request        = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)
          val templateCaptor = ArgumentCaptor.forClass(classOf[String])
          val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

          val expectedJson = Json.obj("mrn" -> mrn)

          templateCaptor.getValue mustEqual "unloadingRemarksRejection.njk"
          jsonCaptor.getValue must containJson(expectedJson)

          application.stop()
        }
    }

    "redirect to 'Technical difficulties' page when unloading rejection message returns a None" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockUnloadingRemarksRejectionService.unloadingRemarksRejectionMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UnloadingRemarksRejectionService].toInstance(mockUnloadingRemarksRejectionService)
        )
        .build()

      val request = FakeRequest(GET, routes.UnloadingRemarksRejectionController.onPageLoad(arrivalId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      verify(mockUnloadingRemarksRejectionService, times(1)).unloadingRemarksRejectionMessage(any())(any(), any())

      application.stop()
    }
  }
}
