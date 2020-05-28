#!/bin/bash

echo ""
echo "Applying migration AdditionalChangesInformation"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/additionalChangesInformation                        controllers.AdditionalChangesInformationController.onPageLoad(arrivalId: ArrivalId, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/additionalChangesInformation                        controllers.AdditionalChangesInformationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeAdditionalChangesInformation                  controllers.AdditionalChangesInformationController.onPageLoad(arrivalId: ArrivalId, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeAdditionalChangesInformation                  controllers.AdditionalChangesInformationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "additionalChangesInformation.title = additionalChangesInformation" >> ../conf/messages.en
echo "additionalChangesInformation.heading = additionalChangesInformation" >> ../conf/messages.en
echo "additionalChangesInformation.checkYourAnswersLabel = additionalChangesInformation" >> ../conf/messages.en
echo "additionalChangesInformation.error.required = Enter additionalChangesInformation" >> ../conf/messages.en
echo "additionalChangesInformation.error.length = AdditionalChangesInformation must be 100 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAdditionalChangesInformationUserAnswersEntry: Arbitrary[(AdditionalChangesInformationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AdditionalChangesInformationPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAdditionalChangesInformationPage: Arbitrary[AdditionalChangesInformationPage.type] =";\
    print "    Arbitrary(AdditionalChangesInformationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AdditionalChangesInformationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def additionalChangesInformation: Option[Row] = userAnswers.get(AdditionalChangesInformationPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"additionalChangesInformation.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.AdditionalChangesInformationController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"additionalChangesInformation.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AdditionalChangesInformation completed"
