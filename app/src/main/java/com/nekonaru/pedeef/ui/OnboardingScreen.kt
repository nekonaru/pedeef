package com.nekonaru.pedeef.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.ui.theme.Caveat
import com.nekonaru.pedeef.ui.kit.Btn

@Composable
fun OnboardingScreen(onDone: () -> Unit, onLanguage: (String) -> Unit, language: String) {
  val s = LocalPedeefStrings.current
  val indonesian = s === Id
  Column(
    Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.surface)
      .padding(28.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
  ) {
    Text("Pedeef", fontFamily = Caveat, fontSize = androidx.compose.ui.unit.TextUnit(88f, androidx.compose.ui.unit.TextUnitType.Sp), color = MaterialTheme.colorScheme.primary)
    Text(s.onboardingTitle, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
    Text(s.onboardingBody, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      BulletRow(Icons.Default.Description, if (indonesian) "Semua alat PDF penting di satu aplikasi." else "Every important PDF tool in one app.")
      BulletRow(Icons.Default.Wifi, if (indonesian) "Offline total. Tidak ada akun dan tidak ada izin jaringan." else "Fully offline. No account, no network permission.")
      BulletRow(Icons.Default.Lock, if (indonesian) "Filemu tetap di ponselmu. Titik." else "Your files stay on your phone. Period.")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      LanguageChip("English", selected = language != "id") { onLanguage("en") }
      LanguageChip("Bahasa Indonesia", selected = language == "id") { onLanguage("id") }
    }

    Btn(text = s.onboardingStart, onClick = onDone, modifier = Modifier.fillMaxWidth())
  }
}

@Composable
private fun BulletRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(36.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
    }
    Text(text, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
  }
}

@Composable
private fun LanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
  Surface(
    onClick = onClick,
    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
      if (selected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
      Text(label, modifier = Modifier.padding(start = if (selected) 6.dp else 0.dp), color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
