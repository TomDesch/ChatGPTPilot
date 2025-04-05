package org.stealingdapenta.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import org.stealingdapenta.api.ChatGPTClient

class SendToChatGPTAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.text = "Send to ChatGPT"
        e.presentation.description = "Send selected code to ChatGPT for suggestions"
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val document = editor.document
        val selection = editor.selectionModel.selectedText ?: return

        val prompt = "Improve this UI code with modern best practices:\n\n$selection"
        val result = ChatGPTClient.sendPrompt(prompt)

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(
                editor.selectionModel.selectionStart, editor.selectionModel.selectionEnd, result
            )
        }

        Messages.showMessageDialog(project, "Refactor applied!", "ChatGPT", Messages.getInformationIcon())
    }
}
