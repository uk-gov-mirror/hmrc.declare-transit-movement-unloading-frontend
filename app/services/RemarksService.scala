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
import java.time.LocalDate

import derivable.DeriveNumberOfSeals
import models.messages._
import models.{Index, Seals, UnloadingPermission, UserAnswers}
import pages._
import queries.SealsQuery

class RemarksServiceImpl extends RemarksService {

  import RemarksServiceImpl._

  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Response =
    userAnswers.get(DateGoodsUnloadedPage) match {

      case Some(date) =>
        implicit val unloadingDate: LocalDate = date

        Seq(unloadingPermissionContainsSeals(userAnswers), unloadingPermissionDoesNotContainSeals(userAnswers))
          .reduce(_ orElse _)
          .apply(unloadingPermission.seals)

      case None => Left(FailedToFindUnloadingDate)
    }

  private def unloadingPermissionContainsSeals(userAnswers: UserAnswers)(implicit unloadingDate: LocalDate): PartialFunction[Option[Seals], Response] = {
    case Some(Seals(_, unloadingPermissionSeals)) if unloadingPermissionSeals.nonEmpty => {

      if (haveSealsChanged(unloadingPermissionSeals, userAnswers) ||
          sealsUnreadable(userAnswers.get(CanSealsBeReadPage)) ||
          sealsBroken(userAnswers.get(AreAnySealsBrokenPage))) {
        Right(
          RemarksNonConform(
            stateOfSeals    = Some(0),
            unloadingRemark = userAnswers.get(ChangesToReportPage),
            unloadingDate   = unloadingDate,
            resultOfControl = Nil
          ))
      } else {
        userAnswers.get(ChangesToReportPage) match {
          case Some(unloadingRemarks) =>
            Right(
              RemarksNonConform(
                stateOfSeals    = Some(1),
                unloadingRemark = Some(unloadingRemarks),
                unloadingDate   = unloadingDate,
                resultOfControl = Nil
              )
            )
          case None => Right(RemarksConformWithSeals(unloadingDate))
        }
      }
    }
  }

  private def unloadingPermissionDoesNotContainSeals(userAnswers: UserAnswers)(implicit unloadingDate: LocalDate): PartialFunction[Option[Seals], Response] = {
    case None => {
      userAnswers.get(DeriveNumberOfSeals) match {
        case Some(_) =>
          Right(
            RemarksNonConform(
              stateOfSeals    = None,
              unloadingRemark = userAnswers.get(ChangesToReportPage),
              unloadingDate   = unloadingDate,
              resultOfControl = Nil
            )
          )
        case None => {
          userAnswers.get(ChangesToReportPage) match {
            case Some(unloadingRemarks) =>
              Right(
                RemarksNonConform(
                  stateOfSeals    = None,
                  unloadingRemark = Some(unloadingRemarks),
                  unloadingDate   = unloadingDate,
                  resultOfControl = Nil
                )
              )
            case None => Right(RemarksConform(unloadingDate))
          }
        }
      }
    }
  }
}

object RemarksServiceImpl {

  type Response = Either[RemarksFailure, Remarks]

  private def sealsUnreadable(canSealsBeReadPage: Option[Boolean]): Boolean =
    !canSealsBeReadPage.getOrElse(true)

  private def sealsBroken(areAnySealsBrokenPage: Option[Boolean]): Boolean =
    areAnySealsBrokenPage.getOrElse(false)

  private def haveSealsChanged(originalSeals: Seq[String], userAnswers: UserAnswers): Boolean =
    userAnswers.get(SealsQuery).exists {
      userSeals =>
        userSeals.sorted != originalSeals.sorted
    }

}

trait RemarksService {
  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Either[RemarksFailure, Remarks]
}
