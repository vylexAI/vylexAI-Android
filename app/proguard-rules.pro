# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Retrofit + OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt / Dagger
-keep class dagger.hilt.** { *; }

# ONNX Runtime
-keep class ai.onnxruntime.** { *; }

# Keep serializable task/model classes
-keep class com.vylexai.app.data.**$Companion { *; }
-keepclasseswithmembers class com.vylexai.app.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Transitive annotations referenced by Tink, TFLite support, and other libs
# but not actually shipped at runtime. Safe to strip warnings.
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.auto.value.**
-dontwarn javax.annotation.**

# TensorFlow Lite — keep the native interface surface.
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }

# Play Integrity — keep SDK surface.
-keep class com.google.android.play.core.integrity.** { *; }
-dontwarn com.google.android.play.core.integrity.**
