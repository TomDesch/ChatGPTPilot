package org.stealingdapenta.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.json.JSONObject
import org.stealingdapenta.api.ChatGPTClient
import org.stealingdapenta.git.GitIntegration
import org.stealingdapenta.git.ProjectFileWriter
import org.stealingdapenta.github.GitHubService
import org.stealingdapenta.ui.FeaturePromptDialog

class GenerateFeatureAction : AnAction() {

    init {
        templatePresentation.text = "GPT: Create Feature PR"
        templatePresentation.description = "Use ChatGPT to generate and commit a new feature or fix, then open a GitHub pull request."
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dialog = FeaturePromptDialog()
        if (!dialog.showAndGet()) return

        val prompt = dialog.getPrompt().ifBlank {
            showError(project, "⚠️ Prompt cannot be empty.")
            return
        }
        val taskType = dialog.getTaskType()
        val branchName = generateBranchName(taskType)

        GitHubService.createBranch(newBranch = branchName, onSuccess = {
            val checkoutResult = GitIntegration.runGitCommand(project, "checkout", branchName)
            if (!checkoutResult.contains("Switched to")) {
                showError(project, "❌ Could not switch to new branch:\n$checkoutResult")
                return@createBranch
            }

            val systemPrompt = """
                You are a powerful GitHub automation agent.
                The user will describe a change they want made.
                You will generate a valid JSON object where each key is a relative file path (e.g. src/main/.../MyClass.kt)
                and the value is the full contents of that file.
                Output must be raw JSON. Do not include backticks, markdown, or commentary.
            """.trimIndent()

            val userPrompt = """
                Task Type: $taskType
                
                User Request:
                $prompt
                
                Please return all necessary files as JSON.
            """.trimIndent()

            val fullPrompt = """
                [
                  { "role": "system", "content": "$systemPrompt" },
                  { "role": "user", "content": "$userPrompt" }
                ]
            """.trimIndent()

            val response = ChatGPTClient.sendPrompt(fullPrompt)

            try {
                val fileJson = JSONObject(response)

                for (key in fileJson.keys()) {
                    val filePath = key
                    val content = fileJson.getString(key)
                    ProjectFileWriter.writeFile(project, filePath, content)
                }
            } catch (ex: Exception) {
                showError(project, "❌ Failed to parse or write model output.\n\nRaw response:\n$response")
                return@createBranch
            }

            GitIntegration.stageAll(project)
            GitIntegration.commit(project, "$taskType: $prompt")
            val pushOutput = GitIntegration.push(project, branchName)

            if (!pushOutput.contains("Done") && !pushOutput.contains("success")) {
                showError(project, "🚨 Push failed:\n$pushOutput")
                return@createBranch
            }

            GitHubService.createPullRequest(
                title = "$taskType: $prompt",
                body = "This pull request was automatically generated by ChatGPTPilot.\n\nPrompt: $prompt",
                head = branchName,
                onSuccess = {
                    showInfo(project, "✅ Pull request created successfully!")
                },
                onFailure = { error -> showError(project, error) })
        }, onFailure = { error ->
            showError(project, error)
        })
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    private fun generateBranchName(taskType: String): String {
        return "chatgpt/${taskType.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}"
    }

    private fun showInfo(project: Project, message: String) {
        Messages.showInfoMessage(project, message, "ChatGPTPilot")
    }

    private fun showError(project: Project, message: String) {
        Messages.showErrorDialog(project, message, "ChatGPTPilot - Error")
    }
}
