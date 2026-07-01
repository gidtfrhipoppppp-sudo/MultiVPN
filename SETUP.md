# Android Development Setup Guide for MultiVPN

## 📱 Environment Setup

### 1. System Requirements
- **OS**: Windows 10+, macOS 10.14+, or Linux (Ubuntu 18.04+)
- **RAM**: Minimum 8GB (16GB recommended)
- **Disk Space**: 30GB+ for SDK and emulators
- **Java**: JDK 11 or higher

### 2. Install Android Studio
1. Download from [developer.android.com](https://developer.android.com/studio)
2. Run the installer and follow instructions
3. Complete the initial setup wizard
4. Install SDK components:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android Emulator
   - Android SDK Platform-Tools

### 3. Configure Environment Variables

#### On Windows
```cmd
ANDROID_SDK_ROOT=C:\Users\YourUsername\AppData\Local\Android\Sdk
JAVA_HOME=C:\Program Files\Java\jdk-11
```

#### On macOS/Linux
```bash
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export JAVA_HOME=/usr/libexec/java_home -v 11
export PATH=$PATH:$ANDROID_SDK_ROOT/tools:$ANDROID_SDK_ROOT/platform-tools
```

### 4. Create Android Virtual Device (AVD)

1. Open Android Studio
2. Tools → Device Manager → Create device
3. Select device (e.g., Pixel 6)
4. Select system image (API 34)
5. Configure AVD settings
6. Finish and start emulator

## 🔧 Project Setup

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/MultiVPN.git
cd MultiVPN
```

### 2. Build the Project
```bash
./gradlew build
```

### 3. Sync Dependencies
Android Studio will automatically sync, or manually:
```bash
./gradlew sync
```

## 🚀 Development Workflow

### Debug Build
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Run on Emulator
```bash
./gradlew runDebug
```

### Run Tests
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumentation tests
./gradlew build --info            # Detailed build info
```

## 📝 Code Style

### Kotlin Style Guide
- Use Kotlin official style guide
- Line length: 100 characters max
- Indentation: 4 spaces
- Class naming: PascalCase
- Function naming: camelCase
- Constant naming: UPPER_SNAKE_CASE

### Example
```kotlin
class VpnService : Service() {
    companion object {
        private const val TAG = "VpnService"
    }
    
    private var vpnConnection: VpnConnection? = null
    
    fun startVpn() {
        vpnConnection?.connect()
    }
}
```

## 🔐 Security Setup

### 1. Generate Keystore
```bash
keytool -genkey -v -keystore ~/MultiVPN.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias multivpn
```

### 2. Configure Signing in build.gradle
```gradle
signingConfigs {
    release {
        storeFile file("~/MultiVPN.keystore")
        storePassword System.getenv("STORE_PASSWORD")
        keyAlias "multivpn"
        keyPassword System.getenv("KEY_PASSWORD")
    }
}
```

### 3. Set Environment Variables
```bash
export STORE_PASSWORD="your_store_password"
export KEY_PASSWORD="your_key_password"
```

## 🔥 Firebase Setup (Optional)

1. Create Firebase project at [firebase.google.com](https://firebase.google.com)
2. Add Android app to Firebase project
3. Download `google-services.json`
4. Place in `app/` directory
5. Firebase dependencies are already configured in build.gradle

## 🐛 Debugging

### Enable Android Debug Bridge (ADB)
```bash
adb devices                    # List connected devices
adb logcat                     # View device logs
adb shell                      # Open shell on device
adb install app.apk            # Install APK
adb uninstall com.multivpn.app # Uninstall app
```

### Profiling
1. Android Studio → Profiler
2. Monitor CPU, Memory, Network
3. Record traces for analysis

## 📊 Performance Optimization

### Memory Profiling
```bash
./gradlew :app:debugAndroidTestMemoryMetrics
```

### Build Performance
```bash
./gradlew build --profile
# View report: build/reports/profile
```

## 🔄 Continuous Integration

### GitHub Actions Example
Create `.github/workflows/android.yml`:
```yaml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Gradle
        run: ./gradlew build
```

## 📚 Useful Commands

```bash
# Clean build
./gradlew clean

# Build APK
./gradlew assembleDebug
./gradlew assembleRelease

# Build Bundle (for Play Store)
./gradlew bundleRelease

# Run tests with coverage
./gradlew testDebugUnitTestCoverage

# Lint
./gradlew lint

# Generate documentation
./gradlew javadoc

# View dependency tree
./gradlew dependencies
```

## 🆘 Troubleshooting

### Gradle Sync Issues
```bash
./gradlew clean
./gradlew sync --refresh-dependencies
```

### Build Cache Issues
```bash
./gradlew build --no-build-cache
```

### IDE Caching Issues
File → Invalidate Caches → Invalidate and Restart

### Emulator Issues
```bash
emulator -avd YourAVDName -wipe-data
```

## 📖 Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Jetpack](https://developer.android.com/jetpack)
- [Material Design](https://material.io/design)

---

**Last Updated**: 2026-06-30
