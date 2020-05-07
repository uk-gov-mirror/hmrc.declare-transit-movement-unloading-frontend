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

package models.messages

import cats.data.NonEmptyList
import models.{GoodsItem, Seals, TraderAtDestination}

case class UnloadingRemarks(movementReferenceNumber: String,
                            transportIdentity: Option[String],
                            transportCountry: Option[String],
                            numberOfItems: Int,
                            numberOfPackages: Int,
                            grossMass: String, //TODO: Does this need to be BigDecimal
                            traderAtDestination: TraderAtDestination,
                            presentationOffice: String,
                            seals: Option[Seals], // If stateOfSeals is 1 or None, this is optional (otherwise mandatory)
                            goodsItems: NonEmptyList[GoodsItem],
                            unloadingRemark: Remarks //TODO: Should we set this here
)

object UnloadingRemarks {

  val transportIdentityLength  = 27
  val transportIdentityCountry = 2
  val numberOfItemsLength      = 5
  val numberOfPackagesLength   = 7
  val presentationOfficeLength = 8
}

//TODO: Can have up to 9
//IF UNLOADING RESULT, "Conform" = "YES" (NO ResultsOfControl)
//THEN All data groups and attributes marked with "Cond 210" can not be used
// (don't use ResultsOfControl) ELSE All data groups and attributes marked with "Cond 210" = "R" when relevant.
//TODO: What do we put for description
case class ResultsOfControl(
  description: Option[String], // If errors are found at header level (control indicator is set to OT), this item is required
  controlIndicator: ControlIndicator,
  pointerToAttribute: Option[PointerToAttribute], // See PointerToAttribute for info on this
  correctedValue: Option[String] // an27
)

object ResultsOfControl {
  val descriptionLength    = 140
  val correctedValueLength = 27
}

sealed trait ControlIndicator {
  val code: String
}

object DifferentValuesFound extends ControlIndicator {
  val code = "DI"
}

object OtherThingsToReport extends ControlIndicator {
  val code = "OT"
}

//TODO: Question - when setting results of control, the ControlIndicator can only be set to DI or OT.
// WHat happens if seals are changed and a user reports something? What value do you send?
// Don't send a new value, just include seals as is and flag stateOfSeals to 0
//If errors are found at the HEADER level, then RoC-Control Indicator is set to: - DI (DIfferent values found) or
//- OT (any OTher things to report)
