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

package base

import cats.data.NonEmptyList
import config.FrontendAppConfig
import controllers.actions._
import models.{GoodsItem, MovementReferenceNumber, Packages, ProducedDocument, TraderAtDestinationWithoutEori, UnloadingPermission, UserAnswers}
import org.mockito.Mockito
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{bind, Injector}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.UnloadingPermissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nunjucks.NunjucksRenderer

trait SpecBase
    extends FreeSpec
    with MustMatchers
    with GuiceOneAppPerSuite
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with BeforeAndAfterEach {

  override def beforeEach {
    Mockito.reset(mockRenderer, mockUnloadingPermissionService)
  }

  val mrn: MovementReferenceNumber = MovementReferenceNumber("19", "GB", "1234567890123")

  def emptyUserAnswers = UserAnswers(mrn, Json.obj())

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  val mockRenderer: NunjucksRenderer = mock[NunjucksRenderer]

  val mockUnloadingPermissionService: UnloadingPermissionService = mock[UnloadingPermissionService]

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[UnloadingPermissionService].toInstance(mockUnloadingPermissionService),
        bind[DataRetrievalActionProvider].toInstance(new FakeDataRetrievalActionProvider(userAnswers)),
        bind[NunjucksRenderer].toInstance(mockRenderer)
      )

  protected lazy val traderWithoutEori =
    TraderAtDestinationWithoutEori("The Luggage Carriers", "225 Suedopolish Yard,", "SS8 2BB", ",", "GB")

  protected lazy val packages = Packages(Some("Ref."), "BX", Some(1), None)

  protected lazy val producedDocuments = ProducedDocument("235", Some("Ref."), None)

  protected lazy val goodsItemMandatory = GoodsItem(
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

  protected val unloadingPermission = UnloadingPermission(
    movementReferenceNumber = "19IT02110010007827",
    transportIdentity       = None,
    transportCountry        = None,
    grossMass               = "1000",
    numberOfItems           = 1,
    numberOfPackages        = 1,
    traderAtDestination     = traderWithoutEori,
    presentationOffice      = "GB000060",
    seals                   = None,
    goodsItems              = NonEmptyList(goodsItemMandatory, Nil)
  )
}
