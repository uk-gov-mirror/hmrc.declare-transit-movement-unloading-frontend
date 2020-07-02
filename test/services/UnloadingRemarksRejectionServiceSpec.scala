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

import base.SpecBase
import connectors.UnloadingConnector
import models.{ArrivalId, Movement, MovementMessage}
import org.mockito.Mockito.when
import services.UnloadingPermissionServiceSpec.ie043Message
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class UnloadingRemarksRejectionServiceSpec extends SpecBase {

  private val mockConnector = mock[UnloadingConnector]
  val service               = new UnloadingRemarksRejectionService(mockConnector)

  "UnloadingRemarksRejectionService" - {
    "must return UnloadingRemarksRejection when IE058 message exists" in {
      when(mockConnector.get(ArrivalId(1)))
        .thenReturn(Future.successful(Some(Movement(movementReferenceNumber = mrn, Seq(MovementMessage(messageType = "IE043A", message = ie043Message))))))
      service.arrivalRejectionMessage(ArrivalId(1)).futureValue mustBe a[Some[_]]
    }
  }
}
