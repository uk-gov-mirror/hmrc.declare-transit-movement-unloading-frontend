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
import models.ErrorType.DuplicateMrn
import models.{ErrorPointer, FunctionalError, MessagesLocation, MessagesSummary, UnloadingRemarksRejectionMessage}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
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
      val errors              = Seq(FunctionalError(DuplicateMrn, ErrorPointer("Duplicate MRN"), None, None))
      val notificationMessage = UnloadingRemarksRejectionMessage(mrn.toString, LocalDate.now, None, errors)
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
  }

}
