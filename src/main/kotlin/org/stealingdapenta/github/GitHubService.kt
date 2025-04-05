package org.stealingdapenta.github

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object GitHubService {
    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()

    private fun getToken(): String {
        return GitHubSettingsService.getToken() ?: error("GitHub token is not configured.")
    }

    private fun getRepo(): String {
        return GitHubSettingsService.getRepo() ?: error("GitHub repo is not configured.")
    }

    fun isRepoValid(repo: String, token: String): Boolean {
        val request = Request.Builder().url("https://api.github.com/repos/$repo").addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json").build()

        return try {
            client.newCall(request).execute().use { response ->
                response.code == 200
            }
        } catch (e: Exception) {
            false
        }
    }

    fun createBranch(baseBranch: String = "main", newBranch: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val repo = getRepo()
        val token = getToken()

        val baseUrl = "https://api.github.com/repos/$repo/git/ref/heads/$baseBranch"
        val request = Request.Builder().url(baseUrl).header("Authorization", "Bearer $token").header("Accept", "application/vnd.github+json").build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure("Failed to fetch base branch: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    onFailure("GitHub error: ${response.code} ${response.message}\n$errorBody")
                    return
                }

                val json = objectMapper.readTree(response.body?.string())
                val sha = json["object"]["sha"].asText()

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

        val prUrl = "https://api.github.com/repos/$repo/pulls"
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
                    val errorBody = response.body?.string()
                    onFailure("GitHub error: ${response.code} ${response.message}\n$errorBody")
                }
            }
        })
    }
}