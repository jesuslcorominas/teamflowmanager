import org.gradle.testing.jacoco.tasks.JacocoReport

apply(plugin = "jacoco")

// Android app modules with build flavors — task pattern: test{Flavor}DebugUnitTest
// Add new flavored app modules here (1 line each)
val androidAppModules = listOf(
    ":app" to "testDevDebugUnitTest",
)

// Android library modules (no flavors) — task pattern: testDebugUnitTest
// Add new Android library modules here (1 line each)
val androidModules = listOf(
    ":viewmodel",
    ":data:local",
    ":data:remote",
)

// Pure JVM/Kotlin modules — add new JVM modules here (1 line each)
val jvmModules = listOf(
    ":usecase",
    ":data:core",
)

val classExcludes = listOf(
    "**/di/**",
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/BuildConfig.**",
    "**/Manifest*.*",
    "**/*\$*",
    "**/model/**",
    "**/dao/**",
    "**/api/**",
    "**/*Entity**",
    "**/ui/**",
    "**/*Screen.*",
    "**/*Screen*.*",
    "**/*Activity.*",
    "**/*Activity*.*",
    "**/components/**",
    "**/*Navigation.*",
    "**/*Navigation*.*",
    "**/database/**",
    "**/*FirestoreModel*",
    "**/*FirestoreTransactionRunner*",
    "**/*RealTimeTicker*",
)

afterEvaluate {
    val rootProj = project
    tasks.register<JacocoReport>("testDebugUnitTestCoverage") {
        group = "Reporting"
        description = "Generates unified JaCoCo coverage report for all configured modules"

        dependsOn(
            androidAppModules.map { (path, task) -> "$path:$task" } +
                androidModules.map { "$it:testDebugUnitTest" } +
                jvmModules.map { "$it:test" },
        )

        reports {
            xml.required.set(true)
            html.required.set(true)
            xml.outputLocation.set(
                layout.buildDirectory.file("reports/jacoco/testDebugUnitTestCoverage/coverage.xml"),
            )
            html.outputLocation.set(
                layout.buildDirectory.dir("reports/jacoco/testDebugUnitTestCoverage/html"),
            )
        }

        val androidClassDirs = (androidAppModules.map { (path, _) -> path } + androidModules).map { path ->
            val mod = rootProj.project(path)
            fileTree(mod.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(classExcludes)
            }
        }
        val jvmClassDirs = jvmModules.map { path ->
            val mod = rootProj.project(path)
            fileTree(mod.layout.buildDirectory.dir("classes/kotlin/main")) {
                exclude(classExcludes)
            }
        }
        classDirectories.setFrom(files(androidClassDirs + jvmClassDirs))

        val sourceDirs = (androidAppModules.map { (path, _) -> path } + androidModules + jvmModules).flatMap { path ->
            val mod = rootProj.project(path)
            listOfNotNull(
                mod.file("src/main/java").takeIf { it.exists() },
                mod.file("src/main/kotlin").takeIf { it.exists() },
            )
        }
        sourceDirectories.setFrom(files(sourceDirs))
        additionalSourceDirs.setFrom(files(sourceDirs))

        val execFiles = (androidAppModules.map { (path, _) -> path } + androidModules + jvmModules).map { path ->
            val mod = rootProj.project(path)
            fileTree(mod.layout.buildDirectory) { include("**/*.exec") }
        }
        executionData.setFrom(files(execFiles))
    }
}
