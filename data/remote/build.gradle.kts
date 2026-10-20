plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    // Note: ktorfit Gradle plugin intentionally omitted for KMP —
    // it registers kspCommonMainKotlinMetadata which conflicts with Kotlin 2.1.
    // Instead we use the library directly with kspAndroid.
}

apply(from = "$rootDir/jacoco.gradle.kts")

kotlin {
    jvmToolchain(17)
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.android)
                // Ktorfit + Ktor (Android only in Phase 1)
                implementation(libs.ktorfit.lib)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)
                // Firebase (Android only)
                implementation(libs.firebase.auth.ktx)
                implementation(libs.firebase.firestore.ktx)
                implementation(libs.firebase.storage.ktx)
                implementation(libs.firebase.messaging.ktx)
            }
        }
        val iosMain by creating {
            dependsOn(getByName("commonMain"))
            dependencies {
                implementation(libs.gitlive.firebase.auth)
                implementation(libs.gitlive.firebase.firestore)
                implementation(libs.ktor.client.darwin)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }
    }
}

// KSP only for Android (Ktorfit code generation)
// Firebase BOM applied at module level
dependencies {
    add("kspAndroid", libs.ktorfit.ksp)
    add("androidMainImplementation", platform(libs.firebase.bom))
    add("commonMainImplementation", project(":data:core"))
    add("commonMainImplementation", project(":domain"))
}

android {
    namespace = "com.jesuslcorominas.teamflowmanager.data.remote"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// The dev.gitlive:firebase-* artifacts are cinterop wrappers over the FirebaseCore,
// FirebaseAuth and FirebaseFirestore Objective-C frameworks. This project supplies those
// frameworks through CocoaPods in iosApp.xcworkspace (iosApp/Pods/ is gitignored), not
// through the Gradle build, so Gradle has no framework search path for them. Linking the
// iOS *executable* for this module therefore fails with `ld: framework 'FirebaseCore' not
// found`, which broke `./gradlew build`.
//
// Only the test executable is affected: the production framework produced by :iosApp is
// static, so it defers those symbols to the Xcode link, where the pods are present. The
// commonTest sources of this module are pure Firestore mapper tests and already run on the
// JVM as part of the Android unit tests, so disabling the native test binary costs no
// coverage. Re-enable this if the module ever adopts the Kotlin CocoaPods plugin.
tasks.matching { it.name.matches(Regex("link.*Test(Ios|IosSimulator)Arm64")) }
    .configureEach { enabled = false }
tasks.matching { it.name.matches(Regex("ios(Arm64|SimulatorArm64)Test(Binaries)?")) }
    .configureEach { enabled = false }
