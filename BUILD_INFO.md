# Building and Running the Project

## Prerequisites

- JDK 11 or higher
- Android SDK (for Android builds)
- Xcode (for iOS builds)
- Gradle

## Common Commands

Build the entire project:
```
./gradlew build
```

Clean the build:
```
./gradlew clean
```

## Android

Build the Android application:
```
./gradlew :composeApp:assembleDebug
```

Build a release version:
```
./gradlew :composeApp:assembleRelease
```

Install and run on a connected device or emulator:
```
./gradlew :composeApp:installDebug
```

## iOS

Build iOS frameworks:
```
./gradlew :composeApp:linkDebugFrameworkIosFat
```

For release:
```
./gradlew :composeApp:linkReleaseFrameworkIosFat
```

To run the iOS app, open the Xcode project in the `/iosApp` directory and run it from Xcode.

## Desktop

Build the desktop application:
```
./gradlew :composeApp:desktopJar
```

Run the desktop application:
```
./gradlew :composeApp:desktopRun
```

## Web (WebAssembly)

Run the web application in development mode:
```
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Build for production:
```
./gradlew :composeApp:wasmJsBrowserProductionWebpack
```

Run in production mode:
```
./gradlew :composeApp:wasmJsBrowserProductionRun
```

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.