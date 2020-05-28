#!/bin/bash

echo ""
echo "Applying migration DateGoodsUnloaded"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/dateGoodsUnloaded                  controllers.DateGoodsUnloadedController.onPageLoad(arrivalId: ArrivalId, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/dateGoodsUnloaded                  controllers.DateGoodsUnloadedController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeDateGoodsUnloaded                        controllers.DateGoodsUnloadedController.onPageLoad(arrivalId: ArrivalId, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeDateGoodsUnloaded                        controllers.DateGoodsUnloadedController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "dateGoodsUnloaded.title = DateGoodsUnloaded" >> ../conf/messages.en
echo "dateGoodsUnloaded.heading = DateGoodsUnloaded" >> ../conf/messages.en
echo "dateGoodsUnloaded.hint = For example, 12 11 2007" >> ../conf/messages.en
echo "dateGoodsUnloaded.checkYourAnswersLabel = DateGoodsUnloaded" >> ../conf/messages.en
echo "dateGoodsUnloaded.error.required.all = Enter the dateGoodsUnloaded" >> ../conf/messages.en
echo "dateGoodsUnloaded.error.required.two = The dateGoodsUnloaded" must include {0} and {1} >> ../conf/messages.en
echo "dateGoodsUnloaded.error.required = The dateGoodsUnloaded must include {0}" >> ../conf/messages.en
echo "dateGoodsUnloaded.error.invalid = Enter a real DateGoodsUnloaded" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDateGoodsUnloadedUserAnswersEntry: Arbitrary[(DateGoodsUnloadedPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DateGoodsUnloadedPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDateGoodsUnloadedPage: Arbitrary[DateGoodsUnloadedPage.type] =";\
    print "    Arbitrary(DateGoodsUnloadedPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DateGoodsUnloadedPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def dateGoodsUnloaded: Option[Row] = userAnswers.get(DateGoodsUnloadedPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"dateGoodsUnloaded.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(Literal(answer.format(dateFormatter))),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.DateGoodsUnloadedController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"dateGoodsUnloaded.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration DateGoodsUnloaded completed"
