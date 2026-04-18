apply(from = "version.gradle.kts")
val version: String by project.extra
val calculateVersionCode: (String) -> Int by project.extra

plugins {
    id("com.android.application") // https://google.github.io/android-gradle-dsl/current/
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
val androidTargetVersion = 35

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

android {
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

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

        debug {
            versionNameSuffix = "-SNAPSHOT"
        }

        // https://developer.android.com/studio/build/shrink-code
        release {
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

    namespace = "com.github.zeckson.vernam"

}

dependencies {
    // Biometric module
    implementation(libs.androidx.biometric)

    // App compat backward compatibility lib
    implementation(libs.androidx.appcompat)

    // AndroidX simplified preference manipulation
    implementation(libs.androidx.preference.ktx)

    // AndroidX security lib
    implementation(libs.androidx.security.crypto)

    // Material design UI libs
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Test libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // Lifecycles only (without ViewModel or LiveData)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.extensions1)

    // vernam library
    implementation(libs.vernam.tools)
}
