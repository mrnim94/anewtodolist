import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.04"

project {

    buildType(Build)
    buildType(Package)
    buildType(FastTest)
    buildType(SlowTest)

    sequential {
        buildType(Build)
        parallel {
            buildType(FastTest)
            buildType(SlowTest)
        }
        buildType(Package)
    }
}

object Build : BuildType({
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Nim custom step Build"
            goals = "clean compile"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    features {
        freeDiskSpace {
            requiredSpace = "1gb"
            failBuild = true
        }
    }

    triggers {
        vcs {
            branchFilter = """
                +:lesson1
                +:lesson2
                +:main
            """.trimIndent()
        }
    }

    features {
        freeDiskSpace {
            requiredSpace = "1gb"
            failBuild = true
        }
    }
})

object Package : BuildType({
    name = "Package"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Nim custom step Package"
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    triggers {
        vcs {
        }
    }
})

object FastTest : BuildType({
    name = "FastTest"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Nim custom step FastTest"
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true -Dtest=*.unit.*Test"
        }
    }

    triggers {
        vcs {
        }
    }
})



object SlowTest : BuildType({
    name = "SlowTest"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Nim custom step SlowTest"
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true -Dtest=*.integration.*Test"
        }
    }

    triggers {
        vcs {
        }
    }
})