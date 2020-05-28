#!/bin/bash

echo ""
echo "Applying migration VehicleNameRegistrationReference"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/vehicleNameRegistrationReference                        controllers.VehicleNameRegistrationReferenceController.onPageLoad(arrivalId: ArrivalId, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/vehicleNameRegistrationReference                        controllers.VehicleNameRegistrationReferenceController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeVehicleNameRegistrationReference                  controllers.VehicleNameRegistrationReferenceController.onPageLoad(arrivalId: ArrivalId, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeVehicleNameRegistrationReference                  controllers.VehicleNameRegistrationReferenceController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "vehicleNameRegistrationReference.title = vehicleNameRegistrationReference" >> ../conf/messages.en
echo "vehicleNameRegistrationReference.heading = vehicleNameRegistrationReference" >> ../conf/messages.en
echo "vehicleNameRegistrationReference.checkYourAnswersLabel = vehicleNameRegistrationReference" >> ../conf/messages.en
echo "vehicleNameRegistrationReference.error.required = Enter vehicleNameRegistrationReference" >> ../conf/messages.en
echo "vehicleNameRegistrationReference.error.length = VehicleNameRegistrationReference must be 100 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVehicleNameRegistrationReferenceUserAnswersEntry: Arbitrary[(VehicleNameRegistrationReferencePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[VehicleNameRegistrationReferencePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVehicleNameRegistrationReferencePage: Arbitrary[VehicleNameRegistrationReferencePage.type] =";\
    print "    Arbitrary(VehicleNameRegistrationReferencePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(VehicleNameRegistrationReferencePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def vehicleNameRegistrationReference: Option[Row] = userAnswers.get(VehicleNameRegistrationReferencePage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"vehicleNameRegistrationReference.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.VehicleNameRegistrationReferenceController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"vehicleNameRegistrationReference.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration VehicleNameRegistrationReference completed"
