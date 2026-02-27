plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val iosMain by creating {
            dependsOn(getByName("commonMain"))
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.room.runtime)
                implementation(libs.koin.android)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}

dependencies {
    // Business-logic modules — all are KMP-compatible (commonMain or expect/actual)
    add("commonMainImplementation", project(":domain"))
    add("commonMainImplementation", project(":usecase"))
    add("commonMainImplementation", project(":data:core"))
    add("commonMainImplementation", project(":data:local"))
    add("commonMainImplementation", project(":data:remote"))
    add("commonMainImplementation", project(":viewmodel"))
}

android {
    namespace = "com.jesuslcorominas.teamflowmanager.di"
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
