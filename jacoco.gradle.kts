import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

apply(from = "$rootDir/coverage-exclusions.gradle.kts")
apply(plugin = "jacoco")

configure<JacocoPluginExtension> {
    toolVersion = "0.8.10"
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

    tasks.register<JacocoReport>("test${variantName.capitalize()}UnitTestCoverage") {
        dependsOn(
            ":app:test${variantName.capitalize()}UnitTest",
//            ":viewmodel:test${variantName.capitalize()}UnitTest",
            ":data:local:test${variantName.capitalize()}UnitTest",
            ":data:core:test",
            ":data:remote:test",
            ":usecase:test",
            ":domain:test"
        )

        group = "Reporting"
        description = "Generate Jacoco coverage reports"

        reports {
            xml.required.set(true)
            html.required.set(true)
            xml.outputLocation.set(file("$buildDir/reports/coverage/jacoco/$variantName/coverage.xml"))
            html.outputLocation.set(file("$buildDir/reports/coverage/jacoco/$variantName/coverage.html"))
        }

        val excludes = (project.extra["excludes"] as List<String>)

        classDirectories.setFrom(files(
            fileTree(
                mapOf(
                    "dir" to "$rootDir/app/build/tmp/kotlin-classes/${variantName}",
                    "excludes" to excludes
                )
            ),

//            fileTree(
//                mapOf(
//                    "dir" to "$rootDir/viewmodel/build/tmp/kotlin-classes/${variantName}",
//                    "excludes" to excludes
//                )
//            ),

            fileTree(
                mapOf(
                    "dir" to "$rootDir/data/local/build/tmp/kotlin-classes/${variantName}",
                    "excludes" to excludes
                )
            ),

            fileTree(
                mapOf(
                    "dir" to "$rootDir/data/core/build/classes/kotlin",
                    "excludes" to excludes
                )
            ),

            fileTree(
                mapOf(
                    "dir" to "$rootDir/data/remote/build/classes/kotlin",
                    "excludes" to excludes
                )
            ),

            fileTree(
                mapOf(
                    "dir" to "$rootDir/usecase/build/classes/kotlin",
                    "excludes" to excludes
                )
            )
        ))

        val coverageSourceDirs = listOf(
            "$rootDir/app/src/main/java",
//            "$rootDir/viewmodel/src/main/java",
            "$rootDir/usecase/src/main/java",
            "$rootDir/data/core/src/main/java",
            "$rootDir/data/local/src/main/java",
            "$rootDir/data/remote/src/main/java",
            "$rootDir/domain/src/main/java"
        )

        additionalSourceDirs.setFrom(files(coverageSourceDirs))
        sourceDirectories.setFrom(files(coverageSourceDirs))

        val execFiles = listOf(
            "$rootDir/domain/build/jacoco/test.exec",
//            "$rootDir/viewmodel/build/jacoco/test.exec",
            "$rootDir/data/core/build/jacoco/test.exec",
            "$rootDir/data/remote/build/jacoco/test.exec",
            "$rootDir/usecase/build/jacoco/test.exec",
            "$rootDir/data/local/build/jacoco/test${variantName.capitalize()}UnitTest.exec",
            "$rootDir/app/build/jacoco/test${variantName.capitalize()}UnitTest.exec"
        )
        execFiles.forEach { println("Checking for exec file: $it, exists: ${java.io.File(it).exists()}") }
        executionData.setFrom(files(execFiles))
    }
}
