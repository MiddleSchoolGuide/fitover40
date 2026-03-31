# Project-specific ProGuard rules.

# Room entities and DAOs
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface *
-keep @androidx.room.RawQuery interface *
-keepclassmembers class * {
  @androidx.room.PrimaryKey *;
  @androidx.room.ColumnInfo *;
  @androidx.room.Ignore *;
}

# Data classes used by Room (keep constructor and fields)
-keepclassmembers class * {
    @androidx.room.Entity *;
}

# ViewModel subclasses
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Kotlin Serialization (if used)
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Strip Log.d calls in release
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
