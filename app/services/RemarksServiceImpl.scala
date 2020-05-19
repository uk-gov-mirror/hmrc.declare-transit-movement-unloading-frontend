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
import derivable.DeriveNumberOfSeals
import models.messages._
import models.{Index, Seals, UnloadingPermission, UserAnswers}
import pages.{ChangesToReportPage, DateGoodsUnloadedPage, NewSealNumberPage}

class RemarksServiceImpl extends RemarksService {

  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Either[RemarksFailure, Remarks] =
    userAnswers.get(DateGoodsUnloadedPage) match {
      case Some(date) =>
        unloadingPermission.seals match {
          case Some(Seals(_, unloadingPermissionSeals)) if unloadingPermissionSeals.nonEmpty => {

            val numberOfSeals = userAnswers.get(DeriveNumberOfSeals).getOrElse(0)
            val listOfSeals   = List.range(0, numberOfSeals).map(Index(_))

            if (haveSealsChanged(unloadingPermissionSeals, listOfSeals, userAnswers)) {
              Right(
                RemarksNonConform(
                  stateOfSeals    = Some(0),
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate   = date,
                  resultOfControl = Nil
                ))
            } else {
              userAnswers
                .get(ChangesToReportPage)
                .map {
                  unloadingRemarks =>
                    Right(
                      RemarksNonConform(
                        stateOfSeals    = Some(1),
                        unloadingRemark = Some(unloadingRemarks),
                        unloadingDate   = date,
                        resultOfControl = Nil
                      )
                    )
                }
                .getOrElse(Right(RemarksConformWithSeals(date)))
            }
          }
          case None => {
            userAnswers.get(DeriveNumberOfSeals) match {
              case Some(_) =>
                Right(
                  RemarksNonConform(
                    stateOfSeals    = None,
                    unloadingRemark = userAnswers.get(ChangesToReportPage),
                    unloadingDate   = date,
                    resultOfControl = Nil
                  )
                )
              case None => {
                userAnswers
                  .get(ChangesToReportPage)
                  .map {
                    unloadingRemarks =>
                      Right(
                        RemarksNonConform(
                          stateOfSeals    = None,
                          unloadingRemark = Some(unloadingRemarks),
                          unloadingDate   = date,
                          resultOfControl = Nil
                        )
                      )
                  }
                  .getOrElse(Right(RemarksConform(date)))
              }
            }

          }

        }

      case None =>
        Left(FailedToFindUnloadingDate)
    }

  //TODO: Can this be improved to be more readable
  private def haveSealsChanged(originalSeals: Seq[String], updatedSeals: List[Index], userAnswers: UserAnswers) = {

    val filtered: Seq[(String, Index)] = originalSeals
      .zip(updatedSeals)
      .filter(
        x => {
          userAnswers.get(NewSealNumberPage(x._2)) match {
            case Some(userAnswersValue) => x._1 != userAnswersValue
            case None                   => false
          }
        }
      )

    filtered.nonEmpty
  }
}

trait RemarksService {

  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Either[RemarksFailure, Remarks]
}
