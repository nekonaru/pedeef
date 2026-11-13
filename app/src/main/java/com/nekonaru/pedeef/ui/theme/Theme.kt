package com.nekonaru.pedeef.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nekonaru.pedeef.R
import com.nekonaru.pedeef.data.SettingsState

val Poppins = FontFamily(
  Font(R.font.poppins_regular, FontWeight.Normal),
  Font(R.font.poppins_medium, FontWeight.Medium),
  Font(R.font.poppins_semibold, FontWeight.SemiBold),
)
val Caveat = FontFamily(Font(R.font.caveat, FontWeight.Medium))

private fun typography() = Typography(
  displayLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, letterSpacing = (-0.5).sp),
  displayMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, letterSpacing = (-0.3).sp),
  headlineLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.3).sp),
  headlineMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
  titleLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
  titleMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 16.sp),
  bodyLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 16.sp),
  bodyMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 14.sp),
  labelLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp),
  labelMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.4.sp),
)

// Brand schemes: Ember (warm orange), Cherry (deep red), Arctic (cool blue).

private val emberLight = lightColorScheme(
  primary = androidx.compose.ui.graphics.Color(0xFFB8471F),
  onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  primaryContainer = androidx.compose.ui.graphics.Color(0xFFFFE0D1),
  onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF5A1E04),
  secondary = androidx.compose.ui.graphics.Color(0xFF75584A),
  onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  secondaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDBC9),
  onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF2E1508),
  tertiary = androidx.compose.ui.graphics.Color(0xFF5C5E2A),
  onTertiary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFE4E6AA),
  onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF1D1E00),
  background = androidx.compose.ui.graphics.Color(0xFFFAF6F3),
  surface = androidx.compose.ui.graphics.Color(0xFFFFFBF8),
  surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF2ECE8),
  outline = androidx.compose.ui.graphics.Color(0xFF82736C),
)

private val emberDark = darkColorScheme(
  primary = androidx.compose.ui.graphics.Color(0xFFFFB59D),
  onPrimary = androidx.compose.ui.graphics.Color(0xFF5A1E04),
  primaryContainer = androidx.compose.ui.graphics.Color(0xFF7B3212),
  onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDBC9),
  secondary = androidx.compose.ui.graphics.Color(0xFFE2BFAD),
  onSecondary = androidx.compose.ui.graphics.Color(0xFF43291D),
  secondaryContainer = androidx.compose.ui.graphics.Color(0xFF5B3F33),
  onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDBC9),
  tertiary = androidx.compose.ui.graphics.Color(0xFFC8CA8F),
  onTertiary = androidx.compose.ui.graphics.Color(0xFF33350A),
  tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF474916),
  onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFE4E6AA),
  background = androidx.compose.ui.graphics.Color(0xFF14100D),
  surface = androidx.compose.ui.graphics.Color(0xFF1D1815),
  surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2B2522),
  outline = androidx.compose.ui.graphics.Color(0xFFA1938B),
)

private val cherryLight = lightColorScheme(
  primary = androidx.compose.ui.graphics.Color(0xFF8E2A3E),
  onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  primaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDDE3),
  onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF3D061A),
  secondary = androidx.compose.ui.graphics.Color(0xFF735A5F),
  onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  secondaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDCE3),
  onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF2C151B),
  tertiary = androidx.compose.ui.graphics.Color(0xFF5C5833),
  onTertiary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFF1EDB0),
  onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF1F1C01),
  background = androidx.compose.ui.graphics.Color(0xFFFBF5F6),
  surface = androidx.compose.ui.graphics.Color(0xFFFFFAFA),
  surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF1E9EA),
  outline = androidx.compose.ui.graphics.Color(0xFF837477),
)

private val cherryDark = darkColorScheme(
  primary = androidx.compose.ui.graphics.Color(0xFFFFB3C1),
  onPrimary = androidx.compose.ui.graphics.Color(0xFF55192E),
  primaryContainer = androidx.compose.ui.graphics.Color(0xFF6F2B3F),
  onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDDE3),
  secondary = androidx.compose.ui.graphics.Color(0xFFE2BFC7),
  onSecondary = androidx.compose.ui.graphics.Color(0xFF432A31),
  secondaryContainer = androidx.compose.ui.graphics.Color(0xFF5A3F46),
  onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDCE3),
  tertiary = androidx.compose.ui.graphics.Color(0xFFC5C17F),
  onTertiary = androidx.compose.ui.graphics.Color(0xFF30300F),
  tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF454520),
  onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFF1EDB0),
  background = androidx.compose.ui.graphics.Color(0xFF120D0F),
  surface = androidx.compose.ui.graphics.Color(0xFF1B1518),
  surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2A2226),
  outline = androidx.compose.ui.graphics.Color(0xFFA49194),
)

private val arcticLight = lightColorScheme(
  primary = androidx.compose.ui.graphics.Color(0xFF17567E),
  onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  primaryContainer = androidx.compose.ui.graphics.Color(0xFFCEE5FF),
  onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF001E31),
  secondary = androidx.compose.ui.graphics.Color(0xFF51606F),
  onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  secondaryContainer = androidx.compose.ui.graphics.Color(0xFFD4E4F5),
  onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF141E29),
  tertiary = androidx.compose.ui.graphics.Color(0xFF2F5548),
  onTertiary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
  tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFB5EFDB),
  onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF002019),
  background = androidx.compose.ui.graphics.Color(0xFFF6F9FB),
  surface = androidx.compose.ui.graphics.Color(0xFFFCFDFF),
  surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE9EEF3),
  outline = androidx.compose.ui.graphics.Color(0xFF717D87),
)

private val arcticDark = darkColorScheme(
  primary = androidx.compose.ui.graphics.Color(0xFF8FCDF9),
  onPrimary = androidx.compose.ui.graphics.Color(0xFF00344C),
  primaryContainer = androidx.compose.ui.graphics.Color(0xFF004A69),
  onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFCEE5FF),
  secondary = androidx.compose.ui.graphics.Color(0xFFB8C9D9),
  onSecondary = androidx.compose.ui.graphics.Color(0xFF23303F),
  secondaryContainer = androidx.compose.ui.graphics.Color(0xFF394857),
  onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFD4E4F5),
  tertiary = androidx.compose.ui.graphics.Color(0xFF99D4BE),
  onTertiary = androidx.compose.ui.graphics.Color(0xFF00382C),
  tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF135143),
  onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFB5EFDB),
  background = androidx.compose.ui.graphics.Color(0xFF0F1418),
  surface = androidx.compose.ui.graphics.Color(0xFF171C21),
  surfaceVariant = androidx.compose.ui.graphics.Color(0xFF232A30),
  outline = androidx.compose.ui.graphics.Color(0xFF8C98A1),
)

private fun schemeFor(name: String, dark: Boolean) = when (name) {
  "cherry" -> if (dark) cherryDark else cherryLight
  "arctic" -> if (dark) arcticDark else arcticLight
  else -> if (dark) emberDark else emberLight
}

@Composable
fun PedeefTheme(settings: SettingsState, content: @Composable () -> Unit) {
  val context = LocalContext.current
  val dark = when (settings.themeMode) {
    "dark" -> true
    "light" -> false
    else -> isSystemInDarkTheme()
  }
  val dynamic = settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val colors = when {
    dynamic -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    else -> schemeFor(settings.scheme, dark)
  }
  MaterialTheme(colorScheme = colors, typography = typography(), content = content)
}
