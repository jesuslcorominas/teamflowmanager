import org.gradle.testing.jacoco.tasks.JacocoReport

apply(plugin = "jacoco")

// Android app modules with build flavors — task pattern: test{Flavor}DebugUnitTest
// Add new flavored app modules here (1 line each)
val androidAppModules = listOf(
    ":app" to "testDevDebugUnitTest",
)

// Android library and KMP modules (no flavors) — task pattern: testDebugUnitTest
// A KMP module with androidTarget() belongs here: its tests live in src/androidUnitTest,
// its classes in build/tmp/kotlin-classes/debug and its coverage data in
// build/jacoco/testDebugUnitTest.exec — exactly what this branch aggregates.
// Add new Android library or KMP modules here (1 line each)
val androidModules = listOf(
    ":viewmodel",
    ":data:local",
    ":data:remote",
    ":domain",
    ":usecase",
    ":data:core",
)

// Pure JVM/Kotlin modules — classes under build/classes/kotlin/main, tests on the :test task.
// Empty on purpose: every module in this project targets Android, so none qualifies today.
// Kept as the extension point for a future JVM-only module.
// Add new JVM modules here (1 line each)
val jvmModules = emptyList<String>()

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

        // Both layouts coexist: the classic Gradle one (:app) and the KMP one, where shared
        // code lives in commonMain and platform code in androidMain. A module may offer
        // several of these; takeIf { exists } keeps only the ones it actually has.
        val sourceSetDirs = listOf(
            "src/main/java",
            "src/main/kotlin",
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
        )
        val sourceDirs = (androidAppModules.map { (path, _) -> path } + androidModules + jvmModules).flatMap { path ->
            val mod = rootProj.project(path)
            sourceSetDirs.mapNotNull { dir -> mod.file(dir).takeIf { it.exists() } }
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
