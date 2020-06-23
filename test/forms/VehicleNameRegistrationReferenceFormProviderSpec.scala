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

package forms

import models.messages.UnloadingRemarksRequest
import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class VehicleNameRegistrationReferenceFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "vehicleNameRegistrationReference.error.required"
  private val invalidKey  = "vehicleNameRegistrationReference.error.characters"
  private val maxLength   = UnloadingRemarksRequest.vehicleNameMaxLength

  private val form      = new VehicleNameRegistrationReferenceFormProvider()()
  private val fieldName = "value"

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
  "must not bind strings that do not match regex" in {

    val generator: Gen[String] = RegexpGen.from("[^a-zA-Z0-9]{1,27}")
    val validRegex: String     = "^[a-zA-Z0-9]*$"
    val expectedError          = FormError(fieldName, invalidKey, Seq(validRegex))

    forAll(generator) {
      invalidString =>
        val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
        result.errors should contain(expectedError)
    }
  }

}
