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

import generators.MessagesModelGenerators
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.NodeSeq

class HeaderSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {

  "HeaderSpec" - {

    "must serialize Header to xml" in {
      forAll(arbitrary[Header]) {
        header =>
          val transportIdentity = header.transportIdentity.map(
            transportIdentity => <IdeOfMeaOfTraAtDHEA78>{escapeXml(transportIdentity)}</IdeOfMeaOfTraAtDHEA78>
            <IdeOfMeaOfTraAtDHEA78LNG>EN</IdeOfMeaOfTraAtDHEA78LNG>
          )

          val transportCountry = header.transportCountry.map(
            transportCountry => <NatOfMeaOfTraAtDHEA80>{escapeXml(transportCountry)}</NatOfMeaOfTraAtDHEA80>
          )

          val expectedResult: NodeSeq =
            <HEAHEA>
            <DocNumHEA5>{escapeXml(header.movementReferenceNumber)}</DocNumHEA5>
            {transportIdentity.getOrElse(NodeSeq.Empty)}
            {transportCountry.getOrElse(NodeSeq.Empty)}
            <TotNumOfIteHEA305>{header.numberOfItems}</TotNumOfIteHEA305>
            <TotNumOfPacHEA306>{header.numberOfPackages}</TotNumOfPacHEA306>
            <TotGroMasHEA307>{escapeXml(header.grossMass)}</TotGroMasHEA307>
          </HEAHEA>

          header.toXml mustEqual expectedResult
      }

    }

  }

}
