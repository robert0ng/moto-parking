# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.motoparking.app.**$$serializer { *; }
-keepclassmembers class com.motoparking.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.motoparking.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep Supabase
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# Keep Koin
-keep class org.koin.** { *; }

# Keep Google Maps
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }
