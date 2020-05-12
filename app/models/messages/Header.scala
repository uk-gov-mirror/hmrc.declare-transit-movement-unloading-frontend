package models.messages
import models.{LanguageCode, LanguageCodeEnglish, XMLWrites}

import scala.xml.NodeSeq

case class Header(
                   movementReferenceNumber: String,
                   transportIdentity: Option[String],
                   transportCountry: Option[String],
                   numberOfItems: Int,
                   numberOfPackages: Int,
                   grossMass: String
                 )

object Header {

  object Constants {
    val languageCode: LanguageCode     = LanguageCodeEnglish
  }

  implicit def writes: XMLWrites[Header] = XMLWrites[Header] {
    header =>
      <HEAHEA>
        <DocNumHEA5>{escapeXml(header.movementReferenceNumber)}</DocNumHEA5>
        {
          header.transportIdentity.fold(NodeSeq.Empty) { transportIdentity =>
            <IdeOfMeaOfTraAtDHEA78>{escapeXml(transportIdentity)}</IdeOfMeaOfTraAtDHEA78>
            <IdeOfMeaOfTraAtDHEA78LNG>{Header.Constants.languageCode.code}</IdeOfMeaOfTraAtDHEA78LNG>
          }
        }
        {
          header.transportCountry.fold(NodeSeq.Empty) { transportCountry =>
            <NatOfMeaOfTraAtDHEA80>{escapeXml(transportCountry)}</NatOfMeaOfTraAtDHEA80>
          }
        }
        <TotNumOfIteHEA305>{header.numberOfItems}</TotNumOfIteHEA305>
        <TotNumOfPacHEA306>{header.numberOfPackages}</TotNumOfPacHEA306>
        <TotGroMasHEA307>{escapeXml(header.grossMass)}</TotGroMasHEA307>
      </HEAHEA>
  }
}
