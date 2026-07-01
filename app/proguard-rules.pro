# ProGuard rules for MultiVPN
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep VPN-related classes
-keep class android.net.VpnService
-keep class * implements android.net.VpnService

# Keep Retrofit classes
-keep class retrofit2.** { *; }
-keepclasseswithmembers class retrofit2.** { *; }
-keepattributes Exceptions

# Keep OkHttp3 classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep model classes (adjust package name as needed)
-keep class com.multivpn.app.model.** { *; }
-keep class com.multivpn.app.data.** { *; }

# Keep Kotlin classes
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
-dontwarn kotlin.**

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Keep Room classes
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom application classes
-keep class * extends android.app.Application

# VPN-specific - keep helper classes
-keepclassmembers class * {
    *** *VPN*(...);
}

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
