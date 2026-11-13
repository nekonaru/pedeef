package com.nekonaru.pedeef.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** App settings, persisted as JSON in filesDir. */
@Serializable
data class SettingsState(
  val themeMode: String = "system", // system | light | dark
  val scheme: String = "ember", // ember | cherry | arctic
  val dynamicColor: Boolean = false,
  val language: String = "system", // system | en | id
  val onboardingDone: Boolean = false,
)

object Settings {
  private val json = Json { ignoreUnknownKeys = true }

  private fun fileOf(appContext: android.content.Context) = File(appContext.filesDir, "settings.json")

  fun load(appContext: android.content.Context): SettingsState =
    runCatching { json.decodeFromString<SettingsState>(fileOf(appContext).readText()) }.getOrDefault(SettingsState())

  fun save(appContext: android.content.Context, state: SettingsState) {
    runCatching { fileOf(appContext).writeText(json.encodeToString(state)) }
  }
}
