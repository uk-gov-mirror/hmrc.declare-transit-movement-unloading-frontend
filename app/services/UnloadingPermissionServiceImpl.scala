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

package services
import cats.data.NonEmptyList
import com.google.inject.{Inject, Singleton}
import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import connectors.UnloadingConnector
import models.{GoodsItem, Index, MovementReferenceNumber, Packages, ProducedDocument, Seals, TraderAtDestinationWithEori, UnloadingPermission, UserAnswers}
import pages.NewSealNumberPage
import queries.SealsQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.xml.XML

@Singleton
class UnloadingPermissionServiceImpl @Inject()(connector: UnloadingConnector) extends UnloadingPermissionService {

  private val trader =
    TraderAtDestinationWithEori("GB163910077000", Some("The Luggage Carriers"), Some("225 Suedopolish Yard,"), Some("SS8 2BB"), Some(","), Some("GB"))

  private lazy val packages = Packages(Some("Ref."), "BX", Some(1), None)

  private lazy val producedDocuments = ProducedDocument("235", Some("Ref."), None)

  private lazy val goodsItemMandatory = GoodsItem(
    itemNumber                = 1,
    commodityCode             = None,
    description               = "Flowers",
    grossMass                 = Some("1000"),
    netMass                   = Some("999"),
    producedDocuments         = NonEmptyList(producedDocuments, Nil),
    containers                = Seq.empty,
    packages                  = packages,
    sensitiveGoodsInformation = Seq.empty
  )

  private val unloadingPermissionNoSeals = UnloadingPermission(
    movementReferenceNumber = "19IT02110010007827",
    transportIdentity       = None,
    transportCountry        = None,
    numberOfItems           = 1,
    numberOfPackages        = 1,
    grossMass               = "1000",
    traderAtDestination     = trader,
    presentationOffice      = "GB000060",
    seals                   = None,
    goodsItems              = NonEmptyList(goodsItemMandatory, Nil)
  )

  private val unloadingPermissionSeals = UnloadingPermission(
    movementReferenceNumber = "19IT02110010007827",
    transportIdentity       = Some("NX56RTA"),
    transportCountry        = Some("GB"),
    numberOfItems           = 2,
    numberOfPackages        = 1,
    grossMass               = "1000",
    traderAtDestination     = trader,
    presentationOffice      = "GB000060",
    seals                   = Some(Seals(1, Seq("Seals01", "Seals02"))),
    goodsItems              = NonEmptyList(goodsItemMandatory, List(goodsItemMandatory))
  )

  private val unloadingPermissionwithNoChanges = UnloadingPermission(
    movementReferenceNumber = "99IT9876AB88901209",
    transportIdentity       = None,
    transportCountry        = None,
    numberOfItems           = 1,
    numberOfPackages        = 1,
    grossMass               = "1000",
    traderAtDestination     = trader,
    presentationOffice      = "GB000060",
    seals                   = None,
    goodsItems              = NonEmptyList(goodsItemMandatory, Nil)
  )

  //TODO: This will call the connector but can initially hard code UnloadingPermission
  //TODO: to test the view
  def getUnloadingPermission(mrn: MovementReferenceNumber)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UnloadingPermission]] = {

    val arrivalID = mrn.toString match {
      case "19IT02110010007827" => 1
      case "41IT02110010007825" => 2
      case _                    => 3
    }

    connector.get(arrivalID).map {
      case Some(x) => {
        XmlReader.of[UnloadingPermission].read(XML.loadString(x.messages.head.message)) match {
          case ParseSuccess(unloadingPermission) => Some(unloadingPermission) //Some(unloadingPermissionSeals)
          case _                                 => None
        }
      }
      case None => None
    }

  }

//  def convertSeals(userAnswers: UserAnswers): Option[UserAnswers] = getUnloadingPermission(userAnswers.id) match {
//    case Some(unloadingPermission) =>
//      unloadingPermission.seals match {
//        case Some(seals) =>
//          userAnswers.set(SealsQuery, seals.SealId).map(ua => ua).toOption
//        case _ => Some(userAnswers)
//      }
//    case _ => None
//  }

  //TODO: Refactor
  def convertSeals(userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UserAnswers]] =
    getUnloadingPermission(userAnswers.id).flatMap {
      x =>
        x match {
          case Some(unloadingPermission) =>
            unloadingPermission.seals match {
              case Some(seals) =>
                Future.successful(userAnswers.set(SealsQuery, seals.SealId).map(ua => ua).toOption)
              case _ => Future.successful(Some(userAnswers))
            }
          case _ => Future.successful(None)
        }
    }
}

trait UnloadingPermissionService {
  def getUnloadingPermission(mrn: MovementReferenceNumber)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UnloadingPermission]]
  def convertSeals(userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UserAnswers]]
}
