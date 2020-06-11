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
import com.lucidchart.open.xtract.XmlReader
import generators.MessagesModelGenerators
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues._
import org.scalatest.{FreeSpec, MustMatchers, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TraderSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {

  "TraderDestination" - {

    "Trader" - {

      "must create valid xml" in {
        forAll(arbitrary[Trader]) {
          trader =>
            val expectedResult = <TRADESTRD>
              <NamTRD7>{trader.name}</NamTRD7>
              <StrAndNumTRD22>{trader.streetAndNumber}</StrAndNumTRD22>
              <PosCodTRD23>{trader.postCode}</PosCodTRD23>
              <CitTRD24>{trader.city}</CitTRD24>
              <CouTRD25>{trader.countryCode}</CouTRD25>
              <NADLNGRD>{LanguageCodeEnglish.code}</NADLNGRD>
              <TINTRD59>{trader.eori}</TINTRD59>
            </TRADESTRD>

            trader.toXml mustEqual expectedResult
        }
      }

      "must read xml into valid model" in {

        forAll(arbitrary[Trader]) {
          trader =>
            val inputXml =
              <TRADESTRD>
                <NamTRD7>{trader.name}</NamTRD7>
                <StrAndNumTRD22>{trader.streetAndNumber}</StrAndNumTRD22>
                <CitTRD24>{trader.city}</CitTRD24>
                <PosCodTRD23>{trader.postCode}</PosCodTRD23>
                <CouTRD25>{trader.countryCode}</CouTRD25>
                <NADLNGRD>{LanguageCodeEnglish.code}</NADLNGRD>
                <TINTRD59>{trader.eori}</TINTRD59>
              </TRADESTRD>

            val result = XmlReader.of[Trader].read(inputXml).toOption.value

            result mustEqual trader

        }
      }

      "must write to xml and read xml as trader destination" in {
        forAll(arbitrary[Trader]) {
          trader =>
            val result = XmlReader.of[Trader].read(trader.toXml).toOption.value

            result mustEqual trader

        }
      }
    }
  }

}
