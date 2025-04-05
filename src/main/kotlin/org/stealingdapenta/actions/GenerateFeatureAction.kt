package org.stealingdapenta.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.stealingdapenta.github.GitHubService
import org.stealingdapenta.ui.FeaturePromptDialog

class GenerateFeatureAction : AnAction("Generate GitHub Feature with ChatGPT") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dialog = FeaturePromptDialog()
        if (!dialog.showAndGet()) return

        val prompt = dialog.getPrompt()
        val taskType = dialog.getTaskType()

        val branchName = "chatgpt/${taskType.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}"

        GitHubService.createBranch(
            newBranch = branchName,
            onSuccess = {
                // TODO: Call the model with the prompt and full repo context (next step).
                // Then stage files, commit, push, and create PR
                GitHubService.createPullRequest(
                    title = "$taskType: $prompt",
                    body = "Automatically generated by ChatGPTPilot",
                    head = branchName,
                    onSuccess = {
                        showInfo(project, "Pull request created successfully for branch $branchName!")
                    },
                    onFailure = { error ->
                        GitHubService.showErrorDialog(error)
                    }
                )
            },
            onFailure = { error ->
                GitHubService.showErrorDialog(error)
            }
        )
    }

    private fun showInfo(project: Project, message: String) {
        Messages.showInfoMessage(project, message, "ChatGPTPilot")
    }
}
