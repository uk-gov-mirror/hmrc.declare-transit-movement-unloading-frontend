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
import org.scalacheck.Arbitrary.arbitrary
import play.api.http.Status._

class UnloadingRemarksServiceSpec extends SpecBase with Generators {

  val mockRemarksService = mock[RemarksService]

  "UnloadingRemarksServiceSpec" - {

    "should return 202 for successful submission" in {

      val service = new UnloadingRemarksService(mockRemarksService)

      val unloadingPermissionObject = arbitrary[UnloadingPermission]

      val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

      service.submit(emptyUserAnswers, unloadingPermission).futureValue mustBe ACCEPTED
    }

    "should return None is submission failed" ignore {}

  }

}
