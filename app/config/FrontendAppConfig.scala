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

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import models.ArrivalId
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration) {

  lazy val appName = configuration.get[String]("appName")

  lazy val contactHost: String             = configuration.get[Service]("microservice.services.contact-frontend").baseUrl
  lazy val contactFrontendUrl: String      = configuration.get[Service]("microservice.services.contact-frontend").fullServiceUrl
  val contactFormServiceIdentifier: String = "CTCTraders"

  val trackingConsentUrl: String = configuration.get[String]("microservice.services.tracking-consent-frontend.url")
  val gtmContainer: String       = configuration.get[String]("microservice.services.tracking-consent-frontend.gtm.container")

  val analyticsToken: String         = configuration.get[String](s"google-analytics.token")
  val analyticsHost: String          = configuration.get[String](s"google-analytics.host")
  val betaFeedbackUrl                = s"$contactFrontendUrl/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"$contactFrontendUrl/beta-feedback-unauthenticated"
  val signOutUrl: String             = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")

  lazy val loginUrl: String                            = configuration.get[String]("microservice.services.auth.login")
  lazy val loginContinueUrl: String                    = configuration.get[String]("microservice.services.auth.loginContinue")
  lazy val enrolmentKey: String                        = configuration.get[String]("microservice.services.auth.enrolmentKey")
  lazy val enrolmentIdentifierKey: String              = configuration.get[String]("microservice.services.auth.enrolmentIdentifierKey")
  lazy val enrolmentProxyUrl: String                   = configuration.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl
  lazy val enrolmentManagementFrontendEnrolUrl: String = configuration.get[String]("urls.enrolmentManagementFrontendEnrolUrl")

  lazy val loginHmrcServiceUrl: String = configuration.get[String]("urls.loginHmrcService")

  lazy val nctsEnquiriesUrl: String = configuration.get[String]("urls.nctsEnquiries")
  lazy val timeoutSeconds: String   = configuration.get[String]("session.timeoutSeconds")
  lazy val countdownSeconds: String = configuration.get[String]("session.countdownSeconds")

  private val manageTransitMovementsHost = configuration.get[String]("manage-transit-movements-frontend.host")
  val manageTransitMovementsUrl          = s"$manageTransitMovementsHost/manage-transit-movements/"

  lazy val referenceDataUrl: String = configuration.get[Service]("microservice.services.reference-data").fullServiceUrl

  lazy val arrivalsBackend: String        = configuration.get[Service]("microservice.services.arrivals-backend").fullServiceUrl
  lazy val arrivalsBackendBaseUrl: String = configuration.get[Service]("microservice.services.arrivals-backend").baseUrl

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

}
