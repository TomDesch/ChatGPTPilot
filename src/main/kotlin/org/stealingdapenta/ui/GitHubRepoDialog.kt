package org.stealingdapenta.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class GitHubRepoDialog : DialogWrapper(true) {
    private val repoField = JBTextField().apply {
        toolTipText = "Example: your-username/your-repo"
    }

    init {
        title = "GitHub Repository"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10)).apply {
            add(JLabel("Enter your GitHub repository (e.g. username/repo):"), BorderLayout.NORTH)
            add(repoField, BorderLayout.CENTER)
        }
        return panel
    }

    fun getRepo(): String = repoField.text.trim()
}
