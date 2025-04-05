package org.stealingdapenta.github

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

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

    companion object {
        private fun getInstance(): GitHubSettingsService =
            com.intellij.openapi.application.ApplicationManager
                .getApplication()
                .getService(GitHubSettingsService::class.java)

        fun getToken(): String? = getInstance().getToken()
        fun getRepo(): String? = getInstance().getRepo()
    }
}
