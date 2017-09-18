# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keep class okio.**{*;}
-dontwarn okio.**
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep class android.support.graphics.drawable.**{*;}
-keep class com.ruilonglai.texas_scan.entity.**{*;}
-keep class com.ruilonglai.texas_scan.config.**{*;}
-keep class com.ruilonglai.texas_scan.download.**{*;}
-keep class com.ruilonglai.texas_scan.models.**{*;}
-keep class com.ruilonglai.texas_scan.ScreenShotUtil.**{*;}
-keep class com.ruilonglai.texas_scan.application.**{*;}
-keep class com.ruilonglai.texas_scan.data.**{*;}
-keep class com.ruilonglai.texas_scan.log.**{*;}
-keep class com.ruilonglai.texas_scan.newprocess.**{*;}
-keep class com.sleepbot.datetimepicker.time.**{*;}
-keep class com.nineoldandroids.animation.**{*;}
-keep class com.nineoldandroids.util.**{*;}
-keep class com.nineoldandroids.view.animation.**{*;}
-keep class com.tendcloud.tenddata.**{*;}
-keep class com.wang.avi.indicator.**{*;}
-keep class com.google.gson.**{*;}
-keep class butterknife.internal.**{*;}
-keep class com.lcodecore.tkrefreshlayout.**{*;}
-keep class org.litepal.**{*;}
-keep class okhttp3.internal.**{*;}
-keep class okhttp3.**{*;}
-keep class com.fourmob.datetimepicker.date.**{*;}
-keep class org.angmarch.views.**{*;}
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**



