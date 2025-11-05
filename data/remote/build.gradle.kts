plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
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
    implementation(project(":data:core"))
    implementation(project(":domain"))

    implementation(libs.kotlinx.coroutines.core)
    
    // Koin
    implementation(libs.koin.core)
    
    // KtorFit
    implementation(libs.ktorfit.lib)
    ksp(libs.ktorfit.ksp)
    
    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    
    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
