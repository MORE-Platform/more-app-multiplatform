#!/bin/bash

# This script runs the fastlane setup_google_services lane for both iOS and Android.

echo "Setting up Google Services for iOS..."
(cd iosApp/faslane && fastlane setup_google_services)

echo "Setting up Google Services for Android..."
(cd androidApp/fastlane && fastlane setup_google_services)

echo "Google Services setup completed."
