package org.stealingdapenta.github

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.stealingdapenta.github.GitHubSettingsService
import org.stealingdapenta.ui.GitHubRepoDialog
import org.stealingdapenta.ui.GitHubTokenDialog

@Service
@State(name = "GitHubSettings", storages = [Storage("chatgpt-pilot.xml")])
class GitHubSettingsService : PersistentStateComponent<GitHubSettingsService.State> {

    private var state = State()

    class State {
        var token: String? = null
        var repo: String? = null
    }

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun getToken(): String? = state.token
    fun setToken(token: String) {
        state.token = token
    }

    fun getRepo(): String? = state.repo
    fun setRepo(repo: String) {
        state.repo = repo
    }

    fun ensureRepoConfigured(project: Project): Boolean {
        if (getRepo().isNullOrBlank()) {
            val dialog = GitHubRepoDialog()
            if (!dialog.showAndGet()) return false
            setRepo(dialog.getRepo())
        }
        return true
    }

    fun promptForTokenAndRepo(project: Project): Boolean {
        val dialog = GitHubTokenDialog()
        if (!dialog.showAndGet()) return false

        val token = dialog.getToken()
        val repo = dialog.getRepo()

        if (token.isBlank() || repo.isBlank()) {
            Messages.showErrorDialog(project, "Both GitHub token and repository are required.", "ChatGPTPilot")
            return false
        }

        setToken(token)
        setRepo(repo)
        return true
    }


    companion object {
        private fun getInstance(): GitHubSettingsService =
            com.intellij.openapi.application.ApplicationManager.getApplication().getService(GitHubSettingsService::class.java)

        fun getToken(): String? = getInstance().getToken()
        fun setToken(token: String) = getInstance().setToken(token)
        fun getRepo(): String? = getInstance().getRepo()
        fun setRepo(repo: String) = getInstance().setRepo(repo)
        fun ensureRepoConfigured(project: Project): Boolean {
            return getInstance().ensureRepoConfigured(project)
        }
        fun promptForTokenAndRepo(project: Project): Boolean {
            return getInstance().promptForTokenAndRepo(project)
        }
    }
}
