package com.nekonaru.pedeef

import com.nekonaru.pedeef.data.Operation
import com.nekonaru.pedeef.data.SettingsState
import com.nekonaru.pedeef.data.Workflow
import com.nekonaru.pedeef.engine.WorkflowStep
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Data formats are pure JSON, so migration and round trips can be checked on the JVM. */
class V3DataTest {
  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun workflowRoundTrip() {
    val workflow = Workflow(
      id = "w1",
      name = "Compress and merge",
      steps = listOf(WorkflowStep("s1", "compress", Json.parseToJsonElement("{}") as kotlinx.serialization.json.JsonObject)),
    )
    val decoded = json.decodeFromString<Workflow>(json.encodeToString(workflow))
    assertEquals("w1", decoded.id)
    assertEquals(1, decoded.steps.size)
    assertEquals("compress", decoded.steps[0].op)
  }

  @Test
  fun oldSettingsStillLoad() {
    val old = """{"themeMode":"dark","scheme":"arctic","language":"id","onboardingDone":true}"""
    val settings = json.decodeFromString<SettingsState>(old)
    assertEquals("dark", settings.themeMode)
    assertEquals("id", settings.language)
    assertEquals("system", settings.motion)
    assertEquals(30, settings.autoCleanupDays)
  }

  @Test
  fun operationRoundTrip() {
    val op = Operation("o1", "merge", "Merge PDFs", 123L, listOf("merged.pdf"))
    val decoded = json.decodeFromString<Operation>(json.encodeToString(op))
    assertEquals("merge", decoded.toolId)
    assertTrue(decoded.outputs.contains("merged.pdf"))
  }
}
