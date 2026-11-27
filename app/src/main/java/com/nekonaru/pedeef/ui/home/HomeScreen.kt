package com.nekonaru.pedeef.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
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
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.nekonaru.pedeef.data.Pins
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.SECTIONS
import com.nekonaru.pedeef.tools.TOOLS
import com.nekonaru.pedeef.tools.Tool
import com.nekonaru.pedeef.ui.theme.CaveatBrand
import com.nekonaru.pedeef.ui.theme.Dimens
import com.nekonaru.pedeef.ui.theme.toolIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(onOpenTool: (String) -> Unit, onScan: () -> Unit, onBatch: () -> Unit, onOpenViewer: (ByteArray, String) -> Unit) {
  val s = LocalPedeefStrings.current
  val indonesian = s === Id
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val pinnedIds by Pins.ids.collectAsState()
  var query by remember { mutableStateOf("") }
  var section by remember { mutableStateOf(-1) }
  var history by remember { mutableStateOf(com.nekonaru.pedeef.data.SearchHistory.load(context)) }
  var recentLoading by remember { mutableStateOf(true) }
  val recent by androidx.compose.runtime.produceState<List<com.nekonaru.pedeef.data.FileEntry>>(initialValue = emptyList()) {
    value = withContext(Dispatchers.IO) { com.nekonaru.pedeef.data.RecentFiles.list(context) }
    recentLoading = false
  }

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

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    // Header: app identity + search
    item(span = { GridItemSpan(maxLineSpan) }) {
      Column(Modifier.padding(bottom = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(com.nekonaru.pedeef.R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)),
          )
          Column(Modifier.padding(start = 12.dp)) {
            Text("Pedeef", style = CaveatBrand, color = MaterialTheme.colorScheme.primary)
            Text(s.homeTagline, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        Box(Modifier.height(14.dp))
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(s.searchHint) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClick = { query = "" }, modifier = Modifier.size(Dimens.TouchMin)) {
              Icon(Icons.Default.Close, contentDescription = s.clearSearch)
            }
          },
          keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { history = com.nekonaru.pedeef.data.SearchHistory.add(context, query) }),
          singleLine = true,
          shape = RoundedCornerShape(28.dp),
          modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.height(12.dp))
        // Scanner hero card
        Surface(
          onClick = onScan,
          shape = RoundedCornerShape(Dimens.CardRadiusLg),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(red = MaterialTheme.colorScheme.primary.red * 0.55f, green = MaterialTheme.colorScheme.primary.green * 0.55f, blue = MaterialTheme.colorScheme.primary.blue * 0.55f))))
              .padding(16.dp),
          ) {
            Box(
              Modifier.size(54.dp).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f), CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
              Text(s.scan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
              Text(
                if (indonesian) "Pindai, rapikan, dan dapat dicari otomatis." else "Scan, clean up and make searchable.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
              )
              Text(
                s.offlineBadge,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 2.dp),
              )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
          }
        }
        Box(Modifier.height(12.dp))
        // Quick actions: batch runner and the four most used pinned tools.
        Text(s.quickActions.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = Dimens.Sm, bottom = Dimens.Xs))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Dimens.Sm)) {
          Button(onClick = onBatch, shape = RoundedCornerShape(Dimens.ControlRadius)) {
            Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(Dimens.IconSm))
            Text(s.batchRun, modifier = Modifier.padding(start = Dimens.Xs))
          }
          TOOLS.filter { it.id in pinnedIds }.take(4).forEach { tool ->
            Button(onClick = { onOpenTool(tool.id) }, shape = RoundedCornerShape(Dimens.ControlRadius)) {
              Icon(toolIcon(tool.id), contentDescription = null, modifier = Modifier.size(Dimens.IconSm))
              Text(tool.title(indonesian), modifier = Modifier.padding(start = Dimens.Xs))
            }
          }
        }
        if (query.isBlank() && history.isNotEmpty()) {
          Text(s.searchHistory.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
          Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            history.forEach { h ->
              androidx.compose.material3.AssistChip(onClick = { query = h }, label = { Text(h) })
            }
            androidx.compose.material3.AssistChip(onClick = { com.nekonaru.pedeef.data.SearchHistory.clear(context); history = emptyList() }, label = { Text(s.clearHistory) })
          }
        }
        Box(Modifier.height(12.dp))
        // Recent files strip
        if (recentLoading && recent.isEmpty()) {
          Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Md), modifier = Modifier.padding(bottom = Dimens.Md)) {
            repeat(2) { RecentSkeleton() }
          }
        }
        if (recent.isNotEmpty()) {
          Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(s.recentFiles.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(recent) { entry ->
                RecentCard(entry) {
                  scope.launch {
                    val bytes = withContext(Dispatchers.IO) { runCatching { entry.file.readBytes() }.getOrNull() }
                    if (bytes != null) onOpenViewer(bytes, entry.name)
                  }
                }
              }
            }
          }
        }
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
        Text(s.pinned.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
      }
      items(pinned, key = { "pin-${it.id}" }) { tool ->
        ToolCard(tool, indonesian, pinnedIds, onOpenTool, modifier = Modifier, onTogglePin = { scope.launch { Pins.toggle(context, tool.id) } })
      }
      item(span = { GridItemSpan(maxLineSpan) }) {
        Text(s.allTools.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
      }
    }

    if (rest.isEmpty() && pinned.isEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Text(s.noMatches, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
      }
    } else {
      items(rest, key = { it.id }) { tool ->
        ToolCard(tool, indonesian, pinnedIds, onOpenTool, modifier = Modifier, onTogglePin = { scope.launch { Pins.toggle(context, tool.id) } })
      }
    }
  }
}

@Composable
private fun ToolCard(tool: Tool, indonesian: Boolean, pinnedIds: List<String>, onOpen: (String) -> Unit, modifier: Modifier = Modifier, onTogglePin: () -> Unit) {
  val pinned = tool.id in pinnedIds
  val icon = toolIcon(tool.id)
  val pinTint by animateColorAsState(
    if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    label = "pinTint",
  )
  Surface(
    onClick = { onOpen(tool.id) },
    shape = RoundedCornerShape(Dimens.CardRadius),
    tonalElevation = 2.dp,
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
          Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
          }
        }
        IconButton(onClick = onTogglePin, modifier = Modifier.size(Dimens.TouchMin)) {
          Icon(
            if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
            contentDescription = if (pinned) (if (indonesian) "Lepas sematan" else "Unpin") else (if (indonesian) "Sematkan" else "Pin"),
            tint = pinTint,
          )
        }
      }
      Text(tool.title(indonesian), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 10.dp))
      Text(tool.desc(indonesian), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
    }
  }
}

@Composable
private fun RecentCard(entry: com.nekonaru.pedeef.data.FileEntry, onClick: () -> Unit) {
  val date = remember { java.text.SimpleDateFormat("d MMM", java.util.Locale.getDefault()).format(java.util.Date(entry.modified)) }
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(Dimens.CardRadiusSm),
    tonalElevation = 2.dp,
    modifier = Modifier.width(150.dp),
  ) {
    Column(Modifier.padding(12.dp)) {
      Box(
        Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(Icons.Default.Article, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
      }
      Text(
        entry.name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 8.dp),
      )
      Text(
        "${date}  -  ${com.nekonaru.pedeef.ui.kit.fmtSize(entry.size)}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp),
      )
    }
  }
}

/** Placeholder shown while the recent files scan is still running. */
@Composable
private fun RecentSkeleton() {
  Surface(
    shape = RoundedCornerShape(Dimens.CardRadiusSm),
    tonalElevation = 2.dp,
    modifier = Modifier.width(150.dp),
  ) {
    Column(Modifier.padding(Dimens.Md)) {
      Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
      Box(Modifier.fillMaxWidth().padding(top = Dimens.Md).height(12.dp).clip(RoundedCornerShape(Dimens.Xs)).background(MaterialTheme.colorScheme.surfaceVariant))
      Box(Modifier.fillMaxWidth(0.6f).padding(top = Dimens.Sm).height(10.dp).clip(RoundedCornerShape(Dimens.Xs)).background(MaterialTheme.colorScheme.surfaceVariant))
    }
  }
}
