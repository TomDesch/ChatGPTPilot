package org.stealingdapenta.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages
import org.stealingdapenta.api.ChatGPTClient
import org.stealingdapenta.github.GitHubService
import org.stealingdapenta.ui.FeaturePromptDialog

class FeatureGenerationAction : AnAction("Generate Feature via ChatGPT") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dialog = FeaturePromptDialog()
        if (!dialog.showAndGet()) return

        val prompt = dialog.getPrompt()
        val taskType = dialog.getTaskType()
        val branchName = "chatgpt/${taskType.lowercase().replace(" ", "-")}-${System.currentTimeMillis()}"

        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            val taskDescription = "Generate the following $taskType: $prompt"

            // Step 1: Ask ChatGPT what to do
            val chatResponse = ChatGPTClient.sendPrompt(
                "You are a senior developer. Create code changes based on the following task description:\n\n$taskDescription\n\nOnly respond with the code files and changes. No markdown or explanations."
            )

            // Step 2: Create the GitHub branch
            GitHubService.createBranch(
                newBranch = branchName,
                onSuccess = {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showInfoMessage(
                            project,
                            "Branch '$branchName' created. ChatGPT suggestion:\n\n$chatResponse",
                            "Success"
                        )
                        // Step 3 will be handled in next step: Apply changes, commit, push & PR
                    }
                },
                onFailure = {
                    GitHubService.showErrorDialog("Failed to create branch: $it")
                }
            )
        }, "Generating Code with ChatGPT", false, project)
    }

    override fun getActionUpdateThread() = com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
}
