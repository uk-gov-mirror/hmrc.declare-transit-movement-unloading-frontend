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

import generators.{Generators, ModelGenerators}
import models.XMLWrites._
import models.messages._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.Format

import scala.xml.Utility.trim
import scala.xml.{Elem, Node, NodeSeq}

class RemarksSpec extends FreeSpec with MustMatchers with Generators with ModelGenerators with ScalaCheckPropertyChecks {

  import RemarksSpec._

  "RemarksSpec" - {

    "must serialize RemarksConform to xml" in {

      forAll(arbitrary[RemarksConform]) {
        remarksConform =>
          val xml: Node =
            <UNLREMREM>
              <ConREM65>1</ConREM65>
              <UnlComREM66>1</UnlComREM66>
              <UnlDatREM67>{Format.dateFormatted(remarksConform.unloadingDate)}</UnlDatREM67>
            </UNLREMREM>

          remarksConform.toXml.map(trim) mustBe xml.map(trim)
      }
    }

    "must serialize RemarksConformWithSeals to xml" in {

      forAll(arbitrary[RemarksConformWithSeals]) {
        remarksConformWithSeals =>
          val xml: Node =
            <UNLREMREM>
              <StaOfTheSeaOKREM19>1</StaOfTheSeaOKREM19>
              <ConREM65>1</ConREM65>
              <UnlComREM66>1</UnlComREM66>
              <UnlDatREM67>{Format.dateFormatted(remarksConformWithSeals.unloadingDate)}</UnlDatREM67>
            </UNLREMREM>

          remarksConformWithSeals.toXml.map(trim) mustBe xml.map(trim)
      }
    }

    "must serialize RemarksNonConform to xml" in {

      forAll(arbitrary[RemarksNonConform]) {
        remarksNonConform =>
          val stateOfSeals: Option[Elem] = remarksNonConform.stateOfSeals.map {
            int =>
              <StaOfTheSeaOKREM19>{int}</StaOfTheSeaOKREM19>
          }

          val unloadingRemarks: Option[Elem] = remarksNonConform.unloadingRemark.map {
            remarks =>
              <UnlRemREM53>{remarks}</UnlRemREM53>
          }

          val resultsOfControl: Seq[Node] = remarksNonConform.resultOfControl.flatMap {
            resultsOfControl =>
              resultsOfControlNode(resultsOfControl)
          }

          val xml: NodeSeq =
            <UNLREMREM>
              {stateOfSeals.getOrElse(NodeSeq.Empty)}
              {unloadingRemarks.getOrElse(NodeSeq.Empty)}
              <UnlRemREM53LNG>EN</UnlRemREM53LNG>
              <ConREM65>0</ConREM65>
              <UnlComREM66>1</UnlComREM66>
              <UnlDatREM67>{Format.dateFormatted(remarksNonConform.unloadingDate)}</UnlDatREM67>
            </UNLREMREM> +: resultsOfControl

          remarksNonConform.toXml.map(trim) mustBe xml.map(trim)
      }

    }

  }

}

object RemarksSpec {

  def resultsOfControlNode(resultsOfControl: ResultsOfControl): Elem = resultsOfControl match {
    case resultsOfControl: ResultsOfControlOther =>
      <RESOFCON534>
        <DesTOC2>{resultsOfControl.description}</DesTOC2>
        <DesTOC2LNG>EN</DesTOC2LNG>
        <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
      </RESOFCON534>
    case resultsOfControl: ResultsOfControlDifferentValues =>
      <RESOFCON534>
        <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
        <PoiToTheAttTOC5>{resultsOfControl.pointerToAttribute.pointer.value}</PoiToTheAttTOC5>
        <CorValTOC4>{resultsOfControl.correctedValue}</CorValTOC4>
      </RESOFCON534>
  }
}
