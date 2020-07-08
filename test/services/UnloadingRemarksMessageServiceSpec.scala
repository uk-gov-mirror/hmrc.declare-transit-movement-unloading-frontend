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
import models.{ErrorPointer, FunctionalError, MessagesLocation, MessagesSummary, UnloadingRemarksRejectionMessage}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingRemarksMessageServiceSpec extends SpecBase {

  private val mockConnector = mock[UnloadingConnector]
  val service               = new UnloadingRemarksMessageService(mockConnector)

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockConnector)
  }

  "UnloadingRemarksMessageService" - {
    "must return UnloadingRemarksMessage for the input arrivalId" in {
      val sampleXml = <xml>test</xml>
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
      when(mockConnector.getUnloadingRemarksMessage(any())(any()))
        .thenReturn(Future.successful(Some(sampleXml)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[UnloadingConnector].toInstance(mockConnector))
        .build()
      val unloadingRemarksMessageService = application.injector.instanceOf[UnloadingRemarksMessageService]

      unloadingRemarksMessageService.unloadingRemarksMessage(arrivalId).futureValue mustBe Some(sampleXml)
    }

    "must return None when getSummary call fails to get MessagesSummary" in {

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[UnloadingConnector].toInstance(mockConnector))
        .build()
      val unloadingRemarksMessage = application.injector.instanceOf[UnloadingRemarksMessageService]

      unloadingRemarksMessage.unloadingRemarksMessage(arrivalId).futureValue mustBe None
    }
  }

}
