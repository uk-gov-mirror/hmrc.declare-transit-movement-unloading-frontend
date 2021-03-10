/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.{Clock, Instant, LocalDate, ZoneId, ZoneOffset}
import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.DateGoodsUnloadedFormProvider
import matchers.JsonMatchers
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, UnloadingRemarksRejectionMessage}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.viewmodels.{DateInput, NunjucksSupport}

import scala.concurrent.Future

class DateGoodsUnloadedRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider = new DateGoodsUnloadedFormProvider()
  val stubClock    = Clock.fixed(Instant.now, ZoneId.systemDefault)
  val minDate      = LocalDate.now(stubClock)

  private def form: Form[LocalDate]  = formProvider(minDate)
  private val validAnswer: LocalDate = minDate.plusDays(1)

  private lazy val dateGoodsUnloadedRoute = routes.DateGoodsUnloadedRejectionController.onPageLoad(arrivalId).url

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateGoodsUnloadedRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateGoodsUnloadedRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  private val mockRejectionService = mock[UnloadingRemarksRejectionService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UnloadingRemarksRejectionService].toInstance(mockRejectionService))
      .overrides(bind[Clock].toInstance(stubClock))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRejectionService)
  }

  "DateGoodsUnloaded Controller" - {

    "must populate the view correctly on a GET" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockRejectionService.getRejectedValueAsDate(any(), any())(any())(any())).thenReturn(Future.successful(Some(validAnswer)))

      setNoExistingUserAnswers()

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, getRequest()).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(
        Map(
          "value.day"   -> validAnswer.getDayOfMonth.toString,
          "value.month" -> validAnswer.getMonthValue.toString,
          "value.year"  -> validAnswer.getYear.toString
        )
      )

      val viewModel = DateInput.localDate(filledForm("value"))

      val expectedJson = Json.obj(
        "form"      -> filledForm,
        "arrivalId" -> arrivalId,
        "date"      -> viewModel
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      val originalValue    = "some reference"
      val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer(""), None, Some(originalValue)))
      val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)

      when(mockRejectionService.unloadingRemarksRejectionMessage(any())(any())).thenReturn(Future.successful(Some(rejectionMessage)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setNoExistingUserAnswers()

      val result = route(app, postRequest()).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.RejectionCheckYourAnswersController.onPageLoad(arrivalId).url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, dateGoodsUnloadedRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm      = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val viewModel = DateInput.localDate(boundForm("value"))

      val expectedJson = Json.obj(
        "form"      -> boundForm,
        "arrivalId" -> arrivalId,
        "date"      -> viewModel
      )

      templateCaptor.getValue mustEqual "dateGoodsUnloaded.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
