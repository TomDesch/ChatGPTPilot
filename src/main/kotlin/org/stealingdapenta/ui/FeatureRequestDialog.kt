package org.stealingdapenta.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class FeatureRequestDialog : DialogWrapper(true) {
    private val promptField = JBTextField().apply {
        preferredSize = Dimension(400, 28)
    }

    private val typeDropdown = ComboBox(arrayOf("New Feature", "Bugfix", "Improvement", "Code Review"))

    private val panel = JPanel(BorderLayout())

    init {
        title = "New Feature Request for ChatGPT"
        init()

        val formPanel = JPanel(GridLayout(0, 1, 0, 10)).apply {
            add(JLabel("Describe what you want ChatGPT to implement:"))
            add(promptField)
            add(JLabel("Select task type:"))
            add(typeDropdown)
        }

        panel.add(formPanel, BorderLayout.CENTER)
    }

    override fun createCenterPanel(): JComponent = panel

    fun getPrompt(): String = promptField.text.trim()
    fun getType(): String = typeDropdown.selectedItem as String
}
