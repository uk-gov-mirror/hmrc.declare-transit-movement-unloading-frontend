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
import forms.ConfirmRemoveSealFormProvider
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.{ConfirmRemoveSealPage, NewSealNumberPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveSealController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ConfirmRemoveSealFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(NewSealNumberPage(index)) match {
          case Some(seal) =>
            val form = formProvider(seal)
            renderedPage(mrn, index, mode, form, seal).map(Ok(_))

          case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        }
    }

  private def renderedPage(mrn: MovementReferenceNumber, index: Index, mode: Mode, form: Form[Boolean], seal: String)(
    implicit request: DataRequest[AnyContent]): Future[Html] = {
    val json = Json.obj(
      "form"            -> form,
      "mode"            -> mode,
      "mrn"             -> mrn,
      "sealDescription" -> seal,
      "radios"          -> Radios.yesNo(form("value"))
    )

    renderer.render("confirmRemoveSeal.njk", json)
  }

  def onSubmit(mrn: MovementReferenceNumber, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(NewSealNumberPage(index)) match {
          case Some(seal) =>
            formProvider(seal)
              .bindFromRequest()
              .fold(
                formWithErrors => {
                  renderedPage(mrn, index, mode, formWithErrors, seal).map(BadRequest(_))
                },
                value =>
                  if (value) {
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.remove(NewSealNumberPage(index)))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(ConfirmRemoveSealPage, mode, updatedAnswers))
                  } else {
                    Future.successful(Redirect(navigator.nextPage(ConfirmRemoveSealPage, mode, request.userAnswers)))

                }
              )
        }
    }
}
