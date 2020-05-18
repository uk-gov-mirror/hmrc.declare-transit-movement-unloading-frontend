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
import models.{TraderAtDestination, TraderAtDestinationWithEori, TraderAtDestinationWithoutEori}
import org.scalacheck.Arbitrary._
import org.scalatest.{FreeSpec, MustMatchers, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import models.XMLWrites._

import scala.xml.{Node, NodeSeq}
import scala.xml.Utility.trim

class UnloadingRemarksRequestSpec extends FreeSpec with MustMatchers with MessagesModelGenerators with ScalaCheckPropertyChecks with StreamlinedXmlEquality {

  "UnloadingRemarksRequestSpec" - {

    "must serialise UnloadingRemarks to xml" in {

      forAll(arbitrary[UnloadingRemarksRequest]) {
        unloadingRemarksRequest =>
          val expectedResult: Node =
            <CC044A>
              {unloadingRemarksRequest.meta.toXml}
              {unloadingRemarksRequest.header.toXml}
              {traderAtDesinationNode(unloadingRemarksRequest.traderAtDestination)}
              <CUSOFFPREOFFRES>
                <RefNumRES1>{unloadingRemarksRequest.presentationOffice}</RefNumRES1>
              </CUSOFFPREOFFRES>
              {unloadingRemarkNode(unloadingRemarksRequest.unloadingRemark)}
              {unloadingRemarksRequest.seals.map(_.toXml).getOrElse(NodeSeq.Empty)}
              {unloadingRemarksRequest.goodsItems.map(x => x.toXml).toList.flatten}
            </CC044A>

          unloadingRemarksRequest.toXml.map(trim) mustBe expectedResult.map(trim)
      }

    }

  }

  //TODO: Get toXml on TraderAtDestination interface so we don't have to do this
  private def traderAtDesinationNode(traderAtDestination: TraderAtDestination): NodeSeq = traderAtDestination match {
    case traderAtDestinationWithEori: TraderAtDestinationWithEori       => traderAtDestinationWithEori.toXml
    case traderAtDestinationWithoutEori: TraderAtDestinationWithoutEori => traderAtDestinationWithoutEori.toXml
  }

  //TODO: Get toXml on Remarks interface so we don't have to do this
  private def unloadingRemarkNode(unloadingRemark: Remarks): NodeSeq = unloadingRemark match {
    case remarksConform: RemarksConform                   => remarksConform.toXml
    case remarksConformWithSeals: RemarksConformWithSeals => remarksConformWithSeals.toXml
    case remarksNonConform: RemarksNonConform             => remarksNonConform.toXml
  }

}
