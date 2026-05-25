# 基本のRetention
-keepattributes Signature, InnerClasses, EnclosingMethod

# kotlinx-serialization
-keep,includedescriptorclasses class com.emojicode.app.**$$serializer { *; }
-keepclassmembers class com.emojicode.app.** { *** Companion; }
-keepclasseswithmembers class com.emojicode.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
