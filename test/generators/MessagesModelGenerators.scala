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

package generators

import java.time.{LocalDate, LocalTime}

import models.messages.{Header, UnloadingRemarksRequest, _}
import models.{GoodsItem, Seals, TraderAtDestinationWithEori, TraderAtDestinationWithoutEori, UnloadingPermission}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.choose
import org.scalacheck.{Arbitrary, Gen}

trait MessagesModelGenerators extends Generators {

  implicit lazy val arbitraryMessageSender: Arbitrary[MessageSender] = {
    Arbitrary {
      for {
        environment <- Gen.oneOf(Seq("LOCAL", "QA", "STAGING", "PRODUCTION"))
        eori        <- stringsWithMaxLength(4: Int)
      } yield MessageSender(environment, eori)
    }
  }

  implicit lazy val arbitraryInterchangeControlReference: Arbitrary[InterchangeControlReference] = {
    Arbitrary {
      for {
        dateTime <- stringsWithMaxLength(4: Int)
        index    <- choose(min = 1: Int, 1000: Int)
      } yield InterchangeControlReference(dateTime, index)
    }
  }

  implicit lazy val arbitraryMeta: Arbitrary[Meta] = {
    Arbitrary {
      for {
        messageSender               <- arbitrary[MessageSender]
        interchangeControlReference <- arbitrary[InterchangeControlReference]
        date                        <- arbitrary[LocalDate]
        time                        <- arbitrary[LocalTime]
      } yield
        Meta(
          messageSender,
          interchangeControlReference,
          date,
          time,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
    }
  }

  implicit lazy val arbitraryHeader: Arbitrary[Header] = {
    Arbitrary {
      for {
        movementReferenceNumber <- stringsWithMaxLength(UnloadingPermission.movementReferenceNumberLength)
        transportIdentity       <- Gen.option(stringsWithMaxLength(UnloadingPermission.transportIdentityLength))
        transportCountry        <- Gen.option(Gen.pick(UnloadingPermission.transportCountryLength, 'A' to 'Z'))
        numberOfItems           <- choose(min = 1: Int, 2: Int)
        numberOfPackages        <- choose(min = 1: Int, 2: Int)
        grossMass               <- Gen.choose(0.0, 99999999.999).map(BigDecimal(_).bigDecimal.setScale(3, BigDecimal.RoundingMode.DOWN))
      } yield Header(movementReferenceNumber, transportIdentity, transportCountry.map(_.mkString), numberOfItems, numberOfPackages, grossMass.toString)
    }
  }

  implicit lazy val arbitraryUnloadingRemarksRequest: Arbitrary[UnloadingRemarksRequest] = {
    Arbitrary {
      for {
        meta               <- arbitrary[Meta]
        header             <- arbitrary[Header]
        traderDestination  <- Gen.oneOf(arbitrary[TraderAtDestinationWithEori], arbitrary[TraderAtDestinationWithoutEori])
        presentationOffice <- Gen.pick(UnloadingRemarksRequest.presentationOfficeLength, 'A' to 'Z')
        remarks            <- Gen.oneOf(arbitrary[RemarksConform], arbitrary[RemarksConformWithSeals], arbitrary[RemarksNonConform])
        seals              <- Gen.option(arbitrary[Seals])
        goodsItems         <- nonEmptyListWithMaxSize(2: Int, arbitrary[GoodsItem])
      } yield UnloadingRemarksRequest(meta, header, traderDestination, presentationOffice.mkString, remarks, seals, goodsItems)
    }
  }
}
