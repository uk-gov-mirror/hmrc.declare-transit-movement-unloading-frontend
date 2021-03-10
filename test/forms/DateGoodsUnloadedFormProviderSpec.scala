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

package forms

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours
import play.api.data.FormError

class DateGoodsUnloadedFormProviderSpec extends DateBehaviours {

  val minDate         = LocalDate.of(2020, 12, 31)
  val minDateAsString = "31 December 2020"
  val form            = new DateGoodsUnloadedFormProvider()(minDate)

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.now(ZoneOffset.UTC),
      max = LocalDate.now(ZoneOffset.UTC).plusYears(1)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "dateGoodsUnloaded.error.required.all")

    behave like dateFieldWithMin(form, "value", min = minDate, FormError("value", "dateGoodsUnloaded.error.min.date", Seq(minDateAsString)))

  }
}
