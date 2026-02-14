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
-keep class com.project.common.repo.room.helper.** { *; }
-keep class com.project.common.repo.room.helper.FavouriteTypeConverter.** { *; }
-keep class com.project.common.repo.room.helper.FavouriteTypeConverter.** { <fields>; }
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Signature
-keep class com.google.gson.* { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type
-keep class com.project.common.repo.room.helper.** { <fields>; }
-keepclassmembers class com.project.common.repo.room.helper.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable
# Fixes Tasks class not found error
-keep class com.google.android.gms.tasks.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class com.project.common.repo.room.** { *; }
-keep class com.project.common.repo.room.** { <fields>; }
-keepclassmembers class com.project.common.repo.room.** { *; }

-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Frame { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$AllTag { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Category { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$File { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$File { <fields>; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Data { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Hashtag { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Tag { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Tag1 { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Tag2 { *; }


-keep class com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices { *; }
-keep class com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices.* { *; }
-keep class com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices.* { <fields>; }
-keep class com.apollographql.apollo3.api.EnumType { *; }

-keep class com.fahad.newtruelovebyfahad.GetMainScreenQuery$Frame { *; }
-keep class com.fahad.newtruelovebyfahad.GetMainScreenQuery$Child { *; }
-keep class com.fahad.newtruelovebyfahad.GetMainScreenQuery$ChildCategory { *; }
-keep class com.fahad.newtruelovebyfahad.GetMainScreenQuery$Companion { *; }
-keep class com.fahad.newtruelovebyfahad.GetMainScreenQuery$Data { *; }
-keep class com.apollographql.apollo3.api.EnumType { *; }

-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$Effect { *; }
-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$Data { *; }
-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$ParentCategory { *; }
-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$Tag { *; }
-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$Tag1 { *; }

-keep class com.fahad.newtruelovebyfahad.GetStickersQuery$Sticker { *; }
-keep class com.fahad.newtruelovebyfahad.GetStickersQuery$Data { *; }
-keep class com.fahad.newtruelovebyfahad.GetStickersQuery$ParentCategory { *; }
-keep class com.fahad.newtruelovebyfahad.GetStickersQuery$Tag { *; }

-dontwarn java.lang.invoke.StringConcatFactory
-keep class androidx.datastore.*.** {*;}
-keep class com.project.common.datastore.** {*;}
-keepclassmembers class com.project.common.datastore.** {*;}
-keepattributes *Annotation*
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

-dontwarn com.project.common.R$drawable

-dontwarn com.project.common.R$color

# Hilt
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