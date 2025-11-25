pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TeamFlowManager"

// Android Application
include(":androidApp")

// Shared KMM Modules
include(":shared")
include(":shared:domain")
include(":shared:usecase")
include(":shared:data:core")
include(":shared:data:remote")

// Android-specific modules
include(":viewmodel")
include(":service")
include(":data:local")
include(":di")
