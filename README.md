# UMM Participant App

This document provides detailed description of how to install, prepare and contribute to the App
development as part of the "MORE"-Project.


<!-- GETTING STARTED -->

## Getting Started

This is an example of how you can set up the project locally.
To get a local copy up and running follow these steps.

### Prerequisites

The following prerequisites list contains all the needed software in order to be able to build the
app locally.

**Disclaimer**: To write iOS-specific code and run an iOS application on a
simulated or real device, you'll need a Mac with macOS.
This cannot be performed on other operating systems, such as Microsoft Windows. This is an Apple
requirement.

It's recommended that you install the latest stable versions for compatibility and better
performance. In order to build the iOS application the version of **iOS** should be at least 14.

* [Android Studio](https://developer.android.com/studio)
* [XCode](https://apps.apple.com/us/app/xcode) (Must be of version 16.0 or higher)
* [Command Line Tools](https://developer.apple.com/downloads/)
* [JDK 21](https://www.oracle.com/java/technologies/downloads/)
* [Gradle 8.14.3](https://gradle.org)

It's recommended to install Xcode via `xcodes` and `aria2` as this is faster and more flexible in
downloading specific versions of Xcode, including beta releases.
To download the latest Xcode version simply enter `xcodes install --latest --experimental-unxip` or
`xcodes install --latest-prerelease --experimental-unxip` for a beta version.
Both `xcodes` and `aria2` are available via Homebrew `brew install xcodes aria2`.

Also, it's recommended to install the following plugins in the Android Studio directly:

[Kotlin Multiplatform Mobile](https://kotlinlang.org/docs/multiplatform-mobile-plugin-releases.html) -
In Android Studio, select **Settings/Preferences | Plugins**, search **Marketplace** for Kotlin
Multiplatform Mobile, and then install it.

[Kotlin plugin](https://kotlinlang.org/docs/releases.html#update-to-a-new-release) - The Kotlin
plugin is bundled with each Android Studio release. However, it still needs to be updated to the
latest version to avoid compatibility issues.

To update the plugin, on the Android Studio welcome screen, select **Plugins | Installed**. Click *
*Update** next to Kotlin. You can also check the Kotlin version in **Tools | Kotlin | Configure
Kotlin Plugin Updates**.
The Kotlin plugin should be compatible with the Kotlin Multiplatform Mobile plugin. Refer to
the [compatibility table](https://kotlinlang.org/docs/multiplatform-mobile-plugin-releases.html#release-details).

### Installation

The following is an instruction on how to install and configure the project on your local device.

1. Clone the repo:
   ```sh
   git clone https://github.com/MORE-Platform/more-app-multiplatform.git
   ```
2. Open the project in Android Studio.
3. Make sure to sync project with the Gradle Files. Click **File | Sync Project with Gradle Files**
   and wait until it's done.
4. This project requires **JDK 11** or later. To build the project you need to set your **runtime to
   JDK 11 or later**, and then under **Project Structure** --> **Modules** set in either *
   *androidApp** and **shared** the **Source Compatibility** and the **Target Compatibility** to at
   least **$JavaVersion.VERSION_11**.
5. After being upgraded to JDK 11 or a later version, the Settings dialog in Android Studio can be
   accessed by pressing cmd + , on Mac or Ctrl + Alt + S on Windows/Linux. Then, navigate to "Build,
   Execution, Deployment > Build Tools > Gradle". The JDK location can be set within that section.
6. Now we can build the project. Go to the terminal and perform the following command in the root
   folder of the project:
    ```sh
    ./gradlew build
    ```
7. Now you should be good to go. You can create an emulator device and start your application.

### Troubleshooting with KDoctor

To make sure everything works as expected, install and run the KDoctor tool:

1. In the Android Studio terminal or your command-line tool, run the following command to install
   the tool using Homebrew:
    ```sh
    brew install kdoctor
    ```
   If you don't have Homebrew yet, [install it](https://brew.sh/) or see the
   KDoctor [README](https://github.com/Kotlin/kdoctor#installation) for other ways to install it.

2. After the installation is completed, call KDoctor in the console:
    ```sh
    kdoctor
    ```

3. If KDoctor diagnoses any problems while checking your environment, review the output for issues
   and possible solutions:

* Fix any failed checks `([x])`. You can find problem descriptions and potential solutions after the
  `*` symbol.
* Check the warnings `([!])` and successful messages `([v])`. They may contain useful notes and
  tips, as well.

_You may ignore KDoctor's warnings regarding the CocoaPods installation. In this project, we use
Swift Package Manager and not CocoaPods._



<!-- USAGE EXAMPLES -->

## CI/CD Pipeline

This project uses GitHub Actions for continuous integration and continuous deployment. The pipeline
is split into two workflows:

### Build Workflow

The build workflow (`build.yml`) runs on every push to any branch and on pull requests. It only
builds the apps without deploying them.

**Trigger:**

- Push to any branch (excluding tags)
- Pull requests

**Jobs:**

- iOS: Builds the iOS app using fastlane
- Android: Builds the Android app using fastlane

### Deploy Beta Workflow

The deploy beta workflow (`deploy-beta.yml`) runs only when a tag with the format `x.x.x` (semantic
versioning) is pushed. It builds the apps and deploys them to TestFlight (iOS) and Google Play
Beta (Android).

**Trigger:**

- Push of a tag matching the pattern `[0-9]+.[0-9]+.[0-9]+` (e.g., `1.2.3`)

The tag pattern (e.g., `1.2.3`) directly represents the new app deployment version. The workflow
extracts this version from the tag and sets it as the `FASTLANE_BUILD_NUMBER` environment variable,
which is then used by fastlane to set the build number and version in both iOS and Android apps.

**Jobs:**

- iOS: Builds the iOS app and deploys it to TestFlight
- Android: Builds the Android app and deploys it to Google Play Beta

### Fastlane Integration

This project uses fastlane for automating the build and deployment processes for both iOS and
Android apps.

#### iOS Fastlane

The iOS fastlane configuration includes the following lanes:

- `increment_build`: Bumps build number and version to `FASTLANE_BUILD_NUMBER`
- `build`: Builds the app for App Store, including code signing setup
- `deploy_beta`: Deploys a new beta to TestFlight (calls `increment_build` and `build`, then uploads
  to TestFlight)

The iOS build process uses Xcode 16.4 and creates a temporary keychain for secure code signing. It
supports multiple app targets, including notification service extensions.

#### Android Fastlane

The Android fastlane configuration includes the following lanes:

- `test`: Runs all tests
- `build`: Builds the Android app (debug version)
- `deploy_beta`: Builds a release version and deploys it to Google Play Beta

The Android build process has several important features:

- **Version Code Calculation**: For release builds, the version code is calculated using a formula
  that converts semantic versioning (e.g., 4.0.22) to a 5-digit code:
  `major × 10⁴ + minor × 10² + patch`. For example, version 4.0.22 becomes 40022. The system also
  checks the latest version code from Google Play and increments it by 1, using the maximum of these
  two values to ensure the version code is always increasing.

- **AAB Format**: The Android app is built as an Android App Bundle (AAB) for release, not an APK.

- **Firebase Integration**: When not running in CI mode, the build process supports optional
  Firebase App Distribution for testing.

### Environment Variables

To run the pipeline, you need to set up the following environment variables:

#### iOS Environment Variables

**Secrets:**

- `FASTLANE_TEAM_ID`: Your Apple Developer Team ID
- `APPLE_CONNECT_KEY_ID`: App Store Connect API Key ID
- `APPLE_CONNECT_ISSUER_ID`: App Store Connect API Issuer ID
- `APPLE_CONNECT_KEY_CONTENT`: App Store Connect API Key content (base64 encoded)
- `FASTLANE_MATCH_SECRET`: Password for match repository
- `MATCH_AUTH`: Basic authorization for match Git repository

**Variables:**

- `APP_IDENTIFIERS`: Comma-separated list of app bundle identifiers (e.g., "
  io.redlink.umm.blendedcare.io.redlink.umm.blendedcare.More-Notification-Service-Extension")
- `TARGETS`: Comma-separated list of Xcode targets corresponding to the app identifiers (e.g., "
  BlendedCare,BlendedCare-Notification-Service-Extension")
- `FASTLANE_IOS_BUILD_SCHEME`: Xcode scheme to build
- `FASTLANE_BUILD_NUMBER`: Build number (set automatically from tag in deploy workflow)
- `APPLE_CONNECT_KEY_IS_BASE64`: Whether the APPLE_CONNECT_KEY_CONTENT is base64 encoded (
  true/false)
- `CODE_SIGN_IDENTITY`: The code signing identity to use (e.g., "iPhone Distribution")

#### Android Environment Variables

**Secrets:**

- `GOOGLE_PLAY_KEY_FILE`: Path to Google Play key file
- `GOOGLE_PLAY_KEY_IN_BASE64`: Google Play key file content (base64 encoded)
- `ANDROID_KEYSTORE_BASE64`: Android keystore file (base64 encoded)
- `ANDROID_KEYSTORE_PASSWORD`: Password for the Android keystore
- `ANDROID_KEY_ALIAS`: Alias for the Android signing key
- `ANDROID_KEY_PASSWORD`: Password for the Android signing key
- `FIREBASE_APP_ID`: (Optional) Firebase App ID for Firebase App Distribution

**Variables:**

- `FASTLANE_BUILD_NUMBER`: Build number (set automatically from tag in deploy workflow)
- `PACKAGE`: Android package name (e.g., "ac.at.lbg.dhp.more")

### Running the Pipeline Under Other Accounts

To run the pipeline under your own account, follow these steps:

1. **Fork the repository** to your GitHub account.

2. **Set up the required secrets and variables** in your GitHub repository:
    - Go to your repository settings
    - Navigate to "Secrets and variables" > "Actions"
    - Add all the required secrets and variables listed above

3. **iOS-specific setup:**
    - Create an App Store Connect API key in your Apple Developer account
    - Set up a match repository for code signing
    - Update the bundle identifiers in the project to match your own

4. **Android-specific setup:**
    - Create a Google Play service account and download the key file
    - Create a keystore for signing your Android app
    - Update the package name in the project to match your own

5. **Trigger the workflows:**
    - For the build workflow: Push to any branch or create a pull request
    - For the deploy workflow: Create and push a tag with the format `x.x.x` (e.g.,
      `git tag 1.0.0 && git push origin 1.0.0`)

### Troubleshooting

- **iOS build fails**: Check that all iOS-related environment variables are set correctly and that
  your Apple Developer account has the necessary permissions. Ensure that the
  `APPLE_CONNECT_KEY_CONTENT` is properly base64 encoded and that the `APPLE_CONNECT_KEY_IS_BASE64`
  is set to true.
- **Android build fails**: Verify that the Android keystore and Google Play key are correctly
  encoded in base64. The Android build process expects both `ANDROID_KEYSTORE_BASE64` and
  `GOOGLE_PLAY_KEY_IN_BASE64` to be properly base64 encoded.
- **Deployment fails**: Ensure that the app identifiers match the ones in your Apple Developer
  account or Google Play Console. For iOS, make sure the `TARGETS` variable matches the app
  identifiers in the same order.
- **Version code issues**: If you encounter version code conflicts in Google Play, the system will
  automatically try to increment the version code based on the latest version in Google Play. If
  this fails, it will fall back to the calculated version code based on the semantic version.

## Usage

### Emulator Configuration

In order to run your application you have to create an emulator device. Follow these steps to create
an Android emulator:

1. Click **Device Manager** in the upper right corner, right next to the build symbol.
2. Click **Create device**.
3. Choose the device you would like to use as an emulator. **Important**: The device should have *
   *Play Store** support! You can see it by the device being marked with a Play Store icon.
4. Choose a system image. It's recommended to use the **Tiramisu** release with the **API Level 33
   **.
5. Next verify configuration and the installation of the image will begin immediately.
6. Now you are all set to run your application on the configured Emulator!

### Running the App

After you have configured the emulator device for your project, you can run the application, which
will start the emulator and install your application on it.
After that you can use the emulator to test the app.

Because **More App Multiplatform** supports iOS and Android, you can choose which application and
the corresponding emulator you want to run.

#### Android App

1. In the **Run Configurations** choose **androidApp**.
2. In the **Available Devices** choose your configured **Emulator Device**.
3. Press **Run** arrow.

#### iOS App

1. In the **Run Configurations** choose **ios App**.
2. Press **Run** arrow.

#### Local development with app, studymanager and gateway

Local setup together
with [more-studymanager-backend](https://github.com/MORE-Platform/more-studymanager-backend), [more-studymanager-frontend](https://github.com/MORE-Platform/more-studymanager-frontend)
and [more-datag-ateway](https://github.com/MORE-Platform/more-data-gateway).

##### Android App

The APK from the App Store isn't able to run against your local setup, because it doesn't support
it. To be able to run it with your local setup follow this step-by-step guide:

1. Open Android Studio

2. Go to AndroidManifest and add following line to <application .MoreApplication… (between line 36 &
    37)

```sh
    android:usesCleartextTraffic="true"
```

3. Run app from your AndroidStudio on your device or inside AndroidStudio with an Emulator.

4. Open your MoreApp on Device or Emulator and add following into your Endpoint (the pc and device
   have to be in the same WLAN), and you are good to go.

```sh
    http://<macadresse>:<gateway-port>/api/v1
```

##### iOS App

The IOS-App can be basically runs with any image, since it supports clear traffic. If you doesn't
have changes in the app, you could even run it directly against your local setup with the App-Store
Version.

## Project Architecture

The purpose of the Kotlin Multiplatform Mobile technology is unifying the development of
applications with common logic for Android and iOS platforms.
To make this possible, it uses a mobile-specific structure of Kotlin Multiplatform projects.

To view the complete structure of your mobile multiplatform project, switch the view from **Android
** to **Project**.

### Root Project

The root project is a Gradle project that holds the shared module and the Android application as its
subprojects.
They are linked together via
the [Gradle multi-project mechanism](https://docs.gradle.org/current/userguide/multi_project_builds.html).

![App architecture](https://kotlinlang.org/docs/images/basic-project-structure.png)

The iOS application is produced from an Xcode project. It's stored in a separate directory within
the root project. Xcode uses its own build system; thus, the iOS application project isn't connected
with other parts of the Multiplatform Mobile project via Gradle. Instead, it uses the shared module
as an external artifact – framework. For details on integration between the shared module and the
iOS application,
see [iOS application](https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html#ios-application).

The root project does not hold source code. You can use it to store global configuration in its
`build.gradle(.kts)` or `gradle.properties`, for example, add repositories or define global
configuration variables.

### Shared Module

Shared module contains the core application logic used in both Android and iOS target platforms:
classes, functions, and so on.
This is a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform-get-started.html) module
that compiles into an Android library and an iOS framework. It uses the Gradle build system with the
Kotlin Multiplatform plugin applied and has targets for Android and iOS.

```kotlin
plugins {
    kotlin("multiplatform") version "1.8.10"
    // ..
}

kotlin {
    android()
    ios()
}
```

#### Sources sets

The shared module contains the code that is common for Android and iOS applications.
However, to implement the same logic on Android and iOS, you sometimes need to write two
platform-specific versions of it.
To handle such cases, Kotlin offers the expect/actual mechanism.
The source code of the shared module is organized in three source sets accordingly:

* `commonMain` stores the code that works on both platforms, including the `expect` declarations
* `androidMain` stores Android-specific parts, including `actual` implementations
* `iosMain` stores iOS-specific parts, including `actual` implementations

#### Database changes

When making changes to the Database Schemas, please *make sure to increase the Database Schema
Version* in the `RealmDatabase.kt` file located unter
`shared/src/commonMain/kotlin/io.redlink.umm.participant/database`.

*If this version is not upgraded after a schema change, the app will crash on already deployed
systems!*

#### Deployment

Currently there is not automatic deployment. This should be implemented in near future, but until
then, these are the steps to ensure a proper deployment of new app versions:

1. Update the Version name and code of the Android App under `androidApp/build.gradle.kts`. The
   version code just needs to be incremented by 1, while the name is x.x.x (e.g. 4.0.26)
2. Update the Version in iOS under the `Target` `More` -> General
3. Update the `Bundle version` and `Bundle version string` under Info with the same system x.x.x (
   e.g. 4.0.26)

## Troubleshooting

* Use **Wipe Data** on your emulator device.
* Use **Sync Project with Gradle Files** in the **File** tab.
* Use **Invalidate Caches** in the **File** tab.

### Operating System Management of MORE Apps

As the mobile phone operating systems are attempting to limit unintended application data access and
background activities for privacy and battery preservation, as well as for overall performance
reasons, please consider checking through operating system settings that:

* MORE can run without battery saving limitations as a background application
* Access to the required sensing APIs is available (particularly GPS, accellerometry and wider
  physical activity according to your study needs) and ideally not limited to episodes of active (
  foreground) application use only
* If pairing with further sensing devices is intended, please assure that Bluetooth is enabled with
  appropriate access rights and consider resetting the connection or manually linking devices
  through operating system functionalities if the integrated pairing in MORE fails

## Useful links

* https://kotlinlang.org/docs/multiplatform-mobile-setup.html
* https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html
* https://kotlinlang.org/docs/multiplatform-mobile-integrate-in-existing-app.html
* https://kotlinlang.org/docs/multiplatform-mobile-ktor-sqldelight.html

## License

Apache 2.0 with Commons Clause; see LICENSE.txt for further details


<!-- CONTACT -->

## Contact

Ludwig Boltzmann Institute for Digital Health and
Prevention - [more-health.at](https://more-health.at/) - more@dhp.lbg.ac.at
