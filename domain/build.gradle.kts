plugins {
    id("org.jetbrains.kotlin.jvm")
}

apply(from = "$rootDir/jacoco-nonandroid.gradle.kts")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
