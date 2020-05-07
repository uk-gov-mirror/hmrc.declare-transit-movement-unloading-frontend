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
import models.messages._
import org.scalatest.{FreeSpec, MustMatchers}

import models.XMLWrites._
import scala.xml.Node
import scala.xml.Utility.trim

class ResultsOfControlSpec extends FreeSpec with MustMatchers {

  val resultsOfControlOther = ResultsOfControlOther(
    description = "test"
  )

  val resultsOfControlDifferentValues = ResultsOfControlDifferentValues(
    pointerToAttribute = PointerToAttribute(TransportIdentity),
    correctedValue     = "corrected value here"
  )

  //TODO: Arbitrarys
  "ResultsOfControlSpec" - {

    "convert ResultsOfControlOther to xml node" in {

      val xml: Node =
        <RESOFCON534>
          <DesTOC2>test</DesTOC2>
          <DesTOC2LNG>EN</DesTOC2LNG>
          <ConInd424>OT</ConInd424>
        </RESOFCON534>

      resultsOfControlOther.toXml.map(trim) mustBe xml.map(trim)
    }

    "convert ResultsOfControlDifferentValues to xml node" in {

      val xml: Node =
        <RESOFCON534>
          <ConInd424>DI</ConInd424>
          <PoiToTheAttTOC5>18#1</PoiToTheAttTOC5>
          <CorValTOC4>corrected value here</CorValTOC4>
        </RESOFCON534>

      resultsOfControlDifferentValues.toXml.map(trim) mustBe xml.map(trim)
    }
  }

}
