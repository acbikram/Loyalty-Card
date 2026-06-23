# ----------------------------------------------------------------------------
# Universal Loyalty Wallet - R8 / ProGuard rules
# ----------------------------------------------------------------------------

# Keep line numbers for readable crash traces, then hide the original file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin metadata & coroutines ---
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-dontwarn kotlinx.coroutines.**

# --- Kotlinx Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}

# --- Room (generated implementations) ---
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# --- Hilt / Dagger ---
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }

# --- ML Kit barcode scanning ---
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# --- ZXing ---
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# --- Models that are serialized/deserialized (kept defensively) ---
-keep class com.universalwallet.loyalty.data.model.** { *; }
-keep class com.universalwallet.loyalty.domain.model.** { *; }
