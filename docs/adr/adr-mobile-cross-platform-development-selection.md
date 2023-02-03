# Mobile Application Development - Kotlin Multiplatform Mobile

**Created: 26.01.2023**

**Last Updated: 26.01.2023**

**State**: DECIDED

(
- OPEN: Initial state. Collecting data.
- IN PROGRESS: Discussion is ongoing.
- RFC:  A proposed decision has been made and now everyone can comment on it.
- DECIDED. Final state. A decision has been made and everyone had a fair chance to contribute.
  )

---
**Date: 26.01.2023**


**Participants**: @janoliver20, @dan0bar, @ja-fra

## Introduction

In summer 2022 we started the development More project including two native apps, starting with the Android app written in Kotlin. 
We explored the possibilities of Android and its API's. 
Now, half a year later when we're about to start the iOS implementation, we revisit the original decision to re-evaluate the requirements and options from today's perspective.

We are evaluating the expected effort to implement the native iOS app next to the Android app compared with the implementation of a hybrid/multiplatform application. We are explicitly targeting for a solution that helps us maintaining support for _both platfoms with a syncronized feature-set_.


## Original ADRs

Originally we defined three separate [ADRs](https://github.com/MORE-Platform/more-app-android/tree/main/docs/adr):
- [ADR More Mobile Device OS Selection](https://github.com/MORE-Platform/more-app-android/blob/main/docs/adr/adr-1_device-os-version-selection-android.md): For the Android OS selection, where we decided to use a minimum Android target of 10.
- [Mobile Application Development - Android](https://github.com/MORE-Platform/more-app-android/blob/main/docs/adr/adr-2_mobile-development-android.md): For the Mobile Application development, where we decided to develop a native app, due to the greater availability of API's and the better efficiency.
- [ADR Selection of Mobile Development Frameworks - Android](https://github.com/MORE-Platform/more-app-android/blob/main/docs/adr/adr-3_mobile-development-apis-and-sdks-android.md): For the mobile development frameworks on Android, where we decided to go with the native frameworks provided by Google itself.

## Analyzed Options

There are different options to develop an app for both platforms. It can be categorized into native and mobile cross-platform development.

- iOS Native
- Android Native
- Flutter
- Kotlin Multiplatform Mobile
- React-Native
- PWA

### Native
**Android is developed with Kotlin**

**iOS is developed with Swift**

#### Pros:

- Android and iOS native apps are as performant, energy-, memory- and storage efficient as the system possibly allows it to be, due to its native integration and therefore massive optimizations.
- Direct access to native APIs, SDKs, sensor data and more, with no need for additional frameworks.
- Native and utmost control over the apps UI/UX experience.
- Bugs and security updates are instantly available as soon as they are released.
- New APIs and SDKs are available as soon as they become available on their platforms.
- APIs and SDKs will always be maintained by the creators and don't rely on single open-source contributors.

#### Cons:

- Code is not reusable and has to be rewritten for each platform.
- More maintenance, since each update has to developed separately for each platform.


Cross-Platform apps are built to run on multiple different platforms, including desktop systems and more.
There are a few different frameworks worth considering:


### Flutter
Flutter is a cross-platform development tool to built apps for iOS, Android and Web.
It's built by Google and the language used is Dart. It is still a very new environment, but it grows in popularity.

#### Pros:

- Hot reloading allows for instant changes in the UI while the application is running
- There is only a single Code base. Only one application has to be built.
- Faster development
- Easier maintenance

#### Cons:

- Only a small adoption rate
- Large app sizes
- Lack of Third-party Libraries. Many basic features have to be implemented by hand. Also, many libraries developed open-source by single contributors tend to be abandoned.
- Issues with iOS. There are many bugs and new features use a long time to be adopted, including security updates.
- Dart is still in development, therefore it is missing a lot of features, even basic ones. It also isn't used a lot in other domains than flutter.
- Flutter has no native way to access sensor data of the devices. It still needs a library for that, which relies on the open-source community to take care.
- [Flutter apps use much more energy and memory to their native counterparts.](https://medium.com/swlh/flutter-vs-react-native-vs-native-deep-performance-comparison-990b90c11433)
- It still needs knowledge about Android development in Kotlin or Java to develop Android specific features.

### Kotlin Multiplatform Mobile:
Kotlin Native is a cross-platform development tool to build mobile, desktop and web apps.
Written with Kotlin, it just creates a shared module, which is included into the native apps.

#### Pros:
- A shared core library can be developed for iOS and Android
- Most of the code can be shared and therefore most of the code only has to be developed once, if a good architecture is provided
- Apps have complete access to all native APIs and frameworks.
- UI and app behaviour is completely native
- Excellent interoperability with Java and Kotlin
- Most of the code can be used and edited with Java/Kotlin knowledge only

### Cons:
- Knowledge about iOS Swift and SwiftUI still needed
- Only works for iOS 14.1 and above
- Views cannot be shared, have to be created on each platform


### React Native
React Native is a framework, which can create views for both iOS, Android and PWAs, it is written is JavaScript.

#### Pros:
- Cross-platform development: Can develop for both iOS and Android using a single codebase, saving time and resources compared to native development.
- Faster development: Faster development cycles as hot-reloading allows for quicker testing and iteration.
- Large community: Backed by Facebook and has a large, supportive community of developers.
- Reusable code: Code can be reused across different platforms, reducing the amount of code that needs to be written.

#### Cons:
- Not direct access to all platform features
- Limited Customization: Limited customization options compared to native development as it uses pre-built components.
- Dependence on third-party libraries: Some functionality may require the use of third-party libraries, which can lead to compatibility issues.
- Debugging can be challenging: Debugging can be more difficult than in native development, especially in case of complex issues.
- Updating: Updating to the latest version can sometimes be challenging and require significant code changes.
- Heterogeneous look and feel: Although it aims to provide a native experience, the look and feel can still be slightly different across platforms, leading to a heterogeneous user experience.

### PWA
Progressive Web App (PWA) tries to package a website into a web app, which can be installed on devices and used without an internet connection and without the browser interface.

#### Pros:
- Cross-platform compatibility: Can run on any device with a modern browser, providing a consistent user experience across devices.
- Offline capabilities: PWAs can cache data and still be used offline, providing a better user experience and increased reliability.
- Easy deployment: PWAs are easy to deploy and update, with no need for app store approval.
- Increased discoverability: PWAs are easily discoverable via search engines and can be shared via URLs, making them more accessible to users.

#### Cons:
- Limited access to native device functionality: PWAs have limited access to native device functionality, such as the camera or accelerometer.
- Browser compatibility issues: PWAs may not work optimally on all browsers, leading to compatibility issues for some users.
- User adoption: Users may not be familiar with PWAs and may need to be educated on how to use them.
- Limited distribution channels: PWAs have limited distribution channels compared to native apps, which can make it harder to reach a wide audience.
- Limited monetization options: PWAs currently have limited monetization options compared to native apps, making it harder to generate revenue.


## Candidate Feature Matrix

|                                             | Android Native                            | iOS Native                                                        | React-Native                                        | Flutter                                                                                           | Kotlin Native                                                                                     | PWA                                                                                                                                       |
|---------------------------------------------|-------------------------------------------|-------------------------------------------------------------------|-----------------------------------------------------|---------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| Technical access to sensor                  | Yes                                       | Yes                                                               | Yes(Accelerometer, Gyroscope, Magnetometer)         | Yes(Accelerometer, Gyroscope, Magnetometer)                                                       | Yes                                                                                               | No Bluetooth on iOS: https://developer.mozilla.org/en-US/docs/Web/API/Web_Bluetooth_API, <br/> Accelerometer, Gyroscope and GPS available |
| Background Tasks and Modes                  | Yes (limited to foreground services only) | Some components can run in the backhround (GPS, CMSensorRecorder) | Only Android (limited)                              | Can be accessed via native implementation (Android limited, iOS with some frameworks and sensors) | Can be accessed via native implementation (Android limited, iOS with some frameworks and sensors) | Not on iOS                                                                                                                                |
| Data transformation, batching and streaming | Yes                                       | Yes                                                               | Yes                                                 | Yes                                                                                               | Yes                                                                                               | Data storage on iOS limited to 50 MB                                                                                                      |
| Acceptance in App-Stores                    | Yes                                       | Yes                                                               | Yes                                                 | Yes                                                                                               | Yes                                                                                               | Yes                                                                                                                                       |
| Notifications                               | Yes                                       | Yes                                                               | Yes                                                 | Yes                                                                                               | Yes                                                                                               | Not supported on iOS                                                                                                                      |
| Implement Native Code                       | Yes                                       | Yes                                                               | No                                                  | Yes (limited)                                                                                     | Yes                                                                                               | No                                                                                                                                        |
| Usage of iOS SensorKit                      | n/a                                       | Yes                                                               | No                                                  | Maybe                                                                                             | Yes                                                                                               | No                                                                                                                                        |
| Packaging                                   | APK or AAB                                | Non (Only direct distribution via App Store or TestFlight         | APK/AAB and on iOS only via App Store or TestFlight | APK/AAB and on iOS only via App Store or TestFlight                                               | APK/AAB and on iOS only via App Store or TestFlight                                               | iOS and Android: PWABuilder.com for distribution only                                                                                     |
| Modularization                              | Modules                                   | Frameworks                                                        |                                                     |                                                                                                   | Modules and Frameworks                                                                            |                                                                                                                                           |
  |                                             |                                           |                                                                   |                                                     |                                                                                                   | Needs minimum of iOS 14.1 and Android 9                                                           |                                                                                                                                           | 


## Decision

We come to the conclusion that **Kotlin Multiplatform Mobile** is the best option at this point:

* Kotlin-Experience is wide spread in the development team
* We can re-use significant code-elements from the existing App
* The shared module for the business-logic allows us to use synergies compared with the original approach.
* We can still make use of platform-native features if specific requirements.


---