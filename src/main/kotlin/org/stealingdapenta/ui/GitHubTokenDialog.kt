package org.stealingdapenta.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Desktop
import java.net.URI
import javax.swing.*

class GitHubTokenDialog : DialogWrapper(true) {
    private val tokenField = JBPasswordField()
    private val repoField = JBTextField()

    init {
        title = "Configure GitHub Access"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))

        val label = JLabel(
            """
            <html>
                Provide your <b>GitHub Personal Access Token</b> and repository.
                <br><br>
                You can generate a token with <code>repo</code> scope at:<br>
                <a href='https://github.com/settings/tokens'>https://github.com/settings/tokens</a>
            </html>
            """.trimIndent()
        )

        val openBrowserButton = JButton("Open Token Page").apply {
            addActionListener {
                Desktop.getDesktop().browse(URI("https://github.com/settings/tokens"))
            }
        }

        val formPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JLabel("GitHub Token:"))
            add(tokenField)
            add(Box.createVerticalStrut(10))
            add(JLabel("Repository (owner/repo):"))
            add(repoField)
        }

        panel.add(label, BorderLayout.NORTH)
        panel.add(formPanel, BorderLayout.CENTER)
        panel.add(openBrowserButton, BorderLayout.SOUTH)

        return panel
    }

    fun getToken(): String = String(tokenField.password).trim()
    fun getRepo(): String = repoField.text.trim()
}
