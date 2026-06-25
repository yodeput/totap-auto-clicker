# Keep Compose & Hilt (consumer rules cover most; belt-and-suspenders for release builds)
-dontwarn org.jetbrains.annotations.**

# Strip logging in release
-assumenosideeffects class android.util.Log {
    public *;
}
