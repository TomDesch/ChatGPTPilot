package org.stealingdapenta.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class FeaturePromptDialog : DialogWrapper(true) {

    private val promptField = JBTextField().apply {
        preferredSize = Dimension(400, 28)
        toolTipText = "Describe the feature, fix, or improvement you want ChatGPT to generate"
    }

    private val taskTypes = arrayOf("New Feature", "Bugfix", "Improvement", "Code Review", "Documentation")
    private val taskTypeSelector = ComboBox(taskTypes)

    private val panel = JPanel(BorderLayout(10, 10))

    init {
        title = "Describe Your Task"
        init()

        val form = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JLabel("Task Type:"))
            add(taskTypeSelector)
            add(Box.createRigidArea(Dimension(0, 10)))
            add(JLabel("Prompt:"))
            add(promptField)
        }

        form.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        panel.add(form, BorderLayout.CENTER)
    }

    override fun createCenterPanel(): JComponent = panel

    fun getPrompt(): String = promptField.text.trim()
    fun getTaskType(): String = taskTypeSelector.selectedItem as String
}
