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
import models.{LanguageCodeEnglish, XMLWrites}

//TODO: Can have up to 9
//IF UNLOADING RESULT, "Conform" = "YES" (NO ResultsOfControl)
//THEN All data groups and attributes marked with "Cond 210" can not be used
// (don't use ResultsOfControl) ELSE All data groups and attributes marked with "Cond 210" = "R" when relevant.
//TODO: What do we put for description
//case class ResultsOfControl(
//  description: Option[String], // If errors are found at header level (control indicator is set to OT), this item is required
//  controlIndicator: ControlIndicator,
//  pointerToAttribute: Option[PointerToAttribute], // See PointerToAttribute for info on this
//  correctedValue: Option[String] // an27
//)

sealed trait ResultsOfControl {
  val controlIndicator: ControlIndicator
}

case class ResultsOfControlOther(description: String) extends ResultsOfControl {
  val controlIndicator = ControlIndicator(OtherThingsToReport)
}

object ResultsOfControlOther {
  implicit val writes: XMLWrites[ResultsOfControlOther] = {
    XMLWrites(resultsOfControl => <RESOFCON534>
        <DesTOC2>{resultsOfControl.description}</DesTOC2>
        <DesTOC2LNG>{LanguageCodeEnglish.code}</DesTOC2LNG>
        <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
      </RESOFCON534>)
  }
}

case class ResultsOfControlDifferentValues(pointerToAttribute: PointerToAttribute, correctedValue: String) extends ResultsOfControl {
  val controlIndicator = ControlIndicator(DifferentValuesFound)
}

object ResultsOfControlDifferentValues {

  implicit val writes: XMLWrites[ResultsOfControlDifferentValues] = {
    XMLWrites(resultsOfControl => <RESOFCON534>
      <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
      <PoiToTheAttTOC5>{resultsOfControl.pointerToAttribute.pointer.value}</PoiToTheAttTOC5>
      <CorValTOC4>{resultsOfControl.correctedValue}</CorValTOC4>
    </RESOFCON534>)
  }
}

object ResultsOfControl {
  val descriptionLength    = 140
  val correctedValueLength = 27
}

//TODO: Question - when setting results of control, the ControlIndicator can only be set to DI or OT.
// WHat happens if seals are changed and a user reports something? What value do you send?
// Don't send a new value, just include seals as is and flag stateOfSeals to 0
//If errors are found at the HEADER level, then RoC-Control Indicator is set to: - DI (DIfferent values found) or
//- OT (any OTher things to report)
