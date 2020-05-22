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
import generators.Generators
import models.UnloadingPermission
import models.messages.InterchangeControlReference
import org.scalacheck.Arbitrary.arbitrary
import play.api.http.Status._
import play.api.inject.bind
import repositories.InterchangeControlReferenceIdRepository
import org.mockito.Mockito.when

import scala.concurrent.Future

class UnloadingRemarksServiceSpec extends SpecBase with Generators {

  val mockRemarksService = mock[RemarksService]

  val mockMetaService = mock[MetaService]

  val mockInterchangeControlReferenceIdRepository = mock[InterchangeControlReferenceIdRepository]

  override lazy val app = applicationBuilder(Some(emptyUserAnswers))
    .overrides(bind[RemarksService].toInstance(mockRemarksService))
    .overrides(bind[MetaService].toInstance(mockMetaService))
    .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControlReferenceIdRepository))
    .build()

  val arrivalNotificationService = app.injector.instanceOf[UnloadingRemarksService]

  "UnloadingRemarksServiceSpec" - {

    "should return 202 for successful submission" in {


      //TODO: Need to mock MetaService.build
      when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
        .thenReturn(Future.successful(InterchangeControlReference("date", 1)))

      val unloadingPermissionObject = arbitrary[UnloadingPermission]

      val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

      arrivalNotificationService.submit("eori", emptyUserAnswers, unloadingPermission).futureValue mustBe ACCEPTED
    }

    "should return None when failed to generate InterchangeControlReference" in {
      when(mockInterchangeControlReferenceIdRepository.nextInterchangeControlReferenceId())
        .thenReturn(Future.failed(new Exception("failed to get InterchangeControlReference")))

      val unloadingPermissionObject = arbitrary[UnloadingPermission]

      val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

      arrivalNotificationService.submit("eori", emptyUserAnswers, unloadingPermission).futureValue mustBe None
    }

  }

}
