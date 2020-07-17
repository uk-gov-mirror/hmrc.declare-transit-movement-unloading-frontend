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

package models

import com.lucidchart.open.xtract.{__, XmlReader}

sealed abstract class ErrorPointer() extends Serializable {
  val value = ""
}

object ErrorPointer {

  implicit val xmlReader: XmlReader[ErrorPointer] =
    __.read[String].map {
      case GrossMassPointer.value           => GrossMassPointer
      case NumberOfItemsPointer.value       => NumberOfItemsPointer
      case UnloadingDatePointer.value       => UnloadingDatePointer
      case VehicleRegistrationPointer.value => VehicleRegistrationPointer
      case NumberOfPackagesPointer.value    => NumberOfPackagesPointer
      case _                                => DefaultPointer
    }

  val values = Seq(GrossMassPointer, NumberOfItemsPointer, UnloadingDatePointer, VehicleRegistrationPointer, NumberOfPackagesPointer, DefaultPointer)
}

object GrossMassPointer extends ErrorPointer {
  override val value = "HEA.Total gross mass"
}

object NumberOfItemsPointer extends ErrorPointer {
  override val value = "HEA.Total number of items"
}

object UnloadingDatePointer extends ErrorPointer {
  override val value = "REM.Unloading Date"
}

object VehicleRegistrationPointer extends ErrorPointer {
  override val value = "HEA.Identity of means of transport at departure (exp/trans)"
}

object NumberOfPackagesPointer extends ErrorPointer {
  override val value = "HEA.Total number of packages"
}

object DefaultPointer extends ErrorPointer
