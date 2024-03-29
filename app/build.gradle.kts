@file:Suppress("LocalVariableName")

apply(from = "version.gradle.kts")
val version: String by project.extra
val calculateVersionCode: (String) -> Int by project.extra

plugins {
    id("com.android.application") // https://google.github.io/android-gradle-dsl/current/
    id("kotlin-android")
}

repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/ZVernam/vernam-tools")
        credentials {
            username = project.findProperty("gpr.user")?.toString() ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key")?.toString() ?: System.getenv("TOKEN")
        }
    }
}

val androidMinVersion = 24
val androidTargetVersion = 31

android {
    buildFeatures {
        viewBinding = true
    }

    compileSdk = androidTargetVersion

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    defaultConfig {
        applicationId = "com.github.zeckson.vernam"

        minSdk = androidMinVersion
        targetSdk = androidTargetVersion

        val myVersionCode = calculateVersionCode(version)
        versionName = version
        versionCode = myVersionCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        println("config code: $myVersionCode, name: $version")
    }

    signingConfigs {
        val storeFilePath = findProperty("release.keystore.file") as String?

        if (storeFilePath != null) {
            create("release") {
                storeFile = rootDir.resolve(storeFilePath)
                storePassword = property("release.keystore.password") as String
                keyAlias = property("release.key.alias") as String
                keyPassword = property("release.key.password") as String

                enableV1Signing = true
                enableV2Signing = true

            }
        }

    }

    buildTypes {

        named("debug") {
            versionNameSuffix = "-SNAPSHOT"
        }

        // https://developer.android.com/studio/build/shrink-code
        named("release") {
            isMinifyEnabled = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.findByName("release")
        }
    }

    // To inline the bytecode built with JVM target 1.8 into
    // bytecode that is being built with JVM target 1.6. (e.g. navArgs)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    namespace = "com.github.zeckson.vernam"

}

dependencies {
    // Biometric module
    implementation("androidx.biometric:biometric:1.1.0")

    // App compat backward compatibility lib
    implementation("androidx.appcompat:appcompat:1.3.1")

    // AndroidX simplified preference manipulation
    implementation("androidx.preference:preference-ktx:1.1.1")

    // AndroidX security lib
    implementation("androidx.security:security-crypto:1.1.0-alpha03")

    // Material design UI libs
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")

    // Test libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    val lifecycle_version = "2.2.0"

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version")

    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // vernam library
    implementation("com.github.zeckson:vernam-tools:0.3.0")
}
