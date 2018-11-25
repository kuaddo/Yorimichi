import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

buildscript {
    repositories {
        google()
        jcenter()
        maven("https://maven.fabric.io/public")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.2.1")
        classpath(kotlin("gradle-plugin", "1.2.70"))
        classpath("com.google.gms:google-services:4.1.0")
        classpath("com.google.gms:oss-licenses:0.9.2")
        classpath("com.github.gfx.ribbonizer:ribbonizer-plugin:2.1.0")
        classpath("io.fabric.tools:gradle:1.26.1")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://maven.fabric.io/public")
    }
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}