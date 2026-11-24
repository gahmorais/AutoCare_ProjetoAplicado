# Regras do projeto AutoCare.
# Ver a configuracao em app/build.gradle (proguardFiles).

# Mantem stack traces uteis no Play Console (com o mapping.txt) sem expor os nomes originais.
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

# Room gera o codigo de acesso em tempo de compilacao e traz as proprias regras;
# os modelos nao sao lidos por reflexao e podem ser ofuscados.

# Koin resolve dependencias por tipo em runtime.
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# WorkManager instancia o Worker por reflexao a partir do nome da classe.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
