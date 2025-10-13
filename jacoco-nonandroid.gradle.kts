import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

apply(from = "$rootDir/coverage-exclusions.gradle.kts")
apply(plugin = "jacoco")

configure<JacocoPluginExtension> {
    toolVersion = "0.8.10"
    // Custom reports directory can be specified like this:
    // reportsDir = file("$buildDir/customJacocoReportDir")
}

tasks.withType<Test> {
    extensions.configure<JacocoTaskExtension>("jacoco") {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
        // see related issue https://github.com/gradle/gradle/issues/5184#issuecomment-457865951
    }
}

afterEvaluate {
    val variantName = "debug"
    val unitTestTask = "test${variantName.capitalize()}UnitTest"

    tasks.register<JacocoReport>("${unitTestTask}Coverage") {
        dependsOn(
            ":data:core:test",
            ":data:remote:test",
            ":usecase:test",
            ":domain:test",
        )


        group = "Reporting"
        description = "Generate Jacoco coverage reports for the ${variantName.capitalize()} build"

        val excludes = (project.extra["excludes"] as List<String>)

        classDirectories.setFrom(
            files(
                fileTree(
                    mapOf(
                        "dir" to "$buildDir/classes/kotlin/main",
                        "excludes" to excludes
                    )
                )
            )
        )

        val coverageSourceDirs = listOf(
            "$rootDir/usecase/src/main/java",
            "$rootDir/data/core/src/main/java",
            "$rootDir/data/remote/src/main/java",
            "$rootDir/domain/src/main/java"
        )

        additionalSourceDirs.setFrom(files(coverageSourceDirs))
        sourceDirectories.setFrom(files(coverageSourceDirs))

        executionData.setFrom(files(
            "$rootDir/usecase/build/jacoco/test.exec",
            "$rootDir/data/core/build/jacoco/test.exec",
            "$rootDir/data/remote/build/jacoco/test.exec",
            "$rootDir/domain/build/jacoco/test.exec"
        ))

        reports {
            xml.required.set(true)
            html.required.set(true)
            xml.outputLocation.set(file("$buildDir/reports/coverage/jacoco/$variantName/coverage.xml"))
            html.outputLocation.set(file("$buildDir/reports/coverage/jacoco/$variantName/coverage.html"))
        }
    }
}
