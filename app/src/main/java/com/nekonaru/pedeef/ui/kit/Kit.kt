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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.tools.OptDef
import com.nekonaru.pedeef.tools.Opts
import com.nekonaru.pedeef.tools.with
import com.nekonaru.pedeef.tools.str
import com.nekonaru.pedeef.tools.bool
import com.nekonaru.pedeef.ui.theme.Dimens

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
    fontWeight = FontWeight.SemiBold,
    letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp),
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(start = Dimens.Xs, top = Dimens.LabelTop, bottom = Dimens.LabelBottom),
  )
}

@Composable
fun Group(content: @Composable () -> Unit) {
  Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(Dimens.CardRadius)) {
    Column(Modifier.padding(Dimens.Xs)) { content() }
  }
}

/** Row with a tinted circular icon, title, optional subtitle and optional trailing slot. */
@Composable
fun ListRow(
  icon: ImageVector?,
  title: String,
  subtitle: String? = null,
  onClick: (() -> Unit)? = null,
  trailing: (@Composable () -> Unit)? = null,
  danger: Boolean = false,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
      .padding(horizontal = Dimens.Lg, vertical = Dimens.Md),
  ) {
    if (icon != null) {
      Box(
        Modifier.size(38.dp).background(
          if (danger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
          CircleShape,
        ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          icon,
          contentDescription = null,
          tint = if (danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(20.dp),
        )
      }
      Box(Modifier.width(Dimens.Md))
    }
    Column(Modifier.weight(1f)) {
      Text(
        title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (subtitle != null) {
        Text(
          subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
    if (trailing != null) {
      Box(Modifier.width(Dimens.Sm))
      trailing()
    } else if (onClick != null) {
      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun Btn(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, busy: Boolean = false, danger: Boolean = false) {
  val container = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  val on = if (danger) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
  Surface(
    onClick = { if (enabled && !busy) onClick() },
    shape = RoundedCornerShape(Dimens.ControlRadius),
    color = container.copy(alpha = if (enabled) 1f else 0.5f),
    modifier = modifier.height(52.dp),
  ) {
    Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
      if (busy) {
        CircularProgressIndicator(color = on, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
      } else {
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = on)
      }
    }
  }
}

@Composable
fun Spinner() { CircularProgressIndicator(strokeWidth = 2.dp) }

/** The generic form renderer for a tool's options. */
@Composable
fun Options(defs: List<OptDef>, value: Opts, indonesian: Boolean, onChange: (Opts) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimens.Md)) {
    for (def in defs) {
      val visible = def.visible?.invoke(value) ?: true
      if (!visible) continue
      when (def) {
        is OptDef.Seg -> {
          Text(
            if (indonesian) def.id else def.en,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Sm), modifier = Modifier.padding(top = Dimens.Xs)) {
            for ((v, en, id) in def.choices) {
              val selected = value.str(def.key) == v
              FilterChip(
                selected = selected,
                onClick = { onChange(value.with(def.key, v)) },
                label = { Text(if (indonesian) id else en) },
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
              )
            }
          }
        }
        is OptDef.Text -> {
          Text(
            if (indonesian) def.id else def.en,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
          )
          OutlinedTextField(
            value = value.str(def.key),
            onValueChange = { raw ->
              val filtered = def.strip?.let { r -> raw.filterNot { r.matches(it.toString()) } } ?: raw
              onChange(value.with(def.key, filtered))
            },
            placeholder = { Text(def.placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(Dimens.ControlRadius),
            visualTransformation = if (def.isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().padding(top = Dimens.Xs),
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
    title = { Text(title, fontWeight = FontWeight.SemiBold) },
    text = { Text(body) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(confirm, fontWeight = FontWeight.SemiBold, color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
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
    title = { Text(title, fontWeight = FontWeight.SemiBold) },
    text = {
      OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text(placeholder) }, singleLine = true, shape = RoundedCornerShape(Dimens.ControlRadius), modifier = Modifier.fillMaxWidth())
    },
    confirmButton = { TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) { Text(confirm ?: s.ok, fontWeight = FontWeight.SemiBold) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
  )
}

@Composable
fun PasswordDialog(title: String, error: String?, onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
  val s = LocalPedeefStrings.current
  var text by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, fontWeight = FontWeight.SemiBold) },
    text = {
      Column {
        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          singleLine = true,
          shape = RoundedCornerShape(Dimens.ControlRadius),
          visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
      }
    },
    confirmButton = { TextButton(onClick = { onSubmit(text) }) { Text(s.unlock, fontWeight = FontWeight.SemiBold) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
  )
}

@Composable
fun InfoDialog(title: String, body: String, onDismiss: () -> Unit) {
  val s = LocalPedeefStrings.current
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, fontWeight = FontWeight.SemiBold) },
    text = { Text(body, modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) },
    confirmButton = { TextButton(onClick = onDismiss) { Text(s.ok, fontWeight = FontWeight.SemiBold) } },
  )
}
