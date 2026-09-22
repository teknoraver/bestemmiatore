import java.util.Properties

plugins {
    id("com.android.application")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val hasKeystore = keystorePropertiesFile.exists()
if (hasKeystore) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "net.teknoraver.bestemmiatore"
    compileSdk = 37
    buildToolsVersion = "37.0.0"
    defaultConfig {
        minSdk = 23
        targetSdk = 37
        versionCode = 6
        versionName = "1.2"
    }
    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.core:core:1.19.0")
}
