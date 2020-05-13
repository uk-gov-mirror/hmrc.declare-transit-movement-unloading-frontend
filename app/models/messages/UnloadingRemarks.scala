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

package models.messages

import cats.data.NonEmptyList
import models.{GoodsItem, Seals, TraderAtDestination}

case class UnloadingRemarks(movementReferenceNumber: String,
                            transportIdentity: Option[String],
                            transportCountry: Option[String],
                            numberOfItems: Int,
                            numberOfPackages: Int,
                            grossMass: String, //TODO: Does this need to be BigDecimal
                            traderAtDestination: TraderAtDestination,
                            presentationOffice: String,
                            seals: Option[Seals], // If stateOfSeals is 1 or None, this is optional (otherwise mandatory)
                            goodsItems: NonEmptyList[GoodsItem],
                            unloadingRemark: Remarks //TODO: Should we set this here
)

object UnloadingRemarks {

  val transportIdentityLength  = 27
  val transportIdentityCountry = 2
  val numberOfItemsLength      = 5
  val numberOfPackagesLength   = 7
  val presentationOfficeLength = 8
}
