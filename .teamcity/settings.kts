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

    val bts = sequential {
        buildType(Maven("Build", "clean compile"))
        parallel {
            buildType(Maven("Fast Test", "clean test", "-Dmaven.test.failure.ignore=true -Dtest=*.unit.*Test"))
            buildType(Maven("Slow Test", "clean test", "-Dmaven.test.failure.ignore=true -Dtest=*.integration.*Test"))
        }
        buildType(Maven("Package", "clean package", "-DskipTests"))
    }.buildTypes()

    bts.forEach { buildType(it) }
    bts.last().triggers {
        vcs {}
    }
}

class Maven(name: String, goals: String, runnerArgs: String? = null) : BuildType({
    id(name)
    this.name = name

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            this.goals = goals
            this.runnerArgs = runnerArgs
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