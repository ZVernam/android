// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Info on Gradle DSL https://docs.gradle.org/current/dsl/

buildscript {
    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        jcenter()

    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
