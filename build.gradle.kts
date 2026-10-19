// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.gradle.ktlint) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

// Kotlin version used by the compiler, read once here because the `libs` version-catalog
// accessor is only available on the root project script, not inside `subprojects { }`.
val kotlinCompilerVersion = libs.versions.kotlin.get()

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Keep the Kotlin standard library aligned with the compiler version declared in
    // gradle/libs.versions.toml. Some dependencies (notably ktorfit 2.6.0) are published
    // against kotlin-stdlib 2.2.0; that higher version wins Gradle's conflict resolution on
    // the Android classpath, and Kotlin Multiplatform's consistent resolution then propagates
    // a "strictly 2.2.0" constraint onto the common metadata configurations. The resulting
    // 2.2.0 commonMain .klib has an ABI the 2.1.0 compiler cannot read, which breaks
    // :data:remote:compileCommonMainKotlinMetadata. Forcing the stdlib back to the compiler
    // version keeps the runtime library and the compiler in sync.
    configurations.configureEach {
        resolutionStrategy {
            force(
                "org.jetbrains.kotlin:kotlin-stdlib:$kotlinCompilerVersion",
                "org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinCompilerVersion",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinCompilerVersion",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinCompilerVersion",
                "org.jetbrains.kotlin:kotlin-reflect:$kotlinCompilerVersion",
            )
        }
    }

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        verbose.set(true)
        filter {
            exclude("**/generated/**", "**/**Test**")
            exclude { it.file.absolutePath.contains("/build/") }
        }
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
    }
}

tasks.register("ktlintFormatAll") {
    group = "formatting"
    description = "Runs ktlintFormat on all modules"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("ktlintFormat") })
}

tasks.register("ktlintCheckAll") {
    group = "verification"
    description = "Runs ktlintCheck on all modules"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("ktlintCheck") })
}

apply(from = "$rootDir/jacoco-aggregate.gradle.kts")
