#!/bin/bash

echo ""
echo "Applying migration AdditionalChangesinformation"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/additionalChangesinformation                        controllers.AdditionalChangesinformationController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/additionalChangesinformation                        controllers.AdditionalChangesinformationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeAdditionalChangesinformation                  controllers.AdditionalChangesinformationController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeAdditionalChangesinformation                  controllers.AdditionalChangesinformationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "additionalChangesinformation.title = additionalChangesinformation" >> ../conf/messages.en
echo "additionalChangesinformation.heading = additionalChangesinformation" >> ../conf/messages.en
echo "additionalChangesinformation.checkYourAnswersLabel = additionalChangesinformation" >> ../conf/messages.en
echo "additionalChangesinformation.error.required = Enter additionalChangesinformation" >> ../conf/messages.en
echo "additionalChangesinformation.error.length = AdditionalChangesinformation must be 100 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAdditionalChangesinformationUserAnswersEntry: Arbitrary[(AdditionalChangesinformationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AdditionalChangesinformationPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAdditionalChangesinformationPage: Arbitrary[AdditionalChangesinformationPage.type] =";\
    print "    Arbitrary(AdditionalChangesinformationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AdditionalChangesinformationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def additionalChangesinformation: Option[Row] = userAnswers.get(AdditionalChangesinformationPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"additionalChangesinformation.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.AdditionalChangesinformationController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"additionalChangesinformation.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AdditionalChangesinformation completed"
