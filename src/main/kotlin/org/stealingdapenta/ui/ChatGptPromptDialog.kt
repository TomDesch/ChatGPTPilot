package org.stealingdapenta.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class ChatGptPromptDialog(private val currentModel: String) : DialogWrapper(true) {

    private val promptField = JBTextField("Refactor this code").apply {
        preferredSize = Dimension(400, 28)
    }

    private val refactorCheckbox = JBCheckBox("Refactor", true)
    private val cleanUpCheckbox = JBCheckBox("Clean up", false)
    private val optimizeCheckbox = JBCheckBox("Optimize", false)
    private val copyToClipboardCheckbox = JBCheckBox("Copy response to clipboard", true)

    private val panel = JPanel(BorderLayout())

    init {
        title = "Send to ChatGPT"
        init()

        val optionsPanel = JPanel(GridLayout(0, 1)).apply {
            add(refactorCheckbox)
            add(cleanUpCheckbox)
            add(optimizeCheckbox)
            add(copyToClipboardCheckbox)
        }

        val form = JPanel(BorderLayout(5, 10)).apply {
            add(JLabel("Prompt:"), BorderLayout.NORTH)
            add(promptField, BorderLayout.CENTER)
            add(optionsPanel, BorderLayout.SOUTH)
        }

        val modelLabel = JLabel("Model: $currentModel").apply {
            horizontalAlignment = SwingConstants.RIGHT
            foreground = UIManager.getColor("Label.disabledForeground")
        }

        val wrapperPanel = JPanel(BorderLayout(10, 10)).apply {
            add(form, BorderLayout.NORTH)
            add(modelLabel, BorderLayout.SOUTH)
        }

        panel.add(wrapperPanel, BorderLayout.NORTH)
    }

    override fun createCenterPanel(): JComponent = panel

    fun getFinalPrompt(): String {
        val base = promptField.text.trim()
        val additions = mutableListOf<String>()
        if (refactorCheckbox.isSelected) additions.add(
            "Refactor the code with modern best practices. Keep behavior unchanged. Apply clean code, SOLID, DRY, KISS, and YAGNI."
        )
        if (cleanUpCheckbox.isSelected) additions.add("Clean up formatting and style.")
        if (optimizeCheckbox.isSelected) additions.add("Optimize performance.")

        return (additions + base).joinToString(". ") + "."
    }

    fun shouldCopyToClipboard(): Boolean = copyToClipboardCheckbox.isSelected
}
