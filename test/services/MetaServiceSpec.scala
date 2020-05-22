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
import java.time.LocalDateTime

import base.SpecBase
import generators.MessagesModelGenerators
import models.messages.{InterchangeControlReference, MessageSender, Meta}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Configuration
import play.api.inject.bind

class MetaServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  val mockDateTimeService = mock[DateTimeService]

  override lazy val app = applicationBuilder(Some(emptyUserAnswers))
    .configure(Configuration("env" -> "TEST_ENV"))
    .overrides(bind[DateTimeService].toInstance(mockDateTimeService))
    .build()

  val metaService = app.injector.instanceOf[MetaService]

  "MetaServiceSpec" - {

    "return a Meta model" in {
      forAll(stringsWithMaxLength(8: Int), arbitrary[InterchangeControlReference]) {
        (eori, interchangeControlReference) =>
          val localDateTime = LocalDateTime.now()

          when(mockDateTimeService.currentDateTime).thenReturn(localDateTime)

          metaService.build(eori, interchangeControlReference) mustBe Meta(
            MessageSender("TEST_ENV", eori),
            interchangeControlReference,
            localDateTime.toLocalDate,
            localDateTime.toLocalTime
          )
      }
    }
  }

}
