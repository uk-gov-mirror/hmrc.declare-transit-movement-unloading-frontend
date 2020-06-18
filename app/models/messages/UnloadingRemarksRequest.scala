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
import models.{GoodsItem, Seals, TraderAtDestination, TraderAtDestinationWithEori, TraderAtDestinationWithoutEori, XMLWrites}

import scala.xml.{Elem, Node, NodeSeq}
import models.XMLWrites._

case class UnloadingRemarksRequest(
  meta: Meta,
  header: Header,
  traderAtDestination: TraderAtDestination,
  presentationOffice: String,
  unloadingRemark: Remarks,
  seals: Option[Seals],
  goodsItems: NonEmptyList[GoodsItem]
)

object UnloadingRemarksRequest {

  val transportIdentityLength    = 27
  val transportIdentityCountry   = 2
  val numberOfItems              = 99999
  val numberOfPackages           = 999999
  val presentationOfficeLength   = 8
  val newSealNumberMaximumLength = 20
  val vehicleNameMaxLength       = 27
  val alphaNumericRegex          = "[A-Za-z0-9]+".r

  implicit def writes: XMLWrites[UnloadingRemarksRequest] = XMLWrites[UnloadingRemarksRequest] {
    unloadingRemarksRequest =>
      val parentNode: Node = <CC044A></CC044A>

      val childNodes: NodeSeq = {
        unloadingRemarksRequest.meta.toXml ++
          unloadingRemarksRequest.header.toXml ++
          traderAtDesinationNode(unloadingRemarksRequest.traderAtDestination) ++
          <CUSOFFPREOFFRES>
            <RefNumRES1>{unloadingRemarksRequest.presentationOffice}</RefNumRES1>
          </CUSOFFPREOFFRES> ++
          unloadingRemarkNode(unloadingRemarksRequest.unloadingRemark) ++
          unloadingRemarksRequest.seals.map(_.toXml).getOrElse(NodeSeq.Empty) ++
          unloadingRemarksRequest.goodsItems.map(x => x.toXml).toList.flatten
      }

      Elem(parentNode.prefix, parentNode.label, parentNode.attributes, parentNode.scope, parentNode.child.isEmpty, parentNode.child ++ childNodes: _*)
  }

  private def traderAtDesinationNode(traderAtDestination: TraderAtDestination): NodeSeq = traderAtDestination match {
    case traderAtDestinationWithEori: TraderAtDestinationWithEori       => traderAtDestinationWithEori.toXml
    case traderAtDestinationWithoutEori: TraderAtDestinationWithoutEori => traderAtDestinationWithoutEori.toXml
  }

  private def unloadingRemarkNode(unloadingRemark: Remarks): NodeSeq = unloadingRemark match {
    case remarksConform: RemarksConform                   => remarksConform.toXml
    case remarksConformWithSeals: RemarksConformWithSeals => remarksConformWithSeals.toXml
    case remarksNonConform: RemarksNonConform             => remarksNonConform.toXml
  }
}
