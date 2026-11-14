package com.nekonaru.pedeef.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.data.FilesStore
import com.nekonaru.pedeef.data.PublicFiles
import com.nekonaru.pedeef.data.SettingsState
import com.nekonaru.pedeef.data.UpdateCheck
import com.nekonaru.pedeef.data.UpdateResult
import com.nekonaru.pedeef.i18n.En
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.ui.kit.ConfirmDialog
import com.nekonaru.pedeef.ui.kit.Group
import com.nekonaru.pedeef.ui.kit.GroupLabel
import com.nekonaru.pedeef.ui.kit.InfoDialog
import com.nekonaru.pedeef.ui.kit.ListRow
import com.nekonaru.pedeef.ui.kit.fmtSize
import kotlinx.coroutines.launch
import java.io.File

const val PEDEEF_VERSION = "1.1.0"

private const val PRIVACY_EN = """Pedeef is built to be private:

1. Every tool, the scanner and OCR run on your phone. No file ever leaves the device unless you share it yourself or save a copy through the system file picker.
2. The only network use is the "Check for updates" request, which fetches the latest release tag from GitHub when you tap it. No personal data is sent, nothing is tracked.
3. Files you create are stored in Download/Pedeef (or inside the app if you deny storage access). You can delete them anytime from Settings or with any file manager.
4. The optional document scanner uses the Google Play services document scanner. Images it captures stay on your phone and are processed into PDFs by Pedeef.
5. You can ask for your data deletion by deleting the app and its files. Pedeef stores nothing anywhere else.

Last updated: November 2026. Contact: nekonaru on GitHub."""

private const val PRIVACY_ID = """Pedeef dibuat untuk menjaga privasi:

1. Semua alat, pemindai, dan OCR berjalan di ponselmu. File tidak pernah keluar dari perangkat kecuali kamu sendiri yang membagikannya atau menyalinnya lewat file picker sistem.
2. Satu-satunya penggunaan jaringan adalah "Periksa pembaruan", yang mengambil tag rilis terbaru dari GitHub saat kamu mengetuknya. Tidak ada data pribadi yang dikirim, tidak ada pelacakan.
3. File yang kamu buat disimpan di Download/Pedeef (atau di dalam aplikasi jika akses penyimpanan ditolak). Kamu bisa menghapusnya kapan pun dari Pengaturan atau file manager apa pun.
4. Pemindai dokumen opsional memakai pemindai dokumen Google Play services. Gambar yang diambil tetap di ponselmu dan diproses menjadi PDF oleh Pedeef.
5. Kamu bisa meminta penghapusan data dengan menghapus aplikasi dan file-nya. Pedeef tidak menyimpan apa pun di tempat lain.

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

private val SPONSORS = listOf(
  Triple("GitHub Sponsors", "github.com/sponsors/nekonaru", "https://github.com/sponsors/nekonaru"),
  Triple("Saweria", "saweria.co/nekonaruu", "https://saweria.co/nekonaruu"),
  Triple("SociaBuzz", "sociabuzz.com/nekonaru/donate", "https://sociabuzz.com/nekonaru/donate"),
  Triple("BagiBagi", "bagibagi.co/Nekonaru", "https://bagibagi.co/Nekonaru"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settings: SettingsState, update: ((SettingsState) -> SettingsState) -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === Id
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val storageRoot = remember { PublicFiles.root(context) }
  val privateFallback = remember { FilesStore.root(context) }
  val infoRoot = storageRoot ?: privateFallback
  var refresh by remember { mutableStateOf(0) }
  var showClear by remember { mutableStateOf(false) }
  var showPrivacy by remember { mutableStateOf(false) }
  var showLicenses by remember { mutableStateOf(false) }
  var showSponsor by remember { mutableStateOf(false) }
  var updateStatus by remember { mutableStateOf<UpdateResult?>(null) }
  var checking by remember { mutableStateOf(false) }

  val totalSize = remember(refresh) { FilesStore.totalSize(infoRoot) }
  val fileCount = remember(refresh) { FilesStore.countIn(infoRoot) }
  val granted = storageRoot != null

  fun checkUpdate() {
    scope.launch {
      checking = true
      updateStatus = null
      updateStatus = UpdateCheck.check(PEDEEF_VERSION)
      checking = false
    }
  }

  fun openUrl(url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
  }

  Scaffold(topBar = { TopAppBar(title = { Text(s.settingsTitle) }) }) { padding ->
    Column(
      Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      // Header card
      ElevatedCard(shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
          Image(
            painter = androidx.compose.ui.res.painterResource(com.nekonaru.pedeef.R.drawable.ic_launcher_foreground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(64.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp)),
          )
          Column(Modifier.padding(start = 14.dp)) {
            Text("Pedeef", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(s.homeTagline, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("v$PEDEEF_VERSION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
          }
        }
      }

      GroupLabel(s.appearance)
      Group {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(if (indonesian) "Mode" else "Mode", modifier = Modifier.weight(1f))
          SingleChoiceSegmentedButtonRow {
            listOf("system" to s.themeSystem, "light" to s.themeLight, "dark" to s.themeDark).forEachIndexed { i, (modeKey, label) ->
              SegmentedButton(
                selected = settings.themeMode == modeKey,
                onClick = { update { it.copy(themeMode = modeKey) } },
                shape = SegmentedButtonDefaults.itemShape(i, 3),
              ) { Text(label) }
            }
          }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(s.scheme, modifier = Modifier.weight(1f))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SchemeDot(0xFFB8471F.toInt(), settings.scheme == "ember") { update { it.copy(scheme = "ember") } }
            SchemeDot(0xFF8E2A3E.toInt(), settings.scheme == "cherry") { update { it.copy(scheme = "cherry") } }
            SchemeDot(0xFF17567E.toInt(), settings.scheme == "arctic") { update { it.copy(scheme = "arctic") } }
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
        ListRow(
          Icons.Default.FolderOpen,
          if (granted) "Download/Pedeef" else s.savedToApp,
          "$fileCount " + (if (indonesian) "file" else "files") + "  •  " + fmtSize(totalSize),
        )
        if (!granted) {
          ListRow(Icons.Default.FolderOpen, s.storageAccessButton, s.storageAccessBody, onClick = { PublicFiles.requestManageAllFilesAccess(context) })
        }
        ListRow(Icons.Default.Delete, s.clearAppData, null, onClick = { showClear = true })
      }

      GroupLabel(s.about)
      Group {
        ListRow(
          Icons.Default.Info,
          s.versionRow,
          if (checking) s.checkingUpdates else when (val r = updateStatus) {
            null -> "v$PEDEEF_VERSION"
            is UpdateResult.UpToDate -> s.upToDate
            is UpdateResult.UpdateAvailable -> s.updateAvailable.format(r.tag)
            is UpdateResult.Failed -> s.checkFailed
          },
          onClick = { if (!checking) checkUpdate() },
          trailing = { if (checking) androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) },
        )
        ListRow(
          Icons.Default.AccountCircle,
          "Nicolas Dwi Dharma",
          "github.com/nekonaru",
          onClick = { openUrl("https://github.com/nekonaru") },
          trailing = { Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp)) },
        )
        ListRow(
          Icons.Default.Favorite,
          s.sponsor,
          if (indonesian) "Dukung pengembang Pedeef" else "Support the developer",
          onClick = { showSponsor = true },
        )
      }

      GroupLabel(if (indonesian) "Lainnya" else "More")
      Group {
        ListRow(Icons.Default.PrivacyTip, s.privacyPolicy, null, onClick = { showPrivacy = true })
        ListRow(Icons.Default.Description, s.openSource, null, onClick = { showLicenses = true })
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
        infoRoot.walkTopDown().filter { it.isFile }.forEach { runCatching { it.delete() } }
        infoRoot.walkTopDown().filter { it.isDirectory && it != infoRoot }.sortedByDescending { it.depth }.forEach { runCatching { it.delete() } }
        refresh++
        showClear = false
      },
      onDismiss = { showClear = false },
    )
  }
  if (showPrivacy) InfoDialog(s.privacyPolicy, if (indonesian) PRIVACY_ID else PRIVACY_EN) { showPrivacy = false }
  if (showLicenses) InfoDialog(s.openSource, if (indonesian) LICENSES_ID else LICENSES_EN) { showLicenses = false }

  if (showSponsor) {
    AlertDialog(
      onDismissRequest = { showSponsor = false },
      title = { Text(s.sponsor) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(s.sponsorBody)
          Box(Modifier.padding(top = 8.dp))
          for ((name, short, url) in SPONSORS) {
            ListRow(
              Icons.Default.Favorite,
              name,
              short,
              onClick = { openUrl(url) },
              trailing = { Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp)) },
            )
          }
        }
      },
      confirmButton = { TextButton(onClick = { showSponsor = false }) { Text(s.ok) } },
    )
  }
}

private val File.depth: Int get() = absolutePath.count { it == '/' || it == '\\' }

@Composable
private fun SchemeDot(color: Int, selected: Boolean, onClick: () -> Unit) {
  Box(
    Modifier
      .size(34.dp)
      .background(androidx.compose.ui.graphics.Color(color), CircleShape)
      .clip(CircleShape)
      .clickable(onClick = onClick),
  ) {
    if (selected) Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(18.dp).align(Alignment.Center))
  }
}
