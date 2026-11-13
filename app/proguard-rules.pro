# Pedeef ProGuard rules

# PDFBox (issues with reflection in some paths)
-keep class com.tom_roush.pdfbox.** { *; }
-keep class com.tom_roush.harmony.** { *; }
-dontwarn com.gemalto.jp2.**
-dontwarn com.tom_roush.**

# kotlinx-serialization, the annotations are looked up reflectively on some paths
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# ML Kit
-keep class com.google.mlkit.** { *; }
