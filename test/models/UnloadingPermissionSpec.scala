/*
 * Copyright 2021 HM Revenue & Customs
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
import cats.data.NonEmptyList
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, XmlReader}
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Utility.trim
import scala.xml.{Elem, NodeSeq}

class UnloadingPermissionSpec extends FreeSpec with MustMatchers with Generators with ScalaCheckPropertyChecks {

  import UnloadingPermissionSpec._

  "UnloadingPermission" - {

    "must serialize UnloadingPermission from Xml" in {

      forAll(arbitrary[UnloadingPermission]) {
        unloadingPermission =>
          //NOTE: Only extracting what we need for UnloadingRemarks message
          val expectedResult = {
            <CC043A>
              <HEAHEA>
                <DocNumHEA5>{unloadingPermission.movementReferenceNumber}</DocNumHEA5>
                {transportIdentity(unloadingPermission.transportIdentity).getOrElse(NodeSeq.Empty)}
                {transportCountry(unloadingPermission.transportCountry).getOrElse(NodeSeq.Empty)}
                <TotNumOfIteHEA305>{unloadingPermission.numberOfItems}</TotNumOfIteHEA305>
                {
                  unloadingPermission.numberOfPackages.fold(NodeSeq.Empty) { numberOfPackages =>
                    <TotNumOfPacHEA306>{numberOfPackages}</TotNumOfPacHEA306>
                  }
                }
                <TotGroMasHEA307>{unloadingPermission.grossMass}</TotGroMasHEA307>
              </HEAHEA>
              {trader(unloadingPermission.traderAtDestination)}
              <CUSOFFPREOFFRES>
                <RefNumRES1>{unloadingPermission.presentationOffice}</RefNumRES1>
              </CUSOFFPREOFFRES>
              {seals(unloadingPermission.seals)}
              {goodsItems(unloadingPermission.goodsItems)}
            </CC043A>
          }

          XmlReader.of[UnloadingPermission].read(trim(expectedResult)) mustBe
            ParseSuccess(unloadingPermission)
      }
    }

    "return ParseFailure when converting into UnloadingPermission with no goodsItem" in {

      val unloadingPermissionObject = arbitrary[UnloadingPermission]

      val unloadingPermission = unloadingPermissionObject.sample.get

      val expectedResult = {
        <CC043A>
          <HEAHEA>
            <DocNumHEA5>{unloadingPermission.movementReferenceNumber}</DocNumHEA5>
            {transportIdentity(unloadingPermission.transportIdentity).getOrElse(NodeSeq.Empty)}
            {transportCountry(unloadingPermission.transportCountry).getOrElse(NodeSeq.Empty)}
            <TotNumOfIteHEA305>{unloadingPermission.numberOfItems}</TotNumOfIteHEA305>
            {
              unloadingPermission.numberOfPackages.fold(NodeSeq.Empty) { numberOfPackages =>
                <TotNumOfPacHEA306>{numberOfPackages}</TotNumOfPacHEA306>
              }
            }
            <TotGroMasHEA307>{unloadingPermission.grossMass}</TotGroMasHEA307>
          </HEAHEA>
          {trader(unloadingPermission.traderAtDestination)}
          <CUSOFFPREOFFRES>
            <RefNumRES1>{unloadingPermission.presentationOffice}</RefNumRES1>
          </CUSOFFPREOFFRES>
          {seals(unloadingPermission.seals)}
        </CC043A>
      }

      XmlReader.of[UnloadingPermission].read(trim(expectedResult)) mustBe
        ParseFailure(List())
    }

    "return ParseFailure when converting into UnloadingPermission with no packages" in {

      val unloadingPermissionObject = arbitrary[UnloadingPermission]

      val unloadingPermission = unloadingPermissionObject.sample.get

      val expectedResult = {
        <CC043A>
          <HEAHEA>
            <DocNumHEA5>{unloadingPermission.movementReferenceNumber}</DocNumHEA5>
            {transportIdentity(unloadingPermission.transportIdentity).getOrElse(NodeSeq.Empty)}
            {transportCountry(unloadingPermission.transportCountry).getOrElse(NodeSeq.Empty)}
            <TotNumOfIteHEA305>{unloadingPermission.numberOfItems}</TotNumOfIteHEA305>
            {
              unloadingPermission.numberOfPackages.fold(NodeSeq.Empty) { numberOfPackages =>
                <TotNumOfPacHEA306>{numberOfPackages}</TotNumOfPacHEA306>
              }
            }
            <TotGroMasHEA307>{unloadingPermission.grossMass}</TotGroMasHEA307>
          </HEAHEA>
          {trader(unloadingPermission.traderAtDestination)}
          <CUSOFFPREOFFRES>
            <RefNumRES1>{unloadingPermission.presentationOffice}</RefNumRES1>
          </CUSOFFPREOFFRES>
          {seals(unloadingPermission.seals)}
          {goodsItems(unloadingPermission.goodsItems, ignorePackages = true)}
        </CC043A>
      }

      XmlReader.of[UnloadingPermission].read(trim(expectedResult)) mustBe
        ParseFailure(List())
    }

  }

}

object UnloadingPermissionSpec {

  val transportIdentity: Option[String] => Option[Elem] =
    transportIdentity =>
      transportIdentity.map {
        transportIdentity =>
          <IdeOfMeaOfTraAtDHEA78>{transportIdentity}</IdeOfMeaOfTraAtDHEA78>
    }

  val transportCountry: Option[String] => Option[Elem] =
    transportCountry =>
      transportCountry.map {
        transportCountry =>
          <NatOfMeaOfTraAtDHEA80>{transportCountry}</NatOfMeaOfTraAtDHEA80>
    }

  def goodsItems(goodsItem: NonEmptyList[GoodsItem], ignorePackages: Boolean = false) = {

    import GoodsItemSpec._

    val response = goodsItem.map {
      goodsItem =>
        val commodityCode: Option[Elem] = goodsItem.commodityCode.map {
          code =>
            <ComCodTarCodGDS10>{code}</ComCodTarCodGDS10>
        }

        val grossMass: Option[Elem] = goodsItem.grossMass.map {
          grossMass =>
            <GroMasGDS46>{grossMass}</GroMasGDS46>
        }

        val netMass: Option[Elem] = goodsItem.netMass.map {
          netMass =>
            <NetMasGDS48>{netMass}</NetMasGDS48>
        }

        val containers: Seq[Elem] = goodsItem.containers.map {
          value =>
            <ConNumNR21>{value}</ConNumNR21>
        }

        <GOOITEGDS>
          <IteNumGDS7>
            {goodsItem.itemNumber}
          </IteNumGDS7>
          {commodityCode.getOrElse(NodeSeq.Empty)}
          <GooDesGDS23>
            {goodsItem.description}
          </GooDesGDS23>
          {grossMass.getOrElse(NodeSeq.Empty)}
          {netMass.getOrElse(NodeSeq.Empty)}
          {producedDocument(goodsItem)}
          {
          containers.map {
            x => <CONNR2>{x}</CONNR2>
          }
          }
          {if(!ignorePackages) packages(goodsItem) }
          {sensitiveGoodsInformation(goodsItem)}
        </GOOITEGDS>
    }

    response.toList
  }

  def seals(seals: Option[Seals]) = seals match {
    case Some(sealValues) => {
      <SEAINFSLI>
        <SeaNumSLI2>{sealValues.numberOfSeals}</SeaNumSLI2>
        <SEAIDSID>
          {
          sealValues.SealId.map {
            sealId => <SeaIdeSID1>{sealId}</SeaIdeSID1>
          }
          }
        </SEAIDSID>
      </SEAINFSLI>
    }
    case None => NodeSeq.Empty
  }

  def trader(traderAtDestination: TraderAtDestination): Elem = traderAtDestination match {

    case traderWithEori: TraderAtDestinationWithEori => {

      val name = traderWithEori.name.map {
        name =>
          <NamTRD7>{name}</NamTRD7>
      }

      val streetAndNumber = traderWithEori.streetAndNumber.map {
        streetAndNumber =>
          <StrAndNumTRD22>{streetAndNumber}</StrAndNumTRD22>
      }

      val postCode = traderWithEori.postCode.map {
        postCode =>
          <PosCodTRD23>{postCode}</PosCodTRD23>
      }

      val city = traderWithEori.city.map {
        city =>
          <CitTRD24>{city}</CitTRD24>
      }

      val countryCode = traderWithEori.countryCode.map {
        countryCode =>
          <CouTRD25>{countryCode}</CouTRD25>
      }

      <TRADESTRD>
        {name.getOrElse(NodeSeq.Empty)}
        {streetAndNumber.getOrElse(NodeSeq.Empty)}
        {postCode.getOrElse(NodeSeq.Empty)}
        {city.getOrElse(NodeSeq.Empty)}
        {countryCode.getOrElse(NodeSeq.Empty)}
        <TINTRD59>{traderWithEori.eori}</TINTRD59>
      </TRADESTRD>

    }
    case traderWithOutEori: TraderAtDestinationWithoutEori => {

      <TRADESTRD>
        <NamTRD7>{traderWithOutEori.name}</NamTRD7>
        <StrAndNumTRD22>{traderWithOutEori.streetAndNumber}</StrAndNumTRD22>
        <PosCodTRD23>{traderWithOutEori.postCode}</PosCodTRD23>
        <CitTRD24>{traderWithOutEori.city}</CitTRD24>
        <CouTRD25>{traderWithOutEori.countryCode}</CouTRD25>
      </TRADESTRD>
    }
  }
}
