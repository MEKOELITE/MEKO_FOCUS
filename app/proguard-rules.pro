# Add project specific ProGuard rules here.

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep Room entities
-keep class com.meko.focus.data.local.entity.** { *; }

# Keep data classes used with DataStore
-keep class com.meko.focus.domain.model.** { *; }

# Kotlin
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
