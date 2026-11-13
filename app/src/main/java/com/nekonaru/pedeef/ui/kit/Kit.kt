package com.nekonaru.pedeef.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.OptDef
import com.nekonaru.pedeef.tools.Opts
import com.nekonaru.pedeef.tools.with
import com.nekonaru.pedeef.tools.str
import com.nekonaru.pedeef.tools.bool

fun fmtSize(bytes: Long): String = when {
  bytes < 1024 -> "$bytes B"
  bytes < 1024 * 1024 -> "%.0f KB".format(bytes / 1024.0)
  bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
  else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
}

@Composable
fun GroupLabel(text: String) {
  Text(
    text.uppercase(),
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(start = 4.dp, top = 18.dp, bottom = 8.dp),
  )
}

@Composable
fun Group(content: @Composable () -> Unit) {
  Surface(tonalElevation = 1.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) {
    Column(Modifier.padding(4.dp)) { content() }
  }
}

@Composable
fun ListRow(icon: ImageVector?, title: String, subtitle: String? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
      .padding(horizontal = 12.dp, vertical = 12.dp),
  ) {
    if (icon != null) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
      Box(Modifier.width(12.dp))
    }
    Column(Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
      if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
    if (trailing != null) {
      Box(Modifier.width(8.dp))
      trailing()
    }
  }
}

@Composable
fun Btn(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, busy: Boolean = false, danger: Boolean = false) {
  val container = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  val on = if (danger) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
  Surface(
    onClick = { if (enabled && !busy) onClick() },
    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    color = container.copy(alpha = if (enabled) 1f else 0.5f),
    modifier = modifier,
  ) {
    Box(Modifier.padding(horizontal = 20.dp, vertical = 13.dp), contentAlignment = Alignment.Center) {
      if (busy) {
        CircularProgressIndicator(color = on, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
      } else {
        Text(text, style = MaterialTheme.typography.labelLarge, color = on)
      }
    }
  }
}

@Composable
fun Spinner() { CircularProgressIndicator(strokeWidth = 2.dp) }

/** The generic form renderer for a tool's options. */
@Composable
fun Options(defs: List<OptDef>, value: Opts, indonesian: Boolean, onChange: (Opts) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    for (def in defs) {
      val visible = def.visible?.invoke(value) ?: true
      if (!visible) continue
      when (def) {
        is OptDef.Seg -> {
          Text(if (indonesian) def.id else def.en, style = MaterialTheme.typography.labelMedium)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            for ((v, en, id) in def.choices) {
              val selected = value.str(def.key) == v
              AssistChip(
                onClick = { onChange(value.with(def.key, v)) },
                label = { Text(if (indonesian) id else en) },
                leadingIcon = if (selected) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
              )
            }
          }
        }
        is OptDef.Text -> {
          Text(if (indonesian) def.id else def.en, style = MaterialTheme.typography.labelMedium)
          OutlinedTextField(
            value = value.str(def.key),
            onValueChange = { raw ->
              val filtered = def.strip?.let { r -> raw.filterNot { r.matches(it.toString()) } } ?: raw
              onChange(value.with(def.key, filtered))
            },
            placeholder = { Text(def.placeholder) },
            singleLine = true,
            visualTransformation = if (def.isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          )
        }
        is OptDef.Toggle -> {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(if (indonesian) def.id else def.en, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = value.bool(def.key), onCheckedChange = { onChange(value.with(def.key, it)) })
          }
        }
      }
    }
  }
}

@Composable
fun ConfirmDialog(title: String, body: String, confirm: String, dismiss: String, danger: Boolean = false, onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = { Text(body) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(confirm, color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(dismiss) } },
  )
}

@Composable
fun PromptDialog(title: String, initial: String, placeholder: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit, confirm: String? = null) {
  val s = LocalPedeefStrings.current
  var text by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(initial) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text(placeholder) }, singleLine = true, modifier = Modifier.fillMaxWidth())
    },
    confirmButton = { TextButton(onClick = { when { text.isNotBlank() -> onConfirm(text.trim()) }; Unit }) { Text(confirm ?: s.ok) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
  )
}

@Composable
fun PasswordDialog(title: String, error: String?, onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
  val s = LocalPedeefStrings.current
  var text by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column {
        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          singleLine = true,
          visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
      }
    },
    confirmButton = { TextButton(onClick = { if (text.isNotBlank()) onSubmit(text) }) { Text(s.unlock) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
  )
}

@Composable
fun InfoDialog(title: String, body: String, onDismiss: () -> Unit) {
  val s = LocalPedeefStrings.current
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = { Text(body, modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) },
    confirmButton = { TextButton(onClick = onDismiss) { Text(s.ok) } },
  )
}
