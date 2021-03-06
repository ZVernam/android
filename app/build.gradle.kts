@file:Suppress("LocalVariableName")

apply(from = "version.gradle.kts")
val version:String by project.extra
val calculateVersionCode: (String) -> Int by project.extra

plugins {
    id("com.android.application") // https://google.github.io/android-gradle-dsl/current/
    id("kotlin-android")
    id("kotlin-android-extensions")
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


android {
    compileSdkVersion(29)

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    defaultConfig {
        applicationId = "com.github.zeckson.vernam"

        minSdkVersion(24)
        targetSdkVersion(29)

        val myVersionCode = calculateVersionCode(version)
        versionName = version
        versionCode = myVersionCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        println("config code: $myVersionCode, name: $version")
    }

    signingConfigs {
        val storeFilePath = findProperty("release.keystore.file") as String?

        if(storeFilePath != null) {
            create("release") {
                storeFile = rootDir.resolve(storeFilePath)
                storePassword = property("release.keystore.password") as String
                keyAlias = property("release.key.alias") as String
                keyPassword = property("release.key.password") as String

                isV1SigningEnabled = true
                isV2SigningEnabled = true

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

}

dependencies {
    // Biometric module
    val biometric_version = "1.0.1"
    implementation("androidx.biometric:biometric:$biometric_version")

    // App compat backward compatibility lib
    val app_compat_version: String by rootProject.extra
    implementation("androidx.appcompat:appcompat:$app_compat_version")

    // AndroidX simplified preference manipulation
    implementation("androidx.preference:preference:1.1.1")

    // AndroidX security lib
    implementation("androidx.security:security-crypto:1.1.0-alpha01")

    // Material design UI libs
    implementation("com.google.android.material:material:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    // Kotlin std lib
    val kotlin_version: String by rootProject.extra
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")

    // Test libraries
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")

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
    implementation("com.github.zeckson:vernam-tools:0.2.0")
}
