/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import controllers.actions._
import forms.DateGoodsUnloadedFormProvider
import javax.inject.Inject
import models.{ArrivalId, Mode}
import navigation.NavigatorUnloadingPermission
import pages.DateGoodsUnloadedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.UnloadingPermissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.DateInput

import scala.concurrent.{ExecutionContext, Future}

class DateGoodsUnloadedController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: NavigatorUnloadingPermission,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: DateGoodsUnloadedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  unloadingPermissionService: UnloadingPermissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private def form = formProvider()

  def onPageLoad(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = (identify andThen getData(arrivalId) andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(DateGoodsUnloadedPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      val viewModel = DateInput.localDate(preparedForm("value"))

      val json = Json.obj(
        "form"      -> preparedForm,
        "mode"      -> mode,
        "mrn"       -> request.userAnswers.mrn,
        "arrivalId" -> arrivalId,
        "date"      -> viewModel
      )

      renderer.render("dateGoodsUnloaded.njk", json).map(Ok(_))
  }

  def onSubmit(arrivalId: ArrivalId, mode: Mode): Action[AnyContent] = (identify andThen getData(arrivalId) andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val viewModel = DateInput.localDate(formWithErrors("value"))

            val json = Json.obj(
              "form"      -> formWithErrors,
              "mode"      -> mode,
              "mrn"       -> request.userAnswers.mrn,
              "arrivalId" -> arrivalId,
              "date"      -> viewModel
            )

            renderer.render("dateGoodsUnloaded.njk", json).map(BadRequest(_))
          },
          value =>
            for {
              updatedAnswers      <- Future.fromTry(request.userAnswers.set(DateGoodsUnloadedPage, value))
              _                   <- sessionRepository.set(updatedAnswers)
              unloadingPermission <- unloadingPermissionService.getUnloadingPermission(arrivalId)
            } yield {
              Redirect(navigator.nextPage(DateGoodsUnloadedPage, mode, updatedAnswers, unloadingPermission))
          }
        )
  }
}
