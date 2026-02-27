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

include(":app")
include(":viewmodel")
include(":usecase")
include(":data:core")
include(":data:local")
include(":data:remote")
include(":di")
include(":domain")
include(":iosApp")
