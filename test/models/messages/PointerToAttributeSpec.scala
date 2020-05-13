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

import models.XMLWrites._
import org.scalatest.{FreeSpec, MustMatchers}

import scala.xml.Node
import scala.xml.Utility.trim

class PointerToAttributeSpec extends FreeSpec with MustMatchers {

  "PointerToAttributeSpec" - {

    "must convert to xml node" - {

      "for TransportIdentity" in {
        val xml: Node = <PoiToTheAttTOC5>18#1</PoiToTheAttTOC5>
        PointerToAttribute(TransportIdentity).toXml.map(trim) mustBe xml.map(trim)
      }

      "for TransportCountry" in {
        val xml: Node = <PoiToTheAttTOC5>18#2</PoiToTheAttTOC5>
        PointerToAttribute(TransportCountry).toXml.map(trim) mustBe xml.map(trim)
      }

      "for NumberOfItems" in {
        val xml: Node = <PoiToTheAttTOC5>5</PoiToTheAttTOC5>
        PointerToAttribute(NumberOfItems).toXml.map(trim) mustBe xml.map(trim)
      }

      "for NumberOfPackages" in {
        val xml: Node = <PoiToTheAttTOC5>6</PoiToTheAttTOC5>
        PointerToAttribute(NumberOfPackages).toXml.map(trim) mustBe xml.map(trim)
      }

      "for GrossMass" in {
        val xml: Node = <PoiToTheAttTOC5>35</PoiToTheAttTOC5>
        PointerToAttribute(GrossMass).toXml.map(trim) mustBe xml.map(trim)
      }

    }

  }

}
