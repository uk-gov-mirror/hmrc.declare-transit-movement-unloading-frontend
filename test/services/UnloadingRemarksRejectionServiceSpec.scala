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
import models.ErrorType.IncorrectValue
import models.{DefaultPointer, FunctionalError, MessagesLocation, MessagesSummary, UnloadingRemarksRejectionMessage}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import pages.{DateGoodsUnloadedPage, TotalNumberOfItemsPage, VehicleNameRegistrationReferencePage}
import play.api.inject.bind

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingRemarksRejectionServiceSpec extends SpecBase {

  private val mockConnector = mock[UnloadingConnector]
  val service               = new UnloadingRemarksRejectionService(mockConnector)

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockConnector)
  }

  "UnloadingRemarksRejectionService" - {
    "must return UnloadingRemarksRejectionMessage for the input arrivalId" in {
      val errors              = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, None))
      val notificationMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
      when(mockConnector.getRejectionMessage(any())(any()))
        .thenReturn(Future.successful(Some(notificationMessage)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[UnloadingConnector].toInstance(mockConnector))
        .build()
      val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

      unloadingRemarksRejectionService.unloadingRemarksRejectionMessage(arrivalId).futureValue mustBe Some(notificationMessage)
    }

    "must return None when getSummary fails to get rejection message" in {
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", None))
      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[UnloadingConnector].toInstance(mockConnector))
        .build()
      val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

      unloadingRemarksRejectionService.unloadingRemarksRejectionMessage(arrivalId).futureValue mustBe None
    }

    "must return None when getSummary call fails to get MessagesSummary" in {

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[UnloadingConnector].toInstance(mockConnector))
        .build()
      val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

      unloadingRemarksRejectionService.unloadingRemarksRejectionMessage(arrivalId).futureValue mustBe None
    }

    "getRejectedValue" - {
      "must return Some(String) when there is no previously saved answers than fetch it from UnloadingRemarksRejectionMessage" in {
        val originalValue    = "some reference"
        val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
        val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockConnector.getRejectionMessage(any())(any()))
          .thenReturn(Future.successful(Some(rejectionMessage)))

        val application = applicationBuilder(None)
          .overrides(bind[UnloadingConnector].toInstance(mockConnector))
          .build()

        val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

        val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, None)(VehicleNameRegistrationReferencePage)
        result.futureValue mustBe Some("some reference")
      }

      "must return Some(String) when there is a user answers but not from the page we needed than fetch it from UnloadingRemarksRejectionMessage" in {
        val originalValue    = "some reference"
        val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
        val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockConnector.getRejectionMessage(any())(any()))
          .thenReturn(Future.successful(Some(rejectionMessage)))

        val application = applicationBuilder(None)
          .overrides(bind[UnloadingConnector].toInstance(mockConnector))
          .build()

        val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

        val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, Some(emptyUserAnswers))(VehicleNameRegistrationReferencePage)
        result.futureValue mustBe Some("some reference")
      }

      "must return Some(Int) when there is no previously saved answers than fetch it from UnloadingRemarksRejectionMessage" in {
        val originalValue    = "1000"
        val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
        val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockConnector.getRejectionMessage(any())(any()))
          .thenReturn(Future.successful(Some(rejectionMessage)))

        val application = applicationBuilder(None)
          .overrides(bind[UnloadingConnector].toInstance(mockConnector))
          .build()

        val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

        val result = unloadingRemarksRejectionService.getRejectedValueAsInt(arrivalId, None)(TotalNumberOfItemsPage)
        result.futureValue.value mustBe 1000
      }

      "must return Some(Date) when there is no previously saved answers than fetch it from UnloadingRemarksRejectionMessage" in {
        val originalValue    = "20200721"
        val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, Some(originalValue)))
        val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockConnector.getRejectionMessage(any())(any()))
          .thenReturn(Future.successful(Some(rejectionMessage)))

        val application = applicationBuilder(None)
          .overrides(bind[UnloadingConnector].toInstance(mockConnector))
          .build()

        val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

        val result = unloadingRemarksRejectionService.getRejectedValueAsDate(arrivalId, None)(DateGoodsUnloadedPage)
        result.futureValue.value mustBe LocalDate.parse("2020-07-21")
      }

      "must return Some(value) when there is a previously saved answers" in {
        val originalValue = "some reference"
        val userAnswers   = emptyUserAnswers.set(VehicleNameRegistrationReferencePage, originalValue).get

        val application = applicationBuilder(Some(userAnswers))
          .build()

        val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

        val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, Some(userAnswers))(VehicleNameRegistrationReferencePage)
        result.futureValue mustBe Some("some reference")
      }

      "must return None when there is no previously saved answers and UnloadingRemarksRejectionMessage returns 'None'" in {
        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockConnector.getRejectionMessage(any())(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[UnloadingConnector].toInstance(mockConnector))
          .build()

        val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

        val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, None)(VehicleNameRegistrationReferencePage)
        result.futureValue mustBe None
      }

      "must return None when there is no previously saved answers and UnloadingRemarksRejectionMessage.originalAttributeValue is 'None'" in {
        val errors           = Seq(FunctionalError(IncorrectValue, DefaultPointer, None, None))
        val rejectionMessage = UnloadingRemarksRejectionMessage(mrn, LocalDate.now, None, errors)
        val messagesSummary =
          MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

        when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockConnector.getRejectionMessage(any())(any()))
          .thenReturn(Future.successful(Some(rejectionMessage)))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[UnloadingConnector].toInstance(mockConnector))
          .build()

        val unloadingRemarksRejectionService = application.injector.instanceOf[UnloadingRemarksRejectionService]

        val result = unloadingRemarksRejectionService.getRejectedValueAsString(arrivalId, None)(VehicleNameRegistrationReferencePage)
        result.futureValue mustBe None
      }
    }
  }

}
