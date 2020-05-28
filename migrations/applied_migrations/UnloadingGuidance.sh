#!/bin/bash

echo ""
echo "Applying migration UnloadingGuidance"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/unloadingGuidance                       controllers.UnloadingGuidanceController.onPageLoad(arrivalId: ArrivalId)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "unloadingGuidance.title = unloadingGuidance" >> ../conf/messages.en
echo "unloadingGuidance.heading = unloadingGuidance" >> ../conf/messages.en

echo "Migration UnloadingGuidance completed"
