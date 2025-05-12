-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }
-dontnote org.slf4j.**

-keep class org.eclipse.paho.client.mqttv3.** { *; }
-keep class org.eclipse.paho.android.service.** { *; }
-dontwarn org.eclipse.paho.client.mqttv3.**
-dontwarn org.eclipse.paho.android.service.**

-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*,*SerializedName*
-dontwarn kotlinx.serialization.**

-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

-keep class java.lang.reflect.** { *; }
-dontwarn java.lang.reflect.**

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keep class * implements java.io.Serializable { *; }

-keep class app.ma.lightcontroller.** { *; }