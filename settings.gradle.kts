pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter() // Add JCenter as backup for older artifacts
    }
}

rootProject.name = "tfDemo"
include(":app")
include(":migrate")
include(":cifar10")
include(":gyro")
include(":db")
include(":net")
include(":executorch")
