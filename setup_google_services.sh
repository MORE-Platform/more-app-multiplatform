#!/bin/bash

# This script runs the fastlane setup_google_services lane for both iOS and Android.

# Use absolute path to project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Setting up Google Services for iOS..."
(cd "$PROJECT_ROOT/iosApp" && fastlane setup_google_services)

echo "Setting up Google Services for Android..."
(cd "$PROJECT_ROOT/androidApp" && fastlane setup_google_services)

echo "Google Services setup completed."
