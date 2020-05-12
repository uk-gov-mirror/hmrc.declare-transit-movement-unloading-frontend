package models
import generators.MessagesModelGenerators

import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import models.XMLWrites._

import scala.xml.NodeSeq

class TraderDestinationSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {

  "TraderDestination" - {

    "must create valid xml" in {
      forAll(arbitrary[TraderAtDestinationWithEori]) {
        trader =>

          val nameNode = trader.name.map(
            name => <NamTRD7>{name}</NamTRD7>
          )
          val streetNameNode = trader.streetAndNumber.map(
            streetName => <StrAndNumTRD22>{streetName}</StrAndNumTRD22>
          )
          //TODO: Double check postcode is used
          val postCodeNode = trader.postCode.map(
            postcode => <PosCodTRD23>{postcode}</PosCodTRD23>
          )
          val cityNode = trader.city.map(
            city => <CitTRD24>{city}</CitTRD24>
          )
          val countryCodeNode = trader.countryCode.map(
            countryCode => <CouTRD25>{countryCode}</CouTRD25>
          )


        val expectedResult = <TRADESTRD>
          nameNode.getOrElse(NodeSeq.Empty) ++
          streetNameNode.getOrElse(NodeSeq.Empty) ++
          postCodeNode.getOrElse(NodeSeq.Empty) ++
          cityNode.getOrElse(NodeSeq.Empty) ++
          countryCodeNode.getOrElse(NodeSeq.Empty) ++
          <TINTRD59>{trader.eori}</TINTRD59>
        </TRADESTRD>

          trader.toXml mustEqual expectedResult
        }

    }

  }
}
