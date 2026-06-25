# Totap release ProGuard / R8 rules (spec §8.7)
# Complete keep-rule set (Appwrite, Room DAOs, models) is added in Phase 8
# as those dependencies land. Below covers the Phase-1 surface: Hilt + Compose.

# --- Strip logging in release (only the Log class, not Object methods) ---
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# --- Kotlin metadata & coroutines ---
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}

# --- Hilt / Dagger (reflection-driven DI graph) ---
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Hilt-generated entry points & components
-keep,allowobfuscation @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep,allowobfuscation @dagger.hilt.android.qualifiers.* class * { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory$ViewModelComponentBuilderEntryPoint { *; }

# --- Compose ---
# Compose ships consumer ProGuard rules; these are belt-and-suspenders for release.
-dontwarn org.jetbrains.annotations.**
-keep class androidx.compose.runtime.** { *; }

# Keep the app's @AndroidEntryPoint activities (instantiated by the framework).
-keep class com.kunnn.totap.MainActivity { *; }
-keep class com.kunnn.totap.TotapApplication { *; }

# --- Keep the BuildConfig class (fields accessed reflectively by some libs) ---
-keep class com.kunnn.totap.BuildConfig { *; }
