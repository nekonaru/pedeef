package com.nekonaru.pedeef.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.data.Pins
import com.nekonaru.pedeef.i18n.En
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.SECTIONS
import com.nekonaru.pedeef.tools.TOOLS
import com.nekonaru.pedeef.tools.Tool
import com.nekonaru.pedeef.tools.SECTION_AUTOMATE
import kotlinx.coroutines.launch

internal val TOOL_ICONS: Map<String, androidx.compose.ui.graphics.vector.ImageVector> = mapOf(
  "workflow" to Icons.Default.AccountTree,
  "merge" to Icons.Default.MergeType,
  "alternate" to Icons.Default.Shuffle,
  "compress" to Icons.Default.Compress,
  "split" to Icons.Default.CallSplit,
  "splitText" to Icons.Default.Subject,
  "splitBookmarks" to Icons.Default.Bookmarks,
  "splitHalf" to Icons.Default.VerticalSplit,
  "splitSize" to Icons.Default.Storage,
  "zip" to Icons.Default.FolderZip,
  "organize" to Icons.Default.Reorder,
  "rotate" to Icons.Default.Rotate90DegreesCw,
  "flip" to Icons.Default.FlipToBack,
  "nup" to Icons.Default.GridView,
  "crop" to Icons.Default.Crop,
  "fillSign" to Icons.Default.Draw,
  "watermark" to Icons.Default.FormatColorText,
  "pageNumbers" to Icons.Default.FormatListNumbered,
  "redact" to Icons.Default.FormatColorFill,
  "metadata" to Icons.Default.Description,
  "ocr" to Icons.Default.TextFields,
  "unlock" to Icons.Default.LockOpen,
  "password" to Icons.Default.Lock,
  "imagesToPdf" to Icons.Default.Image,
  "pdfToImages" to Icons.Default.PhotoLibrary,
  "extractText" to Icons.Default.ContentCopy,
)

private fun sectionName(index: Int, indonesian: Boolean): String =
  SECTIONS.getOrNull(index)?.let { if (indonesian) it.second.second else it.second.first } ?: ""

@Composable
fun HomeScreen(onOpenTool: (String) -> Unit, onScan: () -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === Id
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val pinnedIds by Pins.ids.collectAsState()
  var query by remember { mutableStateOf("") }
  var section by remember { mutableStateOf(-1) } // -1 = all

  val q = query.trim().lowercase()
  fun matches(t: Tool): Boolean {
    if (section != -1 && t.section != section) return false
    if (q.isEmpty()) return true
    return "${t.titleEn} ${t.titleId} ${t.descEn} ${t.descId}".lowercase().contains(q)
  }
  val filtered = TOOLS.filter(::matches)
  val pinned = filtered.filter { it.id in pinnedIds }.sortedBy { pinnedIds.indexOf(it.id) }
  val pinnedSet = pinned.map { it.id }.toSet()
  val rest = filtered.filterNot { it.id in pinnedSet }
  // Also show unpinned tools from other sections when pinned section filter is active
  val visible = if (section == -1 && q.isEmpty()) rest else rest

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
  ) {
    item(span = { GridItemSpan(maxLineSpan) }) {
      Column(Modifier.padding(bottom = 4.dp)) {
        Text("Pedeef", style = MaterialTheme.typography.displayMedium)
        Text(s.homeTagline, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.height(8.dp))
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(s.searchHint) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          shape = RoundedCornerShape(28.dp),
          modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.height(12.dp))
        // Scanner hero
        Surface(
          onClick = onScan,
          color = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          shape = RoundedCornerShape(22.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Box(
              Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
              contentAlignment = Alignment.Center,
            ) { Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp)) }
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
              Text(s.scan, style = MaterialTheme.typography.titleMedium)
              Text(s.onboardingBody.take(80) + "…", style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
          }
        }
        Box(Modifier.height(12.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
          FilterChip(selected = section == -1, onClick = { section = -1 }, label = { Text(s.allTools) })
          SECTIONS.forEachIndexed { i, sec ->
            FilterChip(selected = section == i, onClick = { section = i }, label = { Text(if (indonesian) sec.second.second else sec.second.first) })
          }
        }
      }
    }

    if (pinned.isNotEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Text(s.pinned.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
      }
      items(pinned, key = { "pin-${it.id}" }) { tool -> ToolCard(tool, indonesian, pinnedIds, onOpenTool, onTogglePin = { scope.launch { Pins.toggle(context, tool.id) } }) }
      item(span = { GridItemSpan(maxLineSpan) }) {
        Text(s.allTools.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
      }
    }

    if (visible.isEmpty() && pinned.isEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Text(s.noMatches, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
      }
    } else {
      items(visible, key = { it.id }) { tool -> ToolCard(tool, indonesian, pinnedIds, onOpenTool, onTogglePin = { scope.launch { Pins.toggle(context, tool.id) } }) }
    }
  }
}

@Composable
private fun ToolCard(tool: Tool, indonesian: Boolean, pinnedIds: List<String>, onOpen: (String) -> Unit, onTogglePin: () -> Unit) {
  val pinned = tool.id in pinnedIds
  val icon = TOOL_ICONS[tool.id] ?: Icons.Default.Description
  Surface(
    onClick = { onOpen(tool.id) },
    shape = RoundedCornerShape(18.dp),
    tonalElevation = 2.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
          Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
          }
        }
        IconButton(onClick = onTogglePin) {
          Icon(
            if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
            contentDescription = if (pinned) "Unpin" else "Pin",
            tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Text(tool.title(indonesian), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 10.dp))
      Text(tool.desc(indonesian), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
    }
  }
}
