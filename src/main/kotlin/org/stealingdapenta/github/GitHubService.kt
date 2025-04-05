package org.stealingdapenta.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object GitHubService {
    private const val BASE_API = "https://api.github.com/repos"

    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()
    private fun getToken(): String {
        return GitHubSettingsService.getToken() ?: error("GitHub token is not configured.")
    }

    private fun getRepo(): String {
        return GitHubSettingsService.getRepo() ?: error("GitHub repo is not configured.")
    }

    fun createBranch(baseBranch: String = "main", newBranch: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val repo = getRepo()
        val token = getToken()

        // Step 1: Get the latest commit SHA of base branch
        val baseUrl = "$BASE_API/$repo/git/ref/heads/$baseBranch"
        val request = Request.Builder().url(baseUrl).header("Authorization", "Bearer $token").header("Accept", "application/vnd.github+json").build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure("Failed to fetch base branch: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val errorMsg = response.body?.string()?.let {
                        objectMapper.readTree(it)?.get("message")?.asText()
                    } ?: response.message
                    onFailure("GitHub error: $errorMsg")
                    return
                }

                val json = objectMapper.readTree(response.body?.string())
                val sha = json["object"]["sha"].asText()

                // Step 2: Create new branch ref
                val createUrl = "https://api.github.com/repos/$repo/git/refs"
                val bodyJson = objectMapper.createObjectNode().apply {
                    put("ref", "refs/heads/$newBranch")
                    put("sha", sha)
                }

                val body = bodyJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val createRequest =
                    Request.Builder().url(createUrl).header("Authorization", "Bearer $token").header("Accept", "application/vnd.github+json").post(body).build()

                client.newCall(createRequest).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onFailure("Failed to create branch: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            onSuccess()
                        } else {
                            onFailure("GitHub error while creating branch: ${response.message}")
                        }
                    }
                })
            }
        })
    }

    fun createPullRequest(title: String, body: String, head: String, base: String = "main", onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val repo = getRepo()
        val token = getToken()

        val prUrl = "$BASE_API/$repo/pulls"
        val prJson = objectMapper.createObjectNode().apply {
            put("title", title)
            put("body", body)
            put("head", head)
            put("base", base)
        }

        val requestBody = prJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request =
            Request.Builder().url(prUrl).header("Authorization", "Bearer $token").header("Accept", "application/vnd.github+json").post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure("Failed to create pull request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMsg = response.body?.string()?.let {
                        objectMapper.readTree(it)?.get("message")?.asText()
                    } ?: response.message
                    onFailure("GitHub error: $errorMsg")
                }
            }
        })
    }

    fun showErrorDialog(message: String) {
        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(message, "GitHub API Error")
        }
    }
}