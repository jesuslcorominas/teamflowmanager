import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

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
