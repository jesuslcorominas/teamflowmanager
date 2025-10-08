plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":viewmodel"))
    implementation(project(":domain"))
    
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
