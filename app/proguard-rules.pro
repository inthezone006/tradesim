# Firebase Rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Crashlytics
-keep public class com.google.firebase.crashlytics.** { *; }
-keepattributes SourceFile,LineNumberTable

# Retrofit & Gson Rules
-keepattributes Signature, Metadata, *Annotation*, EnclosingMethod, InnerClasses
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keep public class * extends com.google.gson.reflect.TypeToken

# Fix for ClassCastException in Retrofit suspend functions
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve our API interfaces and data models
-keep interface com.rahul.stocksim.data.FinnhubApi { *; }
-keep interface com.rahul.stocksim.data.TwelveDataApi { *; }
-keep class com.rahul.stocksim.data.Finnhub** { *; }
-keep class com.rahul.stocksim.data.TwelveData** { *; }
-keep class com.rahul.stocksim.data.StockPricePoint { *; }
-keep class com.rahul.stocksim.model.** { *; }
-keepclassmembers class com.rahul.stocksim.data.** { *; }

# Kotlin Coroutines
-keep class kotlin.coroutines.Continuation { *; }

# Coil Rules
-keep class coil.** { *; }

# Navigation Rules
-keep class androidx.navigation.** { *; }

# Room & WorkManager
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>(...);
}
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-dontwarn androidx.work.impl.WorkDatabase_Impl

# Startup / DataStore
-keep class androidx.startup.** { *; }
-keep class androidx.datastore.** { *; }
-keep class * extends androidx.startup.Initializer

# Proguard fix for potential adflow NPE
-dontwarn com.adflow.**
-keep class com.adflow.** { *; }
