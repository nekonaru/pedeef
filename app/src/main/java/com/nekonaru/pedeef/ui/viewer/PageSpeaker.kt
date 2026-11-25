package com.nekonaru.pedeef.ui.viewer

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** Minimal on-device text-to-speech helper; the platform synthesizer runs offline when voices are installed. */
class PageSpeaker(context: Context, private val onDone: () -> Unit) {
  private var tts: TextToSpeech? = null
  private var ready = false
  private var pending: String? = null
  var speaking = false
    private set

  init {
    tts = TextToSpeech(context.applicationContext) { status ->
      ready = status == TextToSpeech.SUCCESS
      if (ready) runCatching { tts?.setLanguage(Locale.getDefault()) }
      pending?.let { if (ready) speakNow(it) else onDone() }
      pending = null
    }
    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(utteranceId: String?) {}
      override fun onDone(utteranceId: String?) { speaking = false; onDone() }
      override fun onError(utteranceId: String?) { speaking = false; onDone() }
    })
  }

  fun speak(text: String) {
    val clean = text.trim()
    if (clean.isEmpty()) { onDone(); return }
    // TextToSpeech chunks speech internally, but very long input is split to avoid a single huge utterance.
    val chunks = clean.chunked(3500)
    speaking = true
    if (!ready) { pending = chunks.first(); return }
    chunks.forEachIndexed { i, chunk ->
      val flush = i == chunks.size - 1
      tts?.speak(chunk, if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "pedeef-page-$i")
    }
  }

  private fun speakNow(text: String) {
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pedeef-page-0")
  }

  fun stop() {
    tts?.stop()
    speaking = false
    pending = null
  }

  fun close() { tts?.shutdown() }
}
