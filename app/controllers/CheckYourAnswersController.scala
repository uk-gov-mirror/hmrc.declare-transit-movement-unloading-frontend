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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import handlers.ErrorHandler
import models.MovementReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import renderer.Renderer
import services.{ReferenceDataService, UnloadingPermissionService, UnloadingRemarksService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.CheckYourAnswersViewModel
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  unloadingPermissionService: UnloadingPermissionService,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  referenceDataService: ReferenceDataService,
  errorHandler: ErrorHandler,
  unloadingRemarksService: UnloadingRemarksService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val redirectUrl: MovementReferenceNumber => Call =
    mrn => controllers.routes.ConfirmationController.onPageLoad(mrn)

  def onPageLoad(mrn: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      unloadingPermissionService.getUnloadingPermission(mrn).flatMap {
        case Some(unloadingPermission) => {
          referenceDataService.getCountryByCode(unloadingPermission.transportCountry).flatMap {
            transportCountry =>
              val viewModel = CheckYourAnswersViewModel(request.userAnswers, unloadingPermission, transportCountry)

              val answers: Seq[Section] = viewModel.sections

              renderer
                .render(
                  "check-your-answers.njk",
                  Json.obj("mrn" -> Json.toJson(mrn), "sections" -> Json.toJson(answers), "redirectUrl" -> redirectUrl(mrn).url)
                )
                .map(Ok(_))
          }
        }
        case _ => errorHandler.onClientError(request, BAD_REQUEST)
      }
  }

  def onSubmit(mrn: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      unloadingPermissionService.getUnloadingPermission(mrn).flatMap {
        case Some(unloadingPermission) => {
          //TODO: arrivalId needs pulling from uri
          unloadingRemarksService.submit(1, request.eoriNumber, request.userAnswers, unloadingPermission) flatMap {
            case Some(status) =>
              status match {
                case ACCEPTED     => Future.successful(Redirect(routes.ConfirmationController.onPageLoad(mrn)))
                case UNAUTHORIZED => errorHandler.onClientError(request, UNAUTHORIZED)
                case _            => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
              }
            case None => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
          }
        }
        case _ => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
      }
  }
}
