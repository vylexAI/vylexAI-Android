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
