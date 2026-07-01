# MultiVPN - Android VPN Application

Professional Android VPN application built with modern development practices.

## 📋 Project Overview

MultiVPN is a feature-rich VPN application for Android that provides secure network connections with:
- Native Android implementation (Kotlin)
- Material Design UI
- Modern architecture patterns (MVVM, Repository pattern)
- Comprehensive dependency injection (Hilt)
- Local database support (Room)
- Firebase integration
- Production-ready security practices

## 🛠 Tech Stack

### Android & Kotlin
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Language**: Kotlin 1.9.10

### Architecture & Patterns
- **Pattern**: MVVM (Model-View-ViewModel)
- **Architecture**: Clean Architecture
- **Dependency Injection**: Hilt
- **Coroutines**: For asynchronous operations

### Libraries
- **UI**: AndroidX, Material Components
- **Database**: Room ORM
- **Networking**: Retrofit 2, OkHttp3
- **Logging**: Timber
- **Firebase**: Analytics, Crashlytics, Messaging
- **Testing**: JUnit, Mockito, Espresso, AndroidX Test

## 📦 Project Structure

```
MultiVPN/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/multivpn/app/
│   │   │   │   ├── ui/              # UI Components (Activities, Fragments)
│   │   │   │   ├── viewmodel/       # ViewModels
│   │   │   │   ├── domain/          # Domain layer (interfaces, use cases)
│   │   │   │   ├── data/            # Data layer (repositories, DAOs)
│   │   │   │   ├── vpn/             # VPN service implementation
│   │   │   │   ├── service/         # Background services
│   │   │   │   ├── receiver/        # Broadcast receivers
│   │   │   │   ├── di/              # Dependency injection modules
│   │   │   │   └── MultiVpnApplication.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/          # Layout files
│   │   │   │   ├── values/          # String, color, style resources
│   │   │   │   └── xml/             # XML configuration files
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                    # Unit tests
│   │   └── androidTest/             # Instrumentation tests
│   ├── build.gradle                 # App-level Gradle configuration
│   └── proguard-rules.pro          # ProGuard rules for code obfuscation
├── build.gradle                     # Root-level Gradle configuration
├── settings.gradle                  # Gradle settings
└── gradle.properties               # Gradle properties
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (Arctic Fox or newer)
- JDK 11 or higher
- Android SDK (API 34)
- Gradle 8.2.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/MultiVPN.git
   cd MultiVPN
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Click "Open an existing project"
   - Select the MultiVPN directory

3. **Sync Gradle files**
   - Android Studio will automatically sync Gradle files
   - Wait for the build to complete

4. **Configure local.properties** (if needed)
   ```properties
   sdk.dir=/path/to/Android/sdk
   ```

5. **Run the application**
   - Click "Run" or press `Shift + F10`
   - Select an emulator or connected device

## 🔧 Build Variants

The project supports two build variants:

### Debug Build
- Full logging enabled
- Debuggable APK
- No code obfuscation
- Optimized for development

### Release Build
- Code obfuscation with ProGuard
- Resource shrinking enabled
- Optimized for performance
- Production-ready

Build the release variant:
```bash
./gradlew assembleRelease
```

## 📱 Features

- ✅ VPN Connection Management
- ✅ Server Configuration Management
- ✅ Persistent Connection State
- ✅ Real-time Status Updates
- ✅ Error Handling & Recovery
- ✅ Local Database Support
- ✅ Firebase Analytics Integration
- ✅ Crash Reporting
- ✅ Push Notifications Support

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Run All Tests
```bash
./gradlew testDebugUnitTest connectedDebugAndroidTest
```

## 🔐 Security Features

- **Cleartext Traffic**: Disabled by default
- **Data Encryption**: Encrypted SharedPreferences for sensitive data
- **Code Obfuscation**: ProGuard rules configured
- **Dependency Updates**: Regular security updates
- **Permission Management**: Principle of least privilege
- **Network Security**: HTTPS only, certificate pinning ready

## 📋 Android Permissions

The app requires the following permissions:

### Network & VPN
- `INTERNET` - Internet access
- `BIND_VPN_SERVICE` - VPN service binding
- `CHANGE_NETWORK_STATE` - Network state management
- `CHANGE_WIFI_STATE` - WiFi state management
- `ACCESS_NETWORK_STATE` - Network access detection

### System
- `WAKE_LOCK` - Prevent sleep
- `FOREGROUND_SERVICE` - Background service
- `POST_NOTIFICATIONS` - Notifications (Android 13+)

See `AndroidManifest.xml` for full permission list.

## 🏗 Architecture

### Clean Architecture Layers

```
┌─────────────────────────┐
│   Presentation Layer    │  (UI, ViewModels)
├─────────────────────────┤
│    Domain Layer         │  (Use Cases, Interfaces)
├─────────────────────────┤
│    Data Layer           │  (Repositories, DAOs, Network)
└─────────────────────────┘
```

### MVVM Pattern
```
View (Activity/Fragment)
    ↕️
ViewModel
    ↕️
Repository
    ↕️
Data Sources (Database, Network, etc.)
```

## 📦 Dependencies

Key dependencies included:

- **AndroidX**: Latest AndroidX libraries
- **Kotlin**: Coroutines, Serialization
- **Networking**: Retrofit, OkHttp
- **Database**: Room ORM
- **DI**: Hilt Android
- **Reactive**: LiveData, Flow
- **Firebase**: BOM 32.7.0
- **Testing**: JUnit, Mockito, Espresso

## 🔄 Development Workflow

1. **Create feature branch**
   ```bash
   git checkout -b feature/your-feature
   ```

2. **Make changes**
   - Write code following Kotlin style guide
   - Add tests for new features
   - Update documentation

3. **Run tests**
   ```bash
   ./gradlew test
   ```

4. **Build the app**
   ```bash
   ./gradlew build
   ```

5. **Submit pull request**

## 📚 Documentation

- [Android Documentation](https://developer.android.com/)
- [Kotlin Guide](https://kotlinlang.org/docs/)
- [Architecture Components](https://developer.android.com/topic/libraries/architecture)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

## 🐛 Troubleshooting

### Gradle Build Issues
```bash
./gradlew clean
./gradlew build
```

### Sync Issues
- File → Invalidate Caches
- Rebuild Project

### Emulator Issues
- Use Android Studio's AVD Manager
- Ensure virtualization is enabled

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For support, email support@multivpn.com or open an issue in the repository.

## 🎯 Roadmap

- [ ] Multi-protocol support (OpenVPN, WireGuard)
- [ ] Advanced encryption options
- [ ] Custom DNS settings
- [ ] Server location optimization
- [ ] Traffic statistics dashboard
- [ ] Secure credential storage
- [ ] Split tunneling
- [ ] Automated kill switch

---

**Last Updated**: 2026-06-30  
**Maintainer**: MultiVPN Development Team
