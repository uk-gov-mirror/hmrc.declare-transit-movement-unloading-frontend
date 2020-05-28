#!/bin/bash

echo ""
echo "Applying migration VehicleRegistrationCountry"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/vehicleRegistrationCountry                        controllers.VehicleRegistrationCountryController.onPageLoad(arrivalId: ArrivalId, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/vehicleRegistrationCountry                        controllers.VehicleRegistrationCountryController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeVehicleRegistrationCountry                  controllers.VehicleRegistrationCountryController.onPageLoad(arrivalId: ArrivalId, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeVehicleRegistrationCountry                  controllers.VehicleRegistrationCountryController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "vehicleRegistrationCountry.title = vehicleRegistrationCountry" >> ../conf/messages.en
echo "vehicleRegistrationCountry.heading = vehicleRegistrationCountry" >> ../conf/messages.en
echo "vehicleRegistrationCountry.checkYourAnswersLabel = vehicleRegistrationCountry" >> ../conf/messages.en
echo "vehicleRegistrationCountry.error.required = Enter vehicleRegistrationCountry" >> ../conf/messages.en
echo "vehicleRegistrationCountry.error.length = VehicleRegistrationCountry must be 100 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVehicleRegistrationCountryUserAnswersEntry: Arbitrary[(VehicleRegistrationCountryPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[VehicleRegistrationCountryPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVehicleRegistrationCountryPage: Arbitrary[VehicleRegistrationCountryPage.type] =";\
    print "    Arbitrary(VehicleRegistrationCountryPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(VehicleRegistrationCountryPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def vehicleRegistrationCountry: Option[Row] = userAnswers.get(VehicleRegistrationCountryPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"vehicleRegistrationCountry.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.VehicleRegistrationCountryController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"vehicleRegistrationCountry.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration VehicleRegistrationCountry completed"
