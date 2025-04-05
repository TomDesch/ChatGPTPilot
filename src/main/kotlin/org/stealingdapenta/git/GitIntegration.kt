package org.stealingdapenta.git

import com.intellij.openapi.project.Project
import java.io.File

object GitIntegration {

    fun runGitCommand(project: Project, vararg args: String): String {
        val workingDir = File(project.basePath ?: ".")
        val process = ProcessBuilder("git", *args)
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output
    }

    fun stageAll(project: Project): String {
        return runGitCommand(project, "add", ".")
    }

    fun commit(project: Project, message: String): String {
        return runGitCommand(project, "commit", "-m", message)
    }

    fun push(project: Project, branch: String): String {
        return runGitCommand(project, "push", "--set-upstream", "origin", branch)
    }
}
