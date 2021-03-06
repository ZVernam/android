// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Info on Gradle DSL https://docs.gradle.org/current/dsl/

buildscript {
    val kotlin_version by extra("1.3.72")
    extra["app_compat_version"] = "1.1.0"

    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")

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
