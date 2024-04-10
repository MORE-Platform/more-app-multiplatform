# More App Multiplatform

This document provides detailed description of how to install, prepare and contribute to the App development as part of the "MORE"-Project.


<!-- GETTING STARTED -->
## Getting Started

This is an example of how you can set up the project locally.
To get a local copy up and running follow these steps.

### Prerequisites

The following prerequisites list contains all the needed software in order to be able to build the app locally.

**Disclaimer**: To write iOS-specific code and run an iOS application on a
simulated or real device, you'll need a Mac with macOS.
This cannot be performed on other operating systems, such as Microsoft Windows. This is an Apple requirement.

It's recommended that you install the latest stable versions for compatibility and better performance. In order to build the iOS application the version of **iOS** should be at least 14.

* [Android Studio](https://developer.android.com/studio)
* [XCode](https://apps.apple.com/us/app/xcode) (Must be of version 14.0 or higher)
* [Command Line Tools](https://developer.apple.com/downloads/)
* [JDK 19](https://www.oracle.com/java/technologies/downloads/)
* [Gradle 8.2+](https://gradle.org)

Also, it's recommended to install the following plugins in the Android Studio directly:

[Kotlin Multiplatform Mobile](https://kotlinlang.org/docs/multiplatform-mobile-plugin-releases.html) - In Android Studio, select **Settings/Preferences | Plugins**, search **Marketplace** for Kotlin Multiplatform Mobile, and then install it.

[Kotlin plugin](https://kotlinlang.org/docs/releases.html#update-to-a-new-release) - The Kotlin plugin is bundled with each Android Studio release. However, it still needs to be updated to the latest version to avoid compatibility issues.

To update the plugin, on the Android Studio welcome screen, select **Plugins | Installed**. Click **Update** next to Kotlin. You can also check the Kotlin version in **Tools | Kotlin | Configure Kotlin Plugin Updates**.
The Kotlin plugin should be compatible with the Kotlin Multiplatform Mobile plugin. Refer to the [compatibility table](https://kotlinlang.org/docs/multiplatform-mobile-plugin-releases.html#release-details).



### Installation

The following is an instruction on how to install and configure the project on your local device.


1.  Clone the repo:
    ```sh
    git clone https://github.com/MORE-Platform/more-app-multiplatform.git
    ```
2. Open the project in Android Studio.
3. Make sure to sync project with the Gradle Files. Click **File | Sync Project with Gradle Files** and wait until it's done.
4. Now we can build the project. Go to the terminal and perform the following command in the root folder of the project:
    ```sh
    ./gradlew build
    ```
5. Now you should be good to go. You can create an emulator device and start your application.

### Troubleshooting with KDoctor

To make sure everything works as expected, install and run the KDoctor tool:
1. In the Android Studio terminal or your command-line tool, run the following command to install the tool using Homebrew:
    ```sh
    brew install kdoctor
    ```
   If you don't have Homebrew yet, [install it](https://brew.sh/) or see the KDoctor [README](https://github.com/Kotlin/kdoctor#installation) for other ways to install it.

2. After the installation is completed, call KDoctor in the console:
    ```sh
    kdoctor
    ```

3. If KDoctor diagnoses any problems while checking your environment, review the output for issues and possible solutions:
* Fix any failed checks `([x])`. You can find problem descriptions and potential solutions after the `*` symbol.
* Check the warnings `([!])` and successful messages `([v])`. They may contain useful notes and tips, as well.

_You may ignore KDoctor's warnings regarding the CocoaPods installation. In this project, we use Swift Package Manager and not CocoaPods._



<!-- USAGE EXAMPLES -->
## Usage

### Emulator Configuration
In order to run your application you have to create an emulator device. Follow these steps to create an Android emulator:

1. Click **Device Manager** in the upper right corner, right next to the build symbol.
2. Click **Create device**.
3. Choose the device you would like to use as an emulator. **Important**: The device should have **Play Store** support! You can see it by the device being marked with a Play Store icon.
4. Choose a system image. It's recommended to use the **Tiramisu** release with the **API Level 33**.
5. Next verify configuration and the installation of the image will begin immediately.
6. Now you are all set to run your application on the configured Emulator!

### Running the App
After you have configured the emulator device for your project, you can run the application, which will start the emulator and install your application on it.
After that you can use the emulator to test the app.

Because **More App Multiplatform** supports iOS and Android, you can choose which application and the corresponding emulator you want to run.

#### Android App
1. In the **Run Configurations** choose **androidApp**.
2. In the **Available Devices** choose your configured **Emulator Device**.
3. Press **Run** arrow.

#### iOS App
1. In the **Run Configurations** choose **ios App**.
2. Press **Run** arrow.


## Project Architecture

The purpose of the Kotlin Multiplatform Mobile technology is unifying the development of applications with common logic for Android and iOS platforms.
To make this possible, it uses a mobile-specific structure of Kotlin Multiplatform projects.

To view the complete structure of your mobile multiplatform project, switch the view from **Android** to **Project**.

### Root Project

The root project is a Gradle project that holds the shared module and the Android application as its subprojects.
They are linked together via the [Gradle multi-project mechanism](https://docs.gradle.org/current/userguide/multi_project_builds.html).

![App architecture](https://kotlinlang.org/docs/images/basic-project-structure.png)

The iOS application is produced from an Xcode project. It's stored in a separate directory within the root project. Xcode uses its own build system; thus, the iOS application project isn't connected with other parts of the Multiplatform Mobile project via Gradle. Instead, it uses the shared module as an external artifact – framework. For details on integration between the shared module and the iOS application, see [iOS application](https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html#ios-application).

The root project does not hold source code. You can use it to store global configuration in its `build.gradle(.kts)` or `gradle.properties`, for example, add repositories or define global configuration variables.

### Shared Module

Shared module contains the core application logic used in both Android and iOS target platforms: classes, functions, and so on.
This is a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform-get-started.html) module that compiles into an Android library and an iOS framework. It uses the Gradle build system with the Kotlin Multiplatform plugin applied and has targets for Android and iOS.

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
However, to implement the same logic on Android and iOS, you sometimes need to write two platform-specific versions of it.
To handle such cases, Kotlin offers the expect/actual mechanism.
The source code of the shared module is organized in three source sets accordingly:

* `commonMain` stores the code that works on both platforms, including the `expect` declarations
* `androidMain` stores Android-specific parts, including `actual` implementations
* `iosMain` stores iOS-specific parts, including `actual` implementations


## Troubleshooting
* Use **Wipe Data** on your emulator device.
* Use **Sync Project with Gradle Files** in the **File** tab.
* Use **Invalidate Caches** in the **File** tab.

### Operating System Management of MORE Apps
As the mobile phone operating systems are attempting to limit unintended application data access and background activities for privacy and battery preservation, as well as for overall performance reasons, please consider checking through operating system settings that:
* MORE can run without battery saving limitations as a background application
* Access to the required sensing APIs is available (particularly GPS, accellerometry and wider physical activity according to your study needs) and ideally not limited to episodes of active (foreground) application use only
* If pairing with further sensing devices is intended, please assure that Bluetooth is enabled with appropriate access rights and consider resetting the connection or manually linking devices through operating system functionalities if the integrated pairing in MORE fails

## Useful links
* https://kotlinlang.org/docs/multiplatform-mobile-setup.html
* https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html
* https://kotlinlang.org/docs/multiplatform-mobile-integrate-in-existing-app.html
* https://kotlinlang.org/docs/multiplatform-mobile-ktor-sqldelight.html



## License

Apache 2.0 with Commons Clause; see LICENSE.txt for further details


<!-- CONTACT -->
## Contact

Ludwig Boltzmann Institute for Digital Health and Prevention - [more-health.at](https://more-health.at/) -  more@dhp.lbg.ac.at

