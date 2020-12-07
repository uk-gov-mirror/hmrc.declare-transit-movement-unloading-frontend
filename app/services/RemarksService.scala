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

import com.google.inject.Inject
import derivable.DeriveNumberOfSeals
import models.messages._
import models.{Seals, UnloadingPermission, UserAnswers}
import pages._
import queries.SealsQuery

import scala.concurrent.Future

class RemarksServiceImpl @Inject()(resultOfControlService: ResultOfControlService) extends RemarksService {

  import RemarksServiceImpl._

  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Response =
    userAnswers.get(DateGoodsUnloadedPage) match {

      case Some(date) =>
        implicit val unloadingDate: LocalDate = date

        implicit val resultsOfControl: Seq[ResultsOfControl] = resultOfControlService.build(userAnswers)

        Seq(unloadingPermissionContainsSeals(userAnswers), unloadingPermissionDoesNotContainSeals(userAnswers))
          .reduce(_ orElse _)
          .apply(unloadingPermission.seals)

      case None =>
        Future.failed(new NoSuchElementException("date goods unloaded not found"))
    }

  private def unloadingPermissionContainsSeals(userAnswers: UserAnswers)(implicit unloadingDate: LocalDate,
                                                                         resultsOfControl: Seq[ResultsOfControl]): PartialFunction[Option[Seals], Response] = {
    case Some(Seals(_, unloadingPermissionSeals)) if unloadingPermissionSeals.nonEmpty => {

      if (haveSealsChanged(unloadingPermissionSeals, userAnswers) ||
          sealsUnreadable(userAnswers.get(CanSealsBeReadPage)) ||
          sealsBroken(userAnswers.get(AreAnySealsBrokenPage))) {
        Future.successful(
          RemarksNonConform(
            stateOfSeals    = Some(0),
            unloadingRemark = userAnswers.get(ChangesToReportPage),
            unloadingDate   = unloadingDate
          ))
      } else {
        Future.successful(
          RemarksConformWithSeals(
            unloadingRemark = userAnswers.get(ChangesToReportPage),
            unloadingDate   = unloadingDate
          )
        )
      }
    }

  }

  private def unloadingPermissionDoesNotContainSeals(
    userAnswers: UserAnswers)(implicit unloadingDate: LocalDate, resultsOfControl: Seq[ResultsOfControl]): PartialFunction[Option[Seals], Response] = {
    case None =>
      userAnswers.get(DeriveNumberOfSeals) match {
        case Some(_) =>
          Future.successful(
            RemarksNonConform(
              stateOfSeals    = None,
              unloadingRemark = userAnswers.get(ChangesToReportPage),
              unloadingDate   = unloadingDate
            )
          )
        case None =>
          Future.successful(
            RemarksConform(
              unloadingRemark = userAnswers.get(ChangesToReportPage),
              unloadingDate   = unloadingDate
            )
          )
      }
  }
}

object RemarksServiceImpl {

  type Response = Future[Remarks]

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
  //TODO: Is it better to return a Future or Either?
  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Future[Remarks]
}
