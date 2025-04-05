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
    private val optimizeCheckbox = JBCheckBox("Optimize", true)
    private val reworkCheckbox = JBCheckBox("Rework (specify!)", true)

    private val panel = JPanel(BorderLayout())

    init {
        title = "Send to ChatGPT"
        init()

        val optionsPanel = JPanel(GridLayout(0, 1)).apply {
            add(refactorCheckbox)
            add(optimizeCheckbox)
            add(reworkCheckbox)
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
            "Refactor the following code using modern software engineering principles. Improve code structure, naming, readability, and maintainability without altering the original functionality. Apply best practices such as Clean Code, SOLID principles (Single Responsibility, Open/Closed, etc.), DRY (Don't Repeat Yourself), KISS (Keep It Simple, Stupid), and YAGNI (You Aren’t Gonna Need It). Avoid unnecessary comments unless they clarify non-obvious logic. The final result should be production-quality and idiomatic for the target language."
        )

        if (optimizeCheckbox.isSelected) additions.add(
            "Analyze and optimize the performance of the provided code. Focus on improving runtime efficiency, reducing unnecessary memory usage, minimizing CPU cycles, and eliminating redundant computations. Maintain identical functional behavior. Suggest or apply algorithmic improvements, caching opportunities, or simplifications where applicable. Only refactor where it yields measurable performance gain without introducing complexity."
        )
        if (reworkCheckbox.isSelected) additions.add(
            "Rework the code based on the custom prompt provided by the user. Follow the intent described and transform the code accordingly while preserving its core logic unless otherwise specified. Use best practices, idiomatic constructs, and concise formatting. Ensure the output is clean, coherent, and tailored to the user's specified use case or transformation."
        )

        additions.add("Always return only the improved code. Do NOT wrap it in markdown, triple backticks, or language annotations.")

        return (additions + base).joinToString(". ") + "."
    }
}
