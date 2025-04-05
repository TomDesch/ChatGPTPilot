package org.stealingdapenta.github

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

object GitHubFileCommitService {
    private val client = OkHttpClient()
    private val mapper = ObjectMapper()

    fun commitFile(
        path: String,
        content: String,
        commitMessage: String,
        branch: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val repo = GitHubSettingsService.getRepo() ?: return onFailure("Repo not configured.")
        val token = GitHubSettingsService.getToken() ?: return onFailure("GitHub token not configured.")

        val url = "https://api.github.com/repos/$repo/contents/$path"

        // Encode content in Base64
        val encodedContent = Base64.getEncoder().encodeToString(content.toByteArray())

        // Build request body
        val json = mapper.createObjectNode().apply {
            put("message", commitMessage)
            put("content", encodedContent)
            put("branch", branch)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .put(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                onFailure("Failed to commit file: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("GitHub commit failed: ${response.code} ${response.message}")
                }
            }
        })
    }
}
