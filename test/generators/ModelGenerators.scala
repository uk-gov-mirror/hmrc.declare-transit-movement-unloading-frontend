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

import java.time.LocalDate

import models.messages._
import models.reference.Country
import models.{UnloadingPermission, _}
import org.scalacheck.Arbitrary.{arbitrary, _}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen.choose

trait ModelGenerators {

  self: Generators =>

  implicit lazy val arbitraryMovementReferenceNumber: Arbitrary[MovementReferenceNumber] =
    Arbitrary {
      for {
        year    <- Gen.choose(0, 99).map(y => f"$y%02d")
        country <- Gen.pick(2, 'A' to 'Z')
        serial  <- Gen.pick(13, ('A' to 'Z') ++ ('0' to '9'))
      } yield MovementReferenceNumber(year, country.mkString, serial.mkString)
    }

  implicit lazy val arbitrarySensitiveGoodsInformation: Arbitrary[SensitiveGoodsInformation] =
    Arbitrary {
      for {
        goodsCode <- Gen.option(Gen.choose(0: Int, 99: Int))
        quantity  <- Gen.choose(1: Int, 1000: Int)
      } yield SensitiveGoodsInformation(goodsCode, quantity)
    }

  implicit lazy val arbitraryPackages: Arbitrary[Packages] =
    Arbitrary {
      for {
        marksAndNumberOfPackages <- Gen.option(stringsWithMaxLength(Packages.marksAndNumberPackageLength))
        kindOfPackage            <- stringsWithMaxLength(Packages.kindOfPackageLength)
        numberOfPackages         <- Gen.option(Gen.choose(0: Int, 100: Int))
        numberOfPieces           <- Gen.option(Gen.choose(0: Int, 100: Int))
      } yield Packages(marksAndNumberOfPackages, kindOfPackage, numberOfPackages, numberOfPieces)
    }

  implicit lazy val arbitraryProducedDocument: Arbitrary[ProducedDocument] =
    Arbitrary {
      for {
        documentType            <- stringsWithMaxLength(ProducedDocument.documentTypeLength)
        reference               <- Gen.option(stringsWithMaxLength(ProducedDocument.referenceLength))
        complementOfInformation <- Gen.option(stringsWithMaxLength(ProducedDocument.complementOfInformationLength))
      } yield ProducedDocument(documentType, reference, complementOfInformation)
    }

  implicit lazy val arbitraryTrader: Arbitrary[Trader] =
    Arbitrary {
      for {
        eori            <- stringsWithMaxLength(Trader.Constants.eoriLength)
        name            <- stringsWithMaxLength(Trader.Constants.nameLength)
        streetAndNumber <- stringsWithMaxLength(Trader.Constants.streetAndNumberLength)
        postCode        <- stringsWithMaxLength(Trader.Constants.postCodeLength)
        city            <- stringsWithMaxLength(Trader.Constants.cityLength)
        countryCode     <- Gen.pick(2, 'A' to 'Z')
      } yield Trader(eori, name, streetAndNumber, postCode, city, countryCode.mkString)
    }

  //TODO: Check spec and add correct max sizes as constants
  implicit lazy val arbitraryUnloadingPermission: Arbitrary[UnloadingPermission] =
    Arbitrary {
      for {
        movementReferenceNumber <- stringsWithMaxLength(UnloadingPermission.movementReferenceNumberLength)
        transportIdentity       <- Gen.option(stringsWithMaxLength(UnloadingPermission.transportIdentityLength))
        transportCountry        <- Gen.option(Gen.pick(2, 'A' to 'Z'))
        numberOfItems           <- choose(min = 1: Int, 2: Int)
        numberOfPackages        <- choose(min = 1: Int, 2: Int)
        grossMass               <- stringsWithMaxLength(2: Int)
        trader                  <- arbitrary[Trader]
        presentationOffice      <- stringsWithMaxLength(UnloadingPermission.presentationOfficeLength)
        seals                   <- Gen.option(arbitrary[Seals])
        goodsItems              <- nonEmptyListWithMaxSize(2: Int, arbitrary[GoodsItem])
      } yield
        UnloadingPermission(
          movementReferenceNumber,
          transportIdentity,
          transportCountry.map(_.mkString),
          numberOfItems,
          numberOfPackages,
          grossMass,
          trader,
          presentationOffice,
          seals,
          goodsItems
        )
    }

  //TODO: Check spec and add correct max sizes as constants
  implicit lazy val arbitrarySeals: Arbitrary[Seals] =
    Arbitrary {
      for {
        numberOfSeals <- choose(min = 1: Int, Seals.maxSeals: Int)
        sealId        <- listWithMaxSize(numberOfSeals, stringsWithMaxLength(Seals.sealIdLength))
      } yield Seals(numberOfSeals, sealId)
    }

  //TODO: Check spec and add correct max sizes as constants
  implicit lazy val arbitraryGoodsItem: Arbitrary[GoodsItem] =
    Arbitrary {
      for {
        itemNumber                <- choose(min = 1: Int, 100: Int)
        commodityCode             <- Gen.option(stringsWithMaxLength(GoodsItem.commodityCodeLength: Int))
        description               <- stringsWithMaxLength(Packages.kindOfPackageLength)
        grossMass                 <- Gen.option(Gen.choose(0.0, 99999999.999).map(BigDecimal(_).bigDecimal.setScale(3, BigDecimal.RoundingMode.DOWN))) //BigDecimal.RoundingMode.DOWN
        netMass                   <- Gen.option(Gen.choose(0.0, 99999999.999).map(BigDecimal(_).bigDecimal.setScale(3, BigDecimal.RoundingMode.DOWN))) //todo does this need to be a bigDecimal
        producedDocuments         <- nonEmptyListWithMaxSize(GoodsItem.maxDocuments: Int, arbitrary[ProducedDocument])
        containers                <- listWithMaxSize(GoodsItem.maxContainers, stringsWithMaxLength(GoodsItem.containerLength))
        packages                  <- arbitrary[Packages] //todo should this be a nonEmptySeq
        sensitiveGoodsInformation <- listWithMaxLength[SensitiveGoodsInformation](GoodsItem.maxSensitiveGoods: Int)
      } yield
        GoodsItem(
          itemNumber,
          commodityCode,
          description,
          grossMass.map(_.toString),
          netMass.map(_.toString),
          producedDocuments,
          containers,
          packages,
          sensitiveGoodsInformation
        )
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] = {
    Arbitrary {
      for {
        state <- Gen.oneOf(Seq("Valid", "Invalid"))
        code  <- Gen.pick(2, 'A' to 'Z')
        name  <- arbitrary[String]
      } yield Country(state, code.mkString, name)
    }
  }

  implicit lazy val arbitraryDifferentValuesFound: Arbitrary[IndicatorValue] = {
    Arbitrary {
      Gen.oneOf(IndicatorValue.values)
    }
  }

  implicit lazy val arbitraryControlIndicator: Arbitrary[ControlIndicator] = {
    Arbitrary {
      for {
        indicator <- arbitrary[IndicatorValue]
      } yield ControlIndicator(indicator)
    }
  }

  implicit lazy val arbitraryPointerToAttribute: Arbitrary[PointerToAttribute] = {
    Arbitrary {
      for {
        pointer <- Gen.oneOf(PointerIdentity.implementations)
      } yield PointerToAttribute(pointer)

    }
  }

  implicit lazy val arbitraryResultsOfControlOther: Arbitrary[ResultsOfControlOther] = {
    Arbitrary {
      for {
        description <- stringsWithMaxLength(ResultsOfControl.descriptionLength)
      } yield ResultsOfControlOther(description)
    }
  }

  implicit lazy val arbitraryResultsOfControlDifferentValues: Arbitrary[ResultsOfControlDifferentValues] = {
    Arbitrary {
      for {
        pointerToAttribute <- arbitrary[PointerToAttribute]
        correctedValue     <- stringsWithMaxLength(ResultsOfControl.correctedValueLength)
      } yield ResultsOfControlDifferentValues(pointerToAttribute, correctedValue)
    }
  }

  implicit lazy val arbitraryResultsOfControl: Arbitrary[ResultsOfControl] = {
    Arbitrary {
      Gen.oneOf(arbitrary[ResultsOfControlOther], arbitrary[ResultsOfControlDifferentValues])
    }
  }

  implicit lazy val arbitraryRemarksConform: Arbitrary[RemarksConform] = {
    Arbitrary {
      for {
        date <- arbitrary[LocalDate]
      } yield RemarksConform(date)
    }
  }

  implicit lazy val arbitraryRemarksConformWithSeals: Arbitrary[RemarksConformWithSeals] = {
    Arbitrary {
      for {
        date <- arbitrary[LocalDate]
      } yield RemarksConformWithSeals(date)
    }
  }

  implicit lazy val arbitraryRemarksNonConform: Arbitrary[RemarksNonConform] = {
    Arbitrary {
      for {
        stateOfSeals    <- Gen.option(choose(min = 0: Int, 1: Int))
        date            <- arbitrary[LocalDate]
        unloadingRemark <- Gen.option(stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength))
        resultOfControl <- listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)
      } yield
        RemarksNonConform(
          stateOfSeals    = stateOfSeals,
          unloadingRemark = unloadingRemark,
          unloadingDate   = date,
          resultOfControl = resultOfControl
        )
    }
  }
}
