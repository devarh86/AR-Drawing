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
-keep class com.project.sticker.data.model.** { *; }
-keep class com.project.frame_placer.data.model.** { *; }
-keep class com.project.sticker.data.enum_classes.** { *; }
-keep class com.project.filter.data.model.** { *; }
-keep class com.project.filter.data.enum_classes.** { *; }
-keep class com.project.text.data.model.** { *; }
-keep class com.project.text.data.enum_classes.** { *; }
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class retrofit2.** { *; }
-keep class com.example.ads.crosspromo.api.retrofit.model.*
-keepclassmembers class com.project.sticker.data.model.** { *; }
-keepclassmembers class com.project.text.data.model.** { *; }
-keep class com.project.sticker.data.model.** { <fields>; }
-keep class com.project.text.data.model.** { <fields>; }
-keep class com.google.gson.* { *; }
-keep class com.google.gson.reflect.TypeToken { *; }

-dontobfuscate
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes Annotation

-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

-keep class com.example.ads.crosspromo.api.retrofit.model.*
-keep class com.example.ads.crosspromo.api.retrofit.**{*;}
-keep class androidx.lifecycle.LiveData { *; }

-keep public class com.google.android.gms.** { public protected *; }

# Firebase Core
-keepattributes *Annotation*
-keepclassmembers class com.google.firebase.** { *; }
-keepclassmembers enum com.google.firebase.** { *; }
-keep public class com.google.firebase.** { *; }
-keepclassmembers class * {
    @com.google.firebase.** *;
}

-keep class com.example.ads.** { *; }
-keep class com.example.analytics.** { *; }
-keep class com.example.inapp.** { *; }

-keepclassmembers class com.google.android.datatransport.runtime.** {
    static ** INSTANCE;
}

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

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.datatransport.runtime.** { *; }
-keepnames class com.google.android.datatransport.runtime.** { *; }

# Keep Crashlytics classes
-keep class com.google.firebase.crashlytics.** { *; }

# Keep AdServices classes
-keep class android.adservices.** { *; }
-keep class com.google.android.datatransport.runtime.DaggerTransportRuntimeComponent$TransportRuntimeComponentImpl.* { *; }

# Firebase Installations
-keep class com.google.firebase.installations.** { *; }
-keepclassmembers class com.google.firebase.installations.** { *; }
-keepclassmembers enum com.google.firebase.installations.** { *; }

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
 -keep,allowoptimization,allowshrinking,allowobfuscation class <3>

 # With R8 full mode generic signatures are stripped for classes that are not kept.
 -keep,allowobfuscation,allowshrinking class retrofit2.Response
# Play Core Proguard Rules: Play In-app Review

-keep class com.google.android.play.core.review.ReviewManager {
  public com.google.android.gms.tasks.Task requestReviewFlow();
  public com.google.android.gms.tasks.Task launchReviewFlow(android.app.Activity, com.google.android.play.core.review.ReviewInfo);
}

-keepnames class com.google.android.play.core.review.ReviewInfo

-keep class com.google.android.play.core.review.ReviewManagerFactory {
  <init>();

  public static com.google.android.play.core.review.ReviewManager create(android.content.Context);
}

-keep class com.google.android.play.core.review.testing.FakeReviewManager {
  public <init>(android.content.Context);
  public com.google.android.gms.tasks.Task requestReviewFlow();
  public com.google.android.gms.tasks.Task launchReviewFlow(android.app.Activity, com.google.android.play.core.review.ReviewInfo);
}

-keep class com.google.android.play.core.review.model.ReviewErrorCode {
    public static int NO_ERROR;
    public static int PLAY_STORE_NOT_FOUND;
    public static int INVALID_REQUEST;
    public static int INTERNAL_ERROR;
}

-keep class com.google.android.play.core.review.ReviewException {
    public int getErrorCode();
}

-keep class com.project.common.repo.room.helper.FavouriteTypeConverter { *; }
-keep class androidx.datastore.*.** {*;}
-keep class com.project.filter.datastore.** {*;}
-keepclassmembers class com.project.filter.datastore.** {*;}
-keep class com.project.sticker.datastore.** {*;}
-keepclassmembers class com.project.sticker.datastore.** {*;}
-keep class com.project.frame_placer.datastore.** {*;}
-keepclassmembers class com.project.frame_placer.datastore.** {*;}

-keep class com.project.common.repo.room.helper.** { *; }
-keep class com.project.common.repo.room.helper.** { <fields>; }
-keepclassmembers class com.project.common.repo.room.helper.** { *; }


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}


-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type


# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

##---------------End: proguard configuration for Gson  ----------



-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Frame { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$AllTag { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Data { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Hashtag { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Tag { *; }
-keep class com.fahad.newtruelovebyfahad.GetFeatureScreenQuery$Tag1 { *; }

-keep class com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery$Frame { *; }
-keep class com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery$Data { *; }
-keep class com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery$Category { *; }
-keep class com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery$Screen { *; }

-keep class com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices { *; }
-keep class com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices.* { *; }
-keep class com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices.* { <fields>; }
-keep class com.apollographql.apollo3.api.EnumType { *; }

-keep class com.fahad.newtruelovebyfahad.GetMainScreenQuery$Frame { *; }
-keep class com.fahad.newtruelovebyfahad.GetMainScreenQuery$Data { *; }

-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$Effect { *; }
-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$Data { *; }
-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$ParentCategory { *; }
-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$Tag { *; }
-keep class com.fahad.newtruelovebyfahad.GetFiltersQuery$Tag1 { *; }

-keep class com.fahad.newtruelovebyfahad.GetStickersQuery$Sticker { *; }
-keep class com.fahad.newtruelovebyfahad.GetStickersQuery$Data { *; }
-keep class com.fahad.newtruelovebyfahad.GetStickersQuery$ParentCategory { *; }
-keep class com.fahad.newtruelovebyfahad.GetStickersQuery$Tag { *; }

 -keep class com.huawei.hianalytics.**{*;}
 -keep class com.huawei.updatesdk.**{*;}
 -keep class com.huawei.hms.**{*;}

-dontwarn com.project.common.R$drawable

-dontwarn com.project.common.R$color
