pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FitOver40"
val backendOnly = providers.environmentVariable("FITOVER40_BACKEND_ONLY")
    .orElse(providers.environmentVariable("RAILWAY_BACKEND_ONLY"))
    .orElse("false")
    .map { it.equals("true", ignoreCase = true) }
    .get()

if (!backendOnly) {
    include(":app")
}
include(":local-auth-server")
