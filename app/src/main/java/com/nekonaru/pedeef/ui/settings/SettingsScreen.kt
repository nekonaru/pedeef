package com.nekonaru.pedeef.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.data.SettingsState
import com.nekonaru.pedeef.i18n.En
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import androidx.compose.material.icons.filled.Check
import com.nekonaru.pedeef.ui.kit.ConfirmDialog
import com.nekonaru.pedeef.ui.kit.Group
import com.nekonaru.pedeef.ui.kit.GroupLabel
import com.nekonaru.pedeef.ui.kit.InfoDialog
import com.nekonaru.pedeef.ui.kit.ListRow
import com.nekonaru.pedeef.ui.kit.fmtSize
import java.io.File

private const val VERSION = "1.0.0"

private const val PRIVACY_EN = """Pedeef is built to be private:

1. Every tool, the scanner and OCR run on your phone. No file ever leaves the device unless you share it yourself or save a copy through the system file picker.
2. Pedeef does not have a network permission, an account, ads or analytics. There is no server to send data to.
3. Files you create are stored inside the app's private storage, visible only to Pedeef. You can delete them anytime from Settings.
4. The optional document scanner uses the Google Play services document scanner. Images it captures stay on your phone and are processed into PDFs by Pedeef.
5. You can ask for your data deletion by deleting the app. Pedeef stores nothing anywhere else.

Last updated: November 2026. Contact: nekonaru on GitHub."""

private const val PRIVACY_ID = """Pedeef dibuat untuk menjaga privasi:

1. Semua alat, pemindai, dan OCR berjalan di ponselmu. File tidak pernah keluar dari perangkat kecuali kamu sendiri yang membagikannya atau menyalinnya lewat file picker sistem.
2. Pedeef tidak memiliki izin jaringan, akun, iklan, atau analitik. Tidak ada server untuk mengirim data.
3. File yang kamu buat disimpan di penyimpanan privat aplikasi, hanya terlihat oleh Pedeef. Kamu bisa menghapusnya kapan pun dari Pengaturan.
4. Pemindai dokumen opsional memakai pemindai dokumen Google Play services. Gambar yang diambil tetap di ponselmu dan diproses menjadi PDF oleh Pedeef.
5. Kamu bisa meminta penghapusan data dengan menghapus aplikasi. Pedeef tidak menyimpan apa pun di tempat lain.

Terakhir diperbarui: November 2026. Kontak: nekonaru di GitHub."""

private const val LICENSES_EN = """Pedeef is MIT licensed. It stands on these open source projects:

- Apache PDFBox (via PdfBox-Android, Tom Roush) - Apache 2.0
- Jetpack Compose, Material 3, Activity, Navigation, Lifecycle - Apache 2.0
- kotlinx.coroutines, kotlinx.serialization - Apache 2.0
- ML Kit Text Recognition - Google ML Kit terms
- ML Kit Document Scanner (Google Play services) - Google Play services terms
- Poppins font (Indian Type Foundry) - SIL Open Font License 1.1
- Caveat font (Impallari Type) - SIL Open Font License 1.1

See THIRD_PARTY_NOTICES.md in the repository for full notices."""

private const val LICENSES_ID = """Pedeef berlisensi MIT. Pedeef berdiri di atas proyek open source berikut:

- Apache PDFBox (via PdfBox-Android, Tom Roush) - Apache 2.0
- Jetpack Compose, Material 3, Activity, Navigation, Lifecycle - Apache 2.0
- kotlinx.coroutines, kotlinx.serialization - Apache 2.0
- ML Kit Text Recognition - Google ML Kit terms
- ML Kit Document Scanner (Google Play services) - Google Play services terms
- Font Poppins (Indian Type Foundry) - SIL Open Font License 1.1
- Font Caveat (Impallari Type) - SIL Open Font License 1.1

Lihat THIRD_PARTY_NOTICES.md di repositori untuk pemberitahuan lengkap."""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settings: SettingsState, update: ((SettingsState) -> SettingsState) -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === Id
  val context = LocalContext.current
  val root = remember { FilesStore.root(context) }
  var refresh by remember { mutableStateOf(0) }
  var showClear by remember { mutableStateOf(false) }
  var showPrivacy by remember { mutableStateOf(false) }
  var showLicenses by remember { mutableStateOf(false) }

  val totalSize = remember(refresh) { FilesStore.totalSize(root) }
  val fileCount = remember(refresh) { FilesStore.countIn(root) }

  fun mode() = settings.themeMode
  fun scheme() = settings.scheme

  Scaffold(topBar = { TopAppBar(title = { Text(s.settingsTitle) }) }) { padding ->
    Column(
      Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      GroupLabel(s.appearance)
      Group {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(if (indonesian) "Mode" else "Mode", modifier = Modifier.weight(1f))
          SingleChoiceSegmentedButtonRow {
            listOf("system" to s.themeSystem, "light" to s.themeLight, "dark" to s.themeDark).forEachIndexed { i, (modeKey, label) ->
              SegmentedButton(
                selected = mode() == modeKey,
                onClick = { update { it.copy(themeMode = modeKey) } },
                shape = SegmentedButtonDefaults.itemShape(i, 3),
              ) { Text(label) }
            }
          }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(s.scheme, modifier = Modifier.weight(1f))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SchemeDot(0xFFB8471F.toInt(), scheme() == "ember") { update { it.copy(scheme = "ember") } }
            SchemeDot(0xFF8E2A3E.toInt(), scheme() == "cherry") { update { it.copy(scheme = "cherry") } }
            SchemeDot(0xFF17567E.toInt(), scheme() == "arctic") { update { it.copy(scheme = "arctic") } }
          }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
          Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Text(s.dynamicColor)
              Text(s.dynamicColorHint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = settings.dynamicColor, onCheckedChange = { v -> update { it.copy(dynamicColor = v) } })
          }
        }
      }

      GroupLabel(s.language)
      Group {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(s.language, modifier = Modifier.weight(1f))
          SingleChoiceSegmentedButtonRow {
            listOf("system" to s.themeSystem, "en" to "English", "id" to "Indonesia").forEachIndexed { i, (langKey, label) ->
              SegmentedButton(
                selected = settings.language == langKey,
                onClick = { update { it.copy(language = langKey) } },
                shape = SegmentedButtonDefaults.itemShape(i, 3),
              ) { Text(label) }
            }
          }
        }
      }

      GroupLabel(s.filesTitle)
      Group {
        ListRow(Icons.Default.FolderOpen, if (indonesian) "File tersimpan" else "Saved files", "$fileCount " + (if (indonesian) "file" else "files") + " • " + fmtSize(totalSize))
        ListRow(
          Icons.Default.Delete,
          s.clearAppData,
          null,
          onClick = { showClear = true },
          trailing = null,
        )
      }

      GroupLabel(s.about)
      Group {
        ListRow(Icons.Default.Info, if (indonesian) "Versi" else "Version", "Pedeef v$VERSION")
        ListRow(Icons.Default.PrivacyTip, s.privacyPolicy, null, onClick = { showPrivacy = true })
        ListRow(Icons.Default.Eco, s.openSource, null, onClick = { showLicenses = true })
      }
      Box(Modifier.padding(bottom = 32.dp))
    }
  }

  if (showClear) {
    ConfirmDialog(
      title = s.clearAppData,
      body = s.clearAppDataConfirm,
      confirm = s.delete,
      dismiss = s.cancel,
      danger = true,
      onConfirm = {
        root.walkTopDown().filter { it.isFile }.forEach { runCatching { it.delete() } }
        root.walkTopDown().filter { it.isDirectory && it != root }.sortedByDescending { it.depth }.forEach { runCatching { it.delete() } }
        refresh++
        showClear = false
      },
      onDismiss = { showClear = false },
    )
  }
  if (showPrivacy) InfoDialog(s.privacyPolicy, if (indonesian) PRIVACY_ID else PRIVACY_EN) { showPrivacy = false }
  if (showLicenses) InfoDialog(s.openSource, if (indonesian) LICENSES_ID else LICENSES_EN) { showLicenses = false }
}

private val File.depth: Int get() = absolutePath.count { it == '/' || it == '\\' }

@Composable
private fun SchemeDot(color: Int, selected: Boolean, onClick: () -> Unit) {
  Box(
    Modifier
      .size(34.dp)
      .background(androidx.compose.ui.graphics.Color(color), CircleShape)
      .clip(CircleShape)
      .clickable(onClick = onClick)
      .then(if (selected) Modifier.padding(0.dp) else Modifier),
  ) {
    if (selected) androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.align(Alignment.Center).size(18.dp))
  }
}
