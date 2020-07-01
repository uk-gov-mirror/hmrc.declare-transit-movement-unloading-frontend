#!/bin/bash

echo ""
echo "Applying migration UnloadingRemarksRejection"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/unloadingRemarksRejection                       controllers.UnloadingRemarksRejectionController.onPageLoad(arrivalId: ArrivalId)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "unloadingRemarksRejection.title = unloadingRemarksRejection" >> ../conf/messages.en
echo "unloadingRemarksRejection.heading = unloadingRemarksRejection" >> ../conf/messages.en

echo "Migration UnloadingRemarksRejection completed"
