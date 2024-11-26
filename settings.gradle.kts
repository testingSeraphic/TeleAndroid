pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        flatDir {
            dirs("libs")
        }
            mavenCentral()
        maven("https://jitpack.io")


        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven ("https://dl.cloudsmith.io/public/cometchat/cometchat/maven/")
    }
}

rootProject.name = "TeleAndroid"
include(":tele-video")
