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
import java.time.LocalDate

import models.{LanguageCodeEnglish, XMLWrites}
import utils.Format

import scala.xml.NodeSeq

sealed trait Remarks {
  val conform: String //1 if no unloading remarks, 0 if unloading remarks are present OR any seals changed/updated OR any data changed
  val unloadingCompletion: String = "1" //0 if unloading of goods is not yet completed, 1 if goods are completely unloaded - This should always be 1
}

case class RemarksConform(unloadingDate: LocalDate) extends Remarks {
  val conform = "1"
}

object RemarksConform {
  implicit val writes: XMLWrites[RemarksConform] = {

    XMLWrites(remarks => <TRADESTRD>
      <ConREM65>{remarks.conform}</ConREM65>
      <UnlComREM66>{remarks.unloadingCompletion}</UnlComREM66>
      <UnlDatREM67>{Format.dateFormatted(remarks.unloadingDate)}</UnlDatREM67>
    </TRADESTRD>)
  }
}

case class RemarksNonConform(
  stateOfSeals: Option[Int], //n1 State of seals ok flag 0 (NO) or 1 (YES) - Optional, must be included if  IE043 contains seals
  unloadingRemark: Option[String],
  unloadingDate: LocalDate, // UnlDatREM67 date goods unloaded format: YYYYMMDD
  resultOfControl: Seq[ResultsOfControl] // up to 9, if conform is 1 this can not be used
) extends Remarks {
  val conform = "0"
}

object RemarksNonConform {

  import models.XMLWrites._

  val unloadingRemarkLength = 350

  implicit val writes: XMLWrites[RemarksNonConform] = {

    def resultOfControlNode(resultsOfControl: Seq[ResultsOfControl]): NodeSeq =
      resultsOfControl.flatMap {
        case y: ResultsOfControlOther           => y.toXml
        case y: ResultsOfControlDifferentValues => y.toXml
      }

    XMLWrites(remarks => <TRADESTRD>
      <UnlRemREM53LNG>{LanguageCodeEnglish.code}</UnlRemREM53LNG>
      <ConREM65>{remarks.conform}</ConREM65>
      <UnlComREM66>{remarks.unloadingCompletion}</UnlComREM66>
      <UnlDatREM67>{Format.dateFormatted(remarks.unloadingDate)}</UnlDatREM67>
    </TRADESTRD> +: resultOfControlNode(remarks.resultOfControl))
  }
}
