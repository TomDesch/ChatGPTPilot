package org.stealingdapenta.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.Messages
import org.stealingdapenta.api.ChatGPTClient
import org.stealingdapenta.ui.ChatGptPromptDialog
import java.awt.datatransfer.StringSelection

class SendToChatGPTAction : AnAction() {
    init {
        templatePresentation.text = "Send to ChatGPT"
        templatePresentation.description = "Send selected code to ChatGPT for transformation"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val document = editor.document
        val selectionModel = editor.selectionModel

        val selectedText = selectionModel.selectedText ?: run {
            Messages.showInfoMessage(project, "No code selected!", "Send to ChatGPT")
            return
        }

        val dialog = ChatGptPromptDialog(ChatGPTClient.getModelUsed())
        if (!dialog.showAndGet()) return

        val fullPrompt = dialog.getFinalPrompt() + "\n\n```kotlin\n$selectedText\n```"

        val result = ChatGPTClient.sendPrompt(fullPrompt)

        ApplicationManager.getApplication().invokeLater {
            val userChoice = Messages.showDialog(
                project, result, "ChatGPT Response", arrayOf("Copy to Clipboard", "Apply", "Cancel"), 0, Messages.getInformationIcon()
            )

            when (userChoice) {
                0 -> CopyPasteManager.getInstance().setContents(StringSelection(result))
                1 -> WriteCommandAction.runWriteCommandAction(project) {
                    document.replaceString(
                        selectionModel.selectionStart, selectionModel.selectionEnd, result
                    )
                }
                // 2 = Cancel
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
