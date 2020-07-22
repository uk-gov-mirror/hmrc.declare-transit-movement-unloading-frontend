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
import com.google.inject.Inject
import connectors.UnloadingConnector
import models.messages._
import models.{ArrivalId, EoriNumber, UnloadingPermission, UserAnswers}
import pages.{DateGoodsUnloadedPage, GrossMassAmountPage, TotalNumberOfItemsPage, TotalNumberOfPackagesPage, VehicleNameRegistrationReferencePage}
import play.api.Logger
import play.api.http.Status._
import repositories.InterchangeControlReferenceIdRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksService @Inject()(metaService: MetaService,
                                        remarksService: RemarksService,
                                        unloadingRemarksRequestService: UnloadingRemarksRequestService,
                                        interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository,
                                        unloadingRemarksMessageService: UnloadingRemarksMessageService,
                                        resultOfControlService: ResultOfControlService,
                                        unloadingConnector: UnloadingConnector)(implicit ec: ExecutionContext) {

  def submit(arrivalId: ArrivalId, eori: EoriNumber, userAnswers: UserAnswers, unloadingPermission: UnloadingPermission)(
    implicit hc: HeaderCarrier): Future[Option[Int]] =
    interchangeControlReferenceIdRepository
      .nextInterchangeControlReferenceId()
      .flatMap {
        interchangeControlReference =>
          {
            remarksService
              .build(userAnswers, unloadingPermission)
              .flatMap {
                unloadingRemarks =>
                  val meta: Meta = metaService.build(eori, interchangeControlReference)

                  val unloadingRemarksRequest: UnloadingRemarksRequest =
                    unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswers)

                  unloadingConnector
                    .post(arrivalId, unloadingRemarksRequest)
                    .flatMap(response => Future.successful(Some(response.status)))
                    .recover {
                      case ex =>
                        Logger.error(s"$ex")
                        Some(SERVICE_UNAVAILABLE)
                    }
              }
          }
      }
      .recover {
        case ex =>
          Logger.error(s"$ex")
          None
      }

  def resubmit(arrivalId: ArrivalId, eori: EoriNumber, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[Int]] =
    unloadingRemarksMessageService.unloadingRemarksMessage(arrivalId) flatMap {
      case Some(unloadingRemarksRequest) =>
        getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest, eori, userAnswers) flatMap {
          case Some(updatedUnloadingRemarks) => unloadingConnector.post(arrivalId, updatedUnloadingRemarks).map(response => Some(response.status))
          case _                             => Future.successful(None)
        }
      case _ => Future.successful(None)
    }

  private[services] def getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest: UnloadingRemarksRequest,
                                                         eori: EoriNumber,
                                                         userAnswers: UserAnswers): Future[Option[UnloadingRemarksRequest]] =
    interchangeControlReferenceIdRepository
      .nextInterchangeControlReferenceId()
      .map {
        interchangeControlReference =>
          val meta: Meta = metaService.build(eori, interchangeControlReference)

          getResultOfControlCorrectedValue(userAnswers: UserAnswers) match {
            case Some((newValue, pointerIdentity: PointerIdentity)) =>
              val resultOfControl: Seq[ResultsOfControl] = unloadingRemarksRequest.resultOfControl.map {
                case differentValues: ResultsOfControlDifferentValues
                    if differentValues.pointerToAttribute.pointer == pointerIdentity &&
                      !differentValues.correctedValue.equals(newValue) =>
                  differentValues.copy(correctedValue = newValue)
                case roc: ResultsOfControl => roc
              }
              Some(unloadingRemarksRequest.copy(meta = meta, resultOfControl = resultOfControl))
            case _ =>
              userAnswers.get(DateGoodsUnloadedPage).map {
                date =>
                  val unloadingRemarks: Remarks = unloadingRemarksRequest.unloadingRemark match {
                    case y: RemarksNonConform => y.copy(unloadingDate = date)
                    case x                    => x
                  }
                  unloadingRemarksRequest.copy(meta = meta, unloadingRemark = unloadingRemarks)
              }
          }
      }

  private def getResultOfControlCorrectedValue(userAnswers: UserAnswers): Option[(String, PointerIdentity)] =
    userAnswers.get(VehicleNameRegistrationReferencePage) match {
      case Some(answer) => Some((answer, TransportIdentity))
      case _ =>
        userAnswers.get(TotalNumberOfPackagesPage) match {
          case Some(answer) => Some((answer.toString, NumberOfPackages))
          case _ =>
            userAnswers.get(TotalNumberOfItemsPage) match {
              case Some(answer) => Some((answer.toString, NumberOfItems))
              case _ =>
                userAnswers.get(GrossMassAmountPage) match {
                  case Some(answer) => Some((answer, GrossMass))
                  case _            => None
                }
            }
        }
    }
}
