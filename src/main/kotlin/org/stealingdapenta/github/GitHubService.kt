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

    fun createBranch(
        newBranch: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val repo = getRepo()
        val token = getToken()

        fun requestBaseSha(branch: String, onResult: (String?) -> Unit) {
            val url = "https://api.github.com/repos/$repo/git/ref/heads/$branch"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onResult(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (!response.isSuccessful || body == null) {
                        onResult(null)
                        return
                    }

                    val sha = try {
                        objectMapper.readTree(body)["object"]["sha"].asText()
                    } catch (e: Exception) {
                        null
                    }

                    onResult(sha)
                }
            })
        }

        fun attemptCreateBranchWith(baseSha: String) {
            val createUrl = "https://api.github.com/repos/$repo/git/refs"
            val bodyJson = objectMapper.createObjectNode().apply {
                put("ref", "refs/heads/$newBranch")
                put("sha", baseSha)
            }

            val body = bodyJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val createRequest = Request.Builder()
                .url(createUrl)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .post(body)
                .build()

            client.newCall(createRequest).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure("❌ Failed to create branch: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("❌ GitHub error creating branch: ${response.code} ${response.message}")
                    }
                }
            })
        }

        // Try "main" first, fallback to "master"
        requestBaseSha("main") { mainSha ->
            if (mainSha != null) {
                attemptCreateBranchWith(mainSha)
            } else {
                requestBaseSha("master") { masterSha ->
                    if (masterSha != null) {
                        attemptCreateBranchWith(masterSha)
                    } else {
                        onFailure("❌ Could not resolve base branch (main/master) for repo: $repo")
                    }
                }
            }
        }
    }


    fun createPullRequest(title: String, body: String, head: String, base: String = "master", onSuccess: () -> Unit, onFailure: (String) -> Unit) {
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