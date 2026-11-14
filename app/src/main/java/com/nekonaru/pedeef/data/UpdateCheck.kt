package com.nekonaru.pedeef.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.net.HttpURLConnection
import java.net.URL

/** Result of an update check. */
sealed class UpdateResult {
  data class UpToDate(val tag: String) : UpdateResult()
  data class UpdateAvailable(val tag: String) : UpdateResult()
  data class Failed(val reason: String) : UpdateResult()
}

/**
 * Asks GitHub for the latest release tag. One GET request, ~10 s timeout, nothing is sent except the request itself.
 * Runs only when the user taps the version row.
 */
object UpdateCheck {
  private val json = Json { ignoreUnknownKeys = true }
  private const val LATEST = "https://api.github.com/repos/nekonaru/pedeef/releases/latest"

  fun compareTag(latest: String, current: String): Int {
    fun parts(s: String) = s.trim().removePrefix("v").split(".").mapNotNull { it.takeWhile(Char::isDigit).toIntOrNull() }
    val a = parts(latest); val b = parts(current)
    for (i in 0 until maxOf(a.size, b.size)) {
      val d = (a.getOrNull(i) ?: 0).compareTo(b.getOrNull(i) ?: 0)
      if (d != 0) return d
    }
    return 0
  }

  suspend fun check(currentVersion: String): UpdateResult = withContext(Dispatchers.IO) {
    try {
      val conn = (URL(LATEST).openConnection() as HttpURLConnection).apply {
        connectTimeout = 10_000
        readTimeout = 10_000
        setRequestProperty("Accept", "application/vnd.github+json")
        setRequestProperty("User-Agent", "Pedeef-Android")
      }
      val code = conn.responseCode
      if (code != 200) return@withContext UpdateResult.Failed("GitHub returned $code")
      val body = conn.inputStream.use { it.readBytes() }.decodeToString()
      val tag = json.parseToJsonElement(body).jsonObject["tag_name"]?.jsonPrimitive?.contentOrNull ?: return@withContext UpdateResult.Failed("no tag")
      if (compareTag(tag, currentVersion) > 0) UpdateResult.UpdateAvailable(tag) else UpdateResult.UpToDate(tag)
    } catch (e: Exception) {
      UpdateResult.Failed(e.message ?: "network error")
    }
  }
}
