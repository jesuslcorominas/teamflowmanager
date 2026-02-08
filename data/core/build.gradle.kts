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
    implementation(project(":domain"))
    implementation(project(":usecase"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
