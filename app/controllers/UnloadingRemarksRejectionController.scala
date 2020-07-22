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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.ArrivalId
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.UnloadingRemarksRejectionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import viewModels.UnloadingRemarksRejectionViewModel

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksRejectionController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  appConfig: FrontendAppConfig,
  service: UnloadingRemarksRejectionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val Log: Logger = Logger(getClass)

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      service.unloadingRemarksRejectionMessage(arrivalId) flatMap {
        case Some(rejectionMessage) if rejectionMessage.errors.length == 1 =>
          UnloadingRemarksRejectionViewModel(rejectionMessage.errors.head, arrivalId) match {
            case Some(viewModel) =>
              def json: JsObject =
                Json.obj(
                  "sections"   -> Json.toJson(viewModel.sections),
                  "contactUrl" -> appConfig.nctsEnquiriesUrl
                )
              renderer.render("unloadingRemarksRejection.njk", json).map(Ok(_))
            case _ => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
          }
        case Some(rejectionMessage) if rejectionMessage.errors.length > 1 =>
          def json: JsObject =
            Json.obj(
              "errors"                     -> rejectionMessage.errors,
              "contactUrl"                 -> appConfig.nctsEnquiriesUrl,
              "declareUnloadingRemarksUrl" -> routes.IndexController.onPageLoad(arrivalId).url
            )

          renderer.render("unloadingRemarksMultipleErrorsRejection.njk", json).map(Ok(_))
        case _ =>
          Log.debug("service.UnloadingRemarksRejectionMessage is None")
          Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
      }
  }
}
