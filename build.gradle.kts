// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.gradle.ktlint) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        verbose.set(true)
        filter {
            exclude("**/generated/**", "**/**Test**")
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
