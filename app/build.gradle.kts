import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.gms.oss.licenses.plugin")
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 0

android {
    compileSdkVersion(28)
    dataBinding.isEnabled = true

    defaultConfig {
        applicationId = "jp.shiita.yorimichi"
        minSdkVersion(19)
        targetSdkVersion(28)
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
            storePassword = "yorimichi"
            keyAlias = "androiddebugkey"
            keyPassword = "yorimichi"
        }
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders = mapOf("GOOGLE_MAPS" to ApiKeys.GOOGLE_MAPS)
            resValue("string", "app_name", "debug_Yorimichi")
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            resValue("string", "app_name", "Yorimichi")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    lintOptions {
        disable("GoogleAppIndexingWarning")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    // SupportLibrary
    val supportVersion = "28.0.0"
    implementation("com.android.support.constraint:constraint-layout:1.1.3")
    implementation("com.android.support:appcompat-v7:$supportVersion")
    implementation("com.android.support:customtabs:$supportVersion")
    implementation("com.android.support:support-v4:$supportVersion")
    implementation("com.android.support:design:$supportVersion")

    // ArchitectureComponents
    val archVersion = "1.1.1"
    implementation("android.arch.lifecycle:runtime:$archVersion")
    implementation("android.arch.lifecycle:extensions:$archVersion")
    implementation("android.arch.lifecycle:reactivestreams:$archVersion")
    kapt("android.arch.lifecycle:compiler:$archVersion")

    // GMS
    implementation("com.google.android.gms:play-services-maps:16.0.0")
    implementation("com.google.android.gms:play-services-location:16.0.0")
    implementation("com.google.android.gms:play-services-oss-licenses:16.0.1")

    // Dagger
    val daggerVersion = "2.17"
    implementation("com.google.dagger:dagger:$daggerVersion")
    implementation("com.google.dagger:dagger-android:$daggerVersion")
    implementation("com.google.dagger:dagger-android-support:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kapt("com.google.dagger:dagger-android-processor:$daggerVersion")

    // Glide
    val glideVersion = "4.8.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    kapt("com.github.bumptech.glide:compiler:$glideVersion")

    // Kotshi
    val kotshiVersion = "1.0.5"
    implementation("se.ansman.kotshi:api:$kotshiVersion")
    kapt("se.ansman.kotshi:compiler:$kotshiVersion")

    // Retrofit
    val retrofitVersion = "2.4.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")

    // OkHttp
    val okHttpVersion = "3.11.0"
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    // Rx
    implementation("io.reactivex.rxjava2:rxjava:2.1.14")
    implementation("io.reactivex.rxjava2:rxkotlin:2.2.0")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.2")

    implementation("com.chibatching.kotpref:kotpref:2.6.0")

    // Test
    testImplementation("junit:junit:4.12")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}