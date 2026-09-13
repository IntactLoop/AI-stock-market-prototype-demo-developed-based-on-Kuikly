plugins {
    id("com.android.application").version("8.7.2").apply(false)
    id("com.android.library").version("8.7.2").apply(false)
    kotlin("android").version("2.1.21").apply(false)
    kotlin("multiplatform").version("2.1.21").apply(false)
    kotlin("plugin.compose").version("2.1.21").apply(false)
    id("com.google.devtools.ksp").version("2.1.21-2.0.1").apply(false)
}

buildscript {
    dependencies {
        classpath(BuildPlugin.kuikly)
    }
}