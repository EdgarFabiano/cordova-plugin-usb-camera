# Mantém todas as classes do pacote com.jiangdg.uvc
-keep class com.jiangdg.uvc.** { *; }

# Mantém todos os métodos nativos para evitar problemas com JNI
-keepclassmembers class * {
    native <methods>;
}
