#!/bin/bash

echo ""
echo "Applying migration TotalNumberOfItems"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/totalNumberOfItems                  controllers.TotalNumberOfItemsController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/totalNumberOfItems                  controllers.TotalNumberOfItemsController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeTotalNumberOfItems                        controllers.TotalNumberOfItemsController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeTotalNumberOfItems                        controllers.TotalNumberOfItemsController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "totalNumberOfItems.title = TotalNumberOfItems" >> ../conf/messages.en
echo "totalNumberOfItems.heading = TotalNumberOfItems" >> ../conf/messages.en
echo "totalNumberOfItems.checkYourAnswersLabel = TotalNumberOfItems" >> ../conf/messages.en
echo "totalNumberOfItems.error.nonNumeric = Enter your totalNumberOfItems using numbers" >> ../conf/messages.en
echo "totalNumberOfItems.error.required = Enter your totalNumberOfItems" >> ../conf/messages.en
echo "totalNumberOfItems.error.wholeNumber = Enter your totalNumberOfItems using whole numbers" >> ../conf/messages.en
echo "totalNumberOfItems.error.outOfRange = TotalNumberOfItems must be between {0} and {1}" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTotalNumberOfItemsUserAnswersEntry: Arbitrary[(TotalNumberOfItemsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TotalNumberOfItemsPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTotalNumberOfItemsPage: Arbitrary[TotalNumberOfItemsPage.type] =";\
    print "    Arbitrary(TotalNumberOfItemsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TotalNumberOfItemsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def totalNumberOfItems: Option[Row] = userAnswers.get(TotalNumberOfItemsPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"totalNumberOfItems.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(Literal(answer.toString)),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.TotalNumberOfItemsController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"totalNumberOfItems.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration TotalNumberOfItems completed"
