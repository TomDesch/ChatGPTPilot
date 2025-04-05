package org.stealingdapenta.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBPasswordField
import java.awt.BorderLayout
import java.awt.Desktop
import java.net.URI
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class GitHubTokenDialog : DialogWrapper(true) {
    private val tokenField = JBPasswordField()

    init {
        title = "GitHub Access Token"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val label = JLabel(
            "<html>Paste your <b>GitHub Personal Access Token</b> below.<br><br>" + "You can generate one with the necessary scopes at:<br>" + "<a href='https://github.com/settings/tokens'>https://github.com/settings/tokens</a></html>"
        )

        val openBrowserButton = JButton("Open GitHub Token Page").apply {
            addActionListener {
                Desktop.getDesktop().browse(URI("https://github.com/settings/tokens"))
            }
        }

        val panel = JPanel(BorderLayout(10, 10)).apply {
            add(label, BorderLayout.NORTH)
            add(tokenField, BorderLayout.CENTER)
            add(openBrowserButton, BorderLayout.SOUTH)
        }

        return panel
    }

    fun getToken(): String = String(tokenField.password).trim()
}
