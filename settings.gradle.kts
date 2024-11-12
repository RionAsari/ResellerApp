pluginManagement {
    repositories {
        google()  // Add Google repository
        mavenCentral()  // Add Maven Central repository
        gradlePluginPortal()  // If you need the Gradle Plugin Portal
    }
}

dependencyResolutionManagement {
    repositories {
        google()  // Add Google repository
        mavenCentral()  // Add Maven Central repository
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ResellerApp"
include(":app")
 