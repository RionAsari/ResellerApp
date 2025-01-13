pluginManagement {
    repositories {
        google()  // Add Google repository
        mavenCentral()  // Add Maven Central repository
        gradlePluginPortal()  // If you need the Gradle Plugin Portal
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()  // Add Google repository
        mavenCentral()  // Add Maven Central repository
        maven { url = uri("https://www.jitpack.io") } // Add JitPack repository
    }
}

rootProject.name = "ResellerApp"
include(":app")
