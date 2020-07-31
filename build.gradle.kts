// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    ext {
        app_compat_version = "1.1.0"
        kotlin_version = "1.3.72"
    }

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

task.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
