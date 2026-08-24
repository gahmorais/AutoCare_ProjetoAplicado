# Regras do projeto AutoCare.
# Ver a configuracao em app/build.gradle (proguardFiles).

# Mantem stack traces uteis no Crashlytics/Play Console sem expor os nomes originais.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove as chamadas de log do binario de release. Varias delas carregavam dados
# pessoais (placa do veiculo, e-mail do usuario) e ficavam legiveis por qualquer
# app com READ_LOGS ou via adb.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# O Firebase Realtime Database desserializa por reflexao: sem isso os modelos
# viram campos ofuscados e a leitura devolve objetos vazios.
-keepclassmembers class br.com.gabrielmorais.autocare.data.models.** {
    <init>();
    <fields>;
    public void set*(***);
    public *** get*();
}
-keepnames class br.com.gabrielmorais.autocare.data.models.**

# Anotacoes usadas pelo mapeador do Firebase.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Koin resolve dependencias por tipo em runtime.
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# WorkManager instancia o Worker por reflexao a partir do nome da classe.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
