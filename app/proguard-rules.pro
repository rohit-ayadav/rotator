# Rotator ProGuard / R8 Rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Preserve AndroidX ViewBinding classes
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static ** inflate(...);
    public static ** bind(...);
}

# Preserve Enum values for OrientationMode
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
