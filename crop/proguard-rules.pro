# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.* { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.* { *; }
-dontwarn dagger.hilt.**

# Keep Hilt generated code
-keep,allowobfuscation,allowshrinking class com.fahad.newtruelovebyfahad.** { *; }
-keep,allowobfuscation,allowshrinking class dagger.hilt.** { *; }
-keep,allowobfuscation,allowshrinking class javax.inject.** { *; }
-keep,allowobfuscation,allowshrinking class androidx.hilt.** { *; }

# Keep specific Dagger/Hilt classes completely
-keep class com.fahad.newtruelovebyfahad.DaggerMyApp_HiltComponents_** { *; }
-keep class com.fahad.newtruelovebyfahad.MyApp_HiltComponents_** { *; }
-keep class dagger.** { *; }

# Keep any classes used with @Inject, @Module, etc.
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <methods>;
}