fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## iOS

### ios load_api_key

```sh
[bundle exec] fastlane ios load_api_key
```

Loads App Store Connect API Key

### ios test

```sh
[bundle exec] fastlane ios test
```

Run all tests

### ios increment_build

```sh
[bundle exec] fastlane ios increment_build
```

Bump build number to FASTLANE_BUILD_NUMBER

### ios build

```sh
[bundle exec] fastlane ios build
```

Build the app (incl. bumping build number)

### ios deploy_beta

```sh
[bundle exec] fastlane ios deploy_beta
```

Deploy a new beta to TestFlight

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
