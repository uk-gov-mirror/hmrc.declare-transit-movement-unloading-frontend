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
import models.XMLWrites._
import models.{GoodsItem, Seals, Trader, XMLWrites}

import scala.xml.{Elem, Node, NodeSeq}

case class UnloadingRemarksRequest(
  meta: Meta,
  header: Header,
  trader: Trader,
  presentationOffice: String,
  unloadingRemark: Remarks,
  seals: Option[Seals],
  goodsItems: NonEmptyList[GoodsItem]
)

object UnloadingRemarksRequest {

  val transportIdentityLength  = 27
  val transportIdentityCountry = 2
  val numberOfItemsLength      = 5
  val numberOfPackagesLength   = 7
  val presentationOfficeLength = 8

  implicit def writes: XMLWrites[UnloadingRemarksRequest] = XMLWrites[UnloadingRemarksRequest] {
    unloadingRemarksRequest =>
      val parentNode: Node = <CC044A></CC044A>

      val childNodes: NodeSeq = {
        unloadingRemarksRequest.meta.toXml ++
          unloadingRemarksRequest.header.toXml ++
          unloadingRemarksRequest.trader.toXml ++
          <CUSOFFPREOFFRES>
            <RefNumRES1>{unloadingRemarksRequest.presentationOffice}</RefNumRES1>
          </CUSOFFPREOFFRES> ++
          unloadingRemarkNode(unloadingRemarksRequest.unloadingRemark) ++
          unloadingRemarksRequest.seals.map(_.toXml).getOrElse(NodeSeq.Empty) ++
          unloadingRemarksRequest.goodsItems.map(x => x.toXml).toList.flatten
      }

      Elem(parentNode.prefix, parentNode.label, parentNode.attributes, parentNode.scope, parentNode.child.isEmpty, parentNode.child ++ childNodes: _*)
  }

  private def unloadingRemarkNode(unloadingRemark: Remarks): NodeSeq = unloadingRemark match {
    case remarksConform: RemarksConform                   => remarksConform.toXml
    case remarksConformWithSeals: RemarksConformWithSeals => remarksConformWithSeals.toXml
    case remarksNonConform: RemarksNonConform             => remarksNonConform.toXml
  }
}
