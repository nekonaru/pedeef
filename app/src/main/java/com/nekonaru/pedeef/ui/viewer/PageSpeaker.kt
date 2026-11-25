package com.nekonaru.pedeef.ui.viewer

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Minimal on-device text-to-speech helper. The platform synthesizer runs offline when voices are installed.
 * Long text is split into utterances: FLUSH the first (clearing anything left from a previous read),
 * ADD the rest, and signal completion only on the last utterance.
 */
class PageSpeaker(context: Context, private val onDone: () -> Unit) {
  private var tts: TextToSpeech? = null
  private var ready = false
  private var pending: String? = null
  private var lastUtterance = ""
  var speaking = false
    private set

  init {
    tts = TextToSpeech(context.applicationContext) { status ->
      ready = status == TextToSpeech.SUCCESS
      if (ready) runCatching { tts?.setLanguage(Locale.getDefault()) }
      val text = pending
      pending = null
      if (text != null) {
        if (ready) speak(text) else { speaking = false; onDoneMain() }
      }
    }
    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(utteranceId: String?) {}
      override fun onDone(utteranceId: String?) { if (utteranceId == lastUtterance) finished() }
      override fun onError(utteranceId: String?) { if (utteranceId == lastUtterance) finished() }
    })
  }

  private fun finished() {
    speaking = false
    onDoneMain()
  }

  /** Utterance callbacks run on a binder thread; Compose state must be written on the main thread. */
  private fun onDoneMain() {
    android.os.Handler(android.os.Looper.getMainLooper()).post(onDone)
  }

  fun speak(text: String) {
    val clean = text.trim()
    if (clean.isEmpty()) { speaking = false; onDoneMain(); return }
    if (!ready) { speaking = true; pending = clean; return }
    val chunks = clean.chunked(3500)
    lastUtterance = "pedeef-page-${chunks.size - 1}"
    speaking = true
    chunks.forEachIndexed { i, chunk ->
      tts?.speak(
        chunk,
        if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
        null,
        "pedeef-page-$i",
      )
    }
  }

  fun stop() {
    tts?.stop()
    speaking = false
    pending = null
  }

  fun close() {
    speaking = false
    pending = null
    tts?.shutdown()
    tts = null
  }
}
