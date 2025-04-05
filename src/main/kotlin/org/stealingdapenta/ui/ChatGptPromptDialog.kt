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

    private val checkboxes = listOf(
        JBCheckBox("Refactor", true), JBCheckBox("Optimize", true), JBCheckBox("Rework (specify!)", true)
    )

    private val panel = JPanel(BorderLayout())

    init {
        title = "Send to ChatGPT"
        init()

        val optionsPanel = JPanel(GridLayout(0, 1)).apply {
            checkboxes.forEach { add(it) }
        }

        val formPanel = JPanel(BorderLayout(5, 10)).apply {
            add(JLabel("Prompt:"), BorderLayout.NORTH)
            add(promptField, BorderLayout.CENTER)
            add(optionsPanel, BorderLayout.SOUTH)
        }

        val modelLabel = JLabel("Model: $currentModel").apply {
            horizontalAlignment = SwingConstants.RIGHT
            foreground = UIManager.getColor("Label.disabledForeground")
        }

        panel.add(JPanel(BorderLayout(10, 10)).apply {
            add(formPanel, BorderLayout.NORTH)
            add(modelLabel, BorderLayout.SOUTH)
        }, BorderLayout.NORTH)
    }

    override fun createCenterPanel(): JComponent = panel

    fun getFinalPrompt(): String {
        val base = promptField.text.trim()
        val additions = buildList {
            if (checkboxes[0].isSelected) add(
                "Refactor the following code using modern software engineering principles. Improve code structure, naming, readability, and maintainability without altering the original functionality. Apply best practices such as Clean Code, SOLID principles (Single Responsibility, Open/Closed, etc.), DRY (Don't Repeat Yourself), KISS (Keep It Simple, Stupid), and YAGNI (You Aren’t Gonna Need It). Avoid unnecessary comments unless they clarify non-obvious logic. The final result should be production-quality and idiomatic for the target language."
            )
            if (checkboxes[1].isSelected) add(
                "Analyze and optimize the performance of the provided code. Focus on improving runtime efficiency, reducing unnecessary memory usage, minimizing CPU cycles, and eliminating redundant computations. Maintain identical functional behavior. Suggest or apply algorithmic improvements, caching opportunities, or simplifications where applicable. Only refactor where it yields measurable performance gain without introducing complexity."
            )
            if (checkboxes[2].isSelected) add(
                "Rework the code based on the custom prompt provided by the user. Follow the intent described and transform the code accordingly while preserving its core logic unless otherwise specified. Use best practices, idiomatic constructs, and concise formatting. Ensure the output is clean, coherent, and tailored to the user's specified use case or transformation."
            )
            add("Always return only the improved code. Do NOT wrap it in markdown, triple backticks, or language annotations.")
        }

        return (additions + base).joinToString(". ") + "."
    }
}
