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
import java.time.LocalDate

import models.XMLWrites._
import models.messages.{RemarksConform, RemarksNonConform}
import org.scalatest.{FreeSpec, MustMatchers}
import utils.Format

import scala.xml.Node
import scala.xml.Utility.trim

class RemarksSpec extends FreeSpec with MustMatchers {

  val remarks = RemarksNonConform(
    stateOfSeals    = None,
    unloadingRemark = None,
    unloadingDate   = LocalDate.now(),
    resultOfControl = Nil
  )

  val remarksConform = RemarksConform(
    unloadingDate = LocalDate.now()
  )

  //TODO: Update to use Arbitrary
  "RemarksSpec" - {

    "convert RemarksNonConform to xml node" in {

      val xml: Node =
        <TRADESTRD>
          <UnlRemREM53LNG>EN</UnlRemREM53LNG>
          <ConREM65>0</ConREM65>
          <UnlComREM66>1</UnlComREM66>
          <UnlDatREM67>{Format.dateFormatted(remarks.unloadingDate)}</UnlDatREM67>
        </TRADESTRD>

      remarks.toXml.map(trim) mustBe xml.map(trim)
    }

    "convert RemarksConform to xml node" in {

      val xml: Node =
        <TRADESTRD>
          <ConREM65>1</ConREM65>
          <UnlComREM66>1</UnlComREM66>
          <UnlDatREM67>{Format.dateFormatted(remarksConform.unloadingDate)}</UnlDatREM67>
        </TRADESTRD>

      remarksConform.toXml.map(trim) mustBe xml.map(trim)

    }

  }

}
