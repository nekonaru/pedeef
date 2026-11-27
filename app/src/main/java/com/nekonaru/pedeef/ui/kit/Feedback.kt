package com.nekonaru.pedeef.ui.kit

import androidx.compose.runtime.staticCompositionLocalOf

/** Optional global message sink. Screens post short confirmations here instead of rendering inline text. */
val LocalSnackbar = staticCompositionLocalOf<((String) -> Unit)?> { null }
