package org.stealingdapenta.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object ChatGPTClient {
    private val client = OkHttpClient()
    private val API_KEY = System.getenv("OPENAI_API_KEY") ?: error("Missing API key")
    private const val DEFAULT_MODEL = "gpt-3.5-turbo"
    private var selectedModel: String = DEFAULT_MODEL

    init {
        try {
            val availableModels = fetchAvailableModels()
            if (availableModels.isNotEmpty()) {
                val bestModel = askGptForBestModel(availableModels)
                if (bestModel in availableModels) {
                    selectedModel = bestModel
                }
            }
        } catch (e: Exception) {
            println("⚠️ Failed to fetch model list. Falling back to $DEFAULT_MODEL. Reason: ${e.message}")
        }
    }

    fun getModelUsed(): String = selectedModel

    private fun fetchAvailableModels(): List<String> {
        val request = Request.Builder().url("https://api.openai.com/v1/models").addHeader("Authorization", "Bearer $API_KEY").build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: return emptyList()
            val data = JSONObject(body).getJSONArray("data")
            return (0 until data.length()).mapNotNull {
                data.getJSONObject(it).optString("id")
            }
        }
    }

    private fun askGptForBestModel(models: List<String>): String {
        val prompt = """
        From this list of models:
        ${models.joinToString(", ")}

        Which one is the best model for IDE code understanding, transformation and refactoring? Return just the model ID.
        """.trimIndent()

        val result = sendPrompt(prompt, DEFAULT_MODEL)
        return result.lines().firstOrNull()?.trim() ?: DEFAULT_MODEL
    }

    fun sendPrompt(prompt: String, modelOverride: String? = null): String {
        val model = modelOverride ?: selectedModel

        val json = JSONObject().put("model", model).put("messages", listOf(JSONObject().put("role", "user").put("content", prompt))).put("temperature", 0.7)

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder().url("https://api.openai.com/v1/chat/completions").addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json").post(body).build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: return "[ERROR: Empty response]"

            val parsed = JSONObject(responseBody)

            if (parsed.has("error")) {
                val errorMessage = parsed.getJSONObject("error").getString("message")
                return "[ERROR from OpenAI]: $errorMessage"
            }

            return parsed.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
        }
    }
}
