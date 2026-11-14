plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
}

android {
  namespace = "com.nekonaru.pedeef"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.nekonaru.pedeef"
    minSdk = 24
    targetSdk = 36
    versionCode = 2
    versionName = "1.1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    // Optional upload key, kept out of the repo in ~/.gradle/gradle.properties.
    val store = findProperty("PEDEEF_UPLOAD_STORE_FILE") as String?
    if (store != null) create("release") {
      storeFile = file(store)
      storePassword = findProperty("PEDEEF_UPLOAD_STORE_PASSWORD") as String?
      keyAlias = findProperty("PEDEEF_UPLOAD_KEY_ALIAS") as String?
      keyPassword = findProperty("PEDEEF_UPLOAD_KEY_PASSWORD") as String?
    } else {
      println("WARNING: no PEDEEF_UPLOAD_* properties found. Release builds will be signed with the SDK debug key. Set them in ~/.gradle/gradle.properties to use your upload key.")
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures { compose = true }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    optIn.addAll(
      "androidx.compose.material3.ExperimentalMaterial3Api",
      "androidx.compose.foundation.layout.ExperimentalLayoutApi",
    )
  }
}

dependencies {
  implementation(platform("androidx.compose:compose-bom:2026.06.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.activity:activity-compose:1.11.0")
  implementation("androidx.navigation:navigation-compose:2.9.4")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

  // PDF engine (Apache PDFBox ported to Android) and on-device OCR.
  implementation("com.tom-roush:pdfbox-android:2.0.27.0")
  implementation("com.google.mlkit:text-recognition:16.0.1")

  // Scanner: ML Kit Document Scanner (edge detection and cleanup built in).
  implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")

  testImplementation("junit:junit:4.13.2")
}
