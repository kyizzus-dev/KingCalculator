package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ChatMessage(
  val id: String = System.currentTimeMillis().toString() + (100..999).random(),
  val role: String, // "user" or "model"
  val text: String,
  val imageBitmap: Bitmap? = null,
  val timestamp: Long = System.currentTimeMillis()
)

object GeminiService {

  private const val MODEL_NAME = "gemini-3.8-flash"
  private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private const val SYSTEM_INSTRUCTION =
    "Eres el Tutor Matemático Inteligente de King Calculator (v1.2). " +
    "Tu función es analizar problemas matemáticos en fotos o texto y explicarlos paso a paso de forma didáctica, clara, limpia y comprensible para cualquier estudiante.\n\n" +
    "REGLAS ESTRICTAS DE FORMATO:\n" +
    "1. PROHIBIDO USAR HASHTAGS (#, ##, ###, ####) para títulos, subtítulos o encabezados. No coloques el símbolo '#' en ningún lugar.\n" +
    "2. PROHIBIDO USAR ASTERISCOS DE FORMATO (**, ***) para negritas. Escribe texto natural y limpio.\n" +
    "3. PROHIBIDO USAR CÓDIGO LATEX NI SIGNOS DE DÓLAR ($ o $$). No uses \\frac, \\times, \\cdot, \\text{}, \\boxed{}, \\begin, \\end, etc.\n" +
    "4. Escribe todas las fórmulas en formato de texto plano y legible usando caracteres estándar: '+', '-', '*', '/', '=', '·', 'x²', '√()', '(', ')'.\n" +
    "5. Organiza SIEMPRE la respuesta con esta estructura clara:\n" +
    "Problema: (breve resumen del enunciado)\n" +
    "Paso 1: (título del paso)\n" +
    "(explicación y cálculo)\n" +
    "Paso 2: (título del paso)\n" +
    "(explicación y cálculo)\n" +
    "...\n" +
    "Resultado Final: (la respuesta definitiva destacada claramente)"

  /**
   * Sends multi-turn chat messages with optional image attachment to Gemini 3.8 Flash.
   */
  suspend fun sendChat(
    history: List<ChatMessage>,
    newPrompt: String,
    imageBitmap: Bitmap? = null
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val apiKey = BuildConfig.GEMINI_API_KEY
      if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext Result.failure(
          IllegalStateException(
            "La API Key de Gemini no está configurada. Por favor, añade tu clave GEMINI_API_KEY en el panel de Secrets de AI Studio."
          )
        )
      }

      val requestJson = JSONObject()

      // System instruction
      val sysInstructionObj = JSONObject()
      val sysParts = JSONArray()
      sysParts.put(JSONObject().put("text", SYSTEM_INSTRUCTION))
      sysInstructionObj.put("parts", sysParts)
      requestJson.put("systemInstruction", sysInstructionObj)

      // Contents array for multi-turn history
      val contentsArray = JSONArray()

      // Add previous conversation turns (limit to last 10 for performance and token control)
      val recentHistory = history.takeLast(10)
      for (msg in recentHistory) {
        val contentObj = JSONObject()
        contentObj.put("role", if (msg.role == "model") "model" else "user")
        val parts = JSONArray()
        if (msg.imageBitmap != null) {
          val inlineData = JSONObject()
          inlineData.put("mimeType", "image/jpeg")
          inlineData.put("data", bitmapToBase64(msg.imageBitmap))
          parts.put(JSONObject().put("inlineData", inlineData))
        }
        parts.put(JSONObject().put("text", msg.text))
        contentObj.put("parts", parts)
        contentsArray.put(contentObj)
      }

      // Add the new user message turn
      val newContentObj = JSONObject()
      newContentObj.put("role", "user")
      val newParts = JSONArray()
      if (imageBitmap != null) {
        val inlineData = JSONObject()
        inlineData.put("mimeType", "image/jpeg")
        inlineData.put("data", bitmapToBase64(imageBitmap))
        newParts.put(JSONObject().put("inlineData", inlineData))
      }
      val promptText = if (newPrompt.isNotBlank()) newPrompt else "Por favor escanea, analiza y resuelve paso a paso el problema de esta imagen de forma limpia, sin hashtags ni código LaTeX, con el resultado final claro."
      newParts.put(JSONObject().put("text", promptText))
      newContentObj.put("parts", newParts)
      contentsArray.put(newContentObj)

      requestJson.put("contents", contentsArray)

      // Generation config
      val config = JSONObject()
      config.put("temperature", 0.3)
      config.put("topP", 0.95)
      requestJson.put("generationConfig", config)

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = requestJson.toString().toRequestBody(mediaType)

      val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
      val httpRequest = Request.Builder()
        .url(url)
        .post(body)
        .build()

      val response = okHttpClient.newCall(httpRequest).execute()
      val responseBodyString = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        val errorMsg = try {
          val errorJson = JSONObject(responseBodyString).optJSONObject("error")
          errorJson?.optString("message") ?: "Error del servidor Gemini (${response.code})"
        } catch (e: Exception) {
          "Error en la petición: Código HTTP ${response.code}"
        }
        return@withContext Result.failure(Exception(errorMsg))
      }

      val json = JSONObject(responseBodyString)
      val candidates = json.optJSONArray("candidates")
      if (candidates == null || candidates.length() == 0) {
        return@withContext Result.failure(Exception("Gemini no generó ninguna respuesta."))
      }

      val firstCandidate = candidates.getJSONObject(0)
      val content = firstCandidate.optJSONObject("content")
      val parts = content?.optJSONArray("parts")
      val textBuilder = StringBuilder()

      if (parts != null) {
        for (i in 0 until parts.length()) {
          val part = parts.getJSONObject(i)
          val text = part.optString("text")
          if (text.isNotEmpty()) {
            textBuilder.append(text)
          }
        }
      }

      val rawText = textBuilder.toString()
      if (rawText.isBlank()) {
        Result.failure(Exception("Respuesta vacía recibida de Gemini."))
      } else {
        val cleanedText = MathTextCleaner.clean(rawText)
        Result.success(cleanedText)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    // Scale down if extremely large to maintain responsiveness and avoid high token payloads
    val scaled = if (bitmap.width > 1280 || bitmap.height > 1280) {
      val ratio = 1280f / Math.max(bitmap.width, bitmap.height)
      Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
    } else {
      bitmap
    }
    scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
  }
}
