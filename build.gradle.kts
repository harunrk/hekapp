// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Bu iki plugin’i "şimdilik pasif bırakıyorum ama gerektiğinde kullanabilirim" anlamına gelir.
    // Özellikle projende birden fazla modül varsa, hangi modülde hangi plugin’i kullanacağını daha
    // kolay yönetmeni sağlar.
    // Mesela, :app modülünde androidx.navigation.safeargs.kotlin kullanırken, :data modülünde
    // sadece ksp kullanabilirsin.
    id ("androidx.navigation.safeargs.kotlin") version "2.8.5" apply false
    id ("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false

    id("com.google.gms.google-services") version "4.4.2" apply false
}