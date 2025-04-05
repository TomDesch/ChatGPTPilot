package org.stealingdapenta.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


object ChatGPTClient {
    private val client = OkHttpClient()
    private val API_KEY = System.getenv("OPENAI_API_KEY") ?: error("Missing API key")

    fun sendPrompt(prompt: String): String {
        val json = JSONObject().put("model", "gpt-4").put("messages", listOf(JSONObject().put("role", "user").put("content", prompt))).put("temperature", 0.7)

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder().url("https://api.openai.com/v1/chat/completions").addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json").post(body).build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: return "[ERROR: Empty response]"
            val parsed = JSONObject(responseBody)
            return parsed.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
        }
    }
}
