#!/bin/bash

echo ""
echo "Applying migration UnloadingSummary"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/unloadingSummary                       controllers.UnloadingSummaryController.onPageLoad(arrivalId: ArrivalId)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "unloadingSummary.title = unloadingSummary" >> ../conf/messages.en
echo "unloadingSummary.heading = unloadingSummary" >> ../conf/messages.en

echo "Migration UnloadingSummary completed"
