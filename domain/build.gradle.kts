plugins {
    id("org.jetbrains.kotlin.jvm")
}

apply(from = "$rootDir/jacoco-nonandroid.gradle.kts")

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
