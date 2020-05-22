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

import controllers.actions._
import javax.inject.Inject
import models.requests.OptionalDataRequest
import models.{MovementReferenceNumber, NormalMode, UserAnswers}
import navigation.Navigator
import pages.UnloadingGuidancePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.MrnQuery
import renderer.Renderer
import repositories.SessionRepository
import services.UnloadingPermissionServiceImpl
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class UnloadingGuidanceController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  navigator: Navigator,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  unloadingPermissionServiceImpl: UnloadingPermissionServiceImpl
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(arrivalId)).async {
    implicit request =>
      unloadingPermissionServiceImpl.getUnloadingPermission(arrivalId) flatMap {
        case Some(unloadingPermission) =>
          val mrn = MovementReferenceNumber(unloadingPermission.movementReferenceNumber).get
          request.userAnswers match {
            case Some(userAnswers) if (userAnswers.mrn != null) =>
              renderPage(arrivalId, mrn, navigator.nextPage(UnloadingGuidancePage, NormalMode, userAnswers).url).map(Ok(_))
            case _ =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(mrn, mrn)).set(MrnQuery, mrn))
                _              <- sessionRepository.set(updatedAnswers)
              } yield renderPage(arrivalId, mrn, navigator.nextPage(UnloadingGuidancePage, NormalMode, updatedAnswers).url).map(Ok(_))
          }
        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }

  private def renderPage(arrivalId: MovementReferenceNumber, mrn: MovementReferenceNumber, nextPageUrl: String)(implicit request: OptionalDataRequest[_]) = {
    val json = Json.obj("arrivalId" -> arrivalId, "mrn" -> mrn, "nextPageUrl" -> nextPageUrl, "mode" -> NormalMode)
    renderer.render("unloadingGuidance.njk", json)
  }
}
