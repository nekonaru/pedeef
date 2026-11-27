package com.nekonaru.pedeef.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.navigation.navArgument
import com.nekonaru.pedeef.data.Settings
import com.nekonaru.pedeef.data.SettingsState
import com.nekonaru.pedeef.i18n.En
import com.nekonaru.pedeef.i18n.Id
import com.nekonaru.pedeef.i18n.LocalPedeefStrings
import com.nekonaru.pedeef.i18n.Strings
import com.nekonaru.pedeef.ui.files.FilesScreen
import com.nekonaru.pedeef.ui.home.HomeScreen
import com.nekonaru.pedeef.ui.kit.LocalSnackbar
import com.nekonaru.pedeef.ui.scan.ScannerScreen
import com.nekonaru.pedeef.ui.settings.SettingsScreen
import com.nekonaru.pedeef.ui.tool.ToolScreen
import com.nekonaru.pedeef.ui.theme.LocalMotionSpec
import com.nekonaru.pedeef.ui.theme.PedeefTheme
import com.nekonaru.pedeef.ui.viewer.ViewerScreen
import kotlinx.coroutines.launch

/** Bytes handed to the viewer between navigation calls (too big for route arguments). */
object ViewerPayload {
  var bytes: ByteArray? = null
  var name: String = "document.pdf"

  /** True when the payload is already stored in the file tree (Files tab, tools); false for incoming VIEW/SEND copies. */
  var persisted: Boolean = true

  /** Bumped on every set or clear; observe with LaunchedEffect(ViewerPayload.version). */
  var version by androidx.compose.runtime.mutableIntStateOf(0)
    private set

  fun set(bytes: ByteArray?, name: String, persisted: Boolean) {
    this.bytes = bytes
    this.name = name
    this.persisted = persisted
    this.version = version + 1
  }

  fun clear() {
    bytes = null
    persisted = true
    this.version = version + 1
  }
}

/** Launcher shortcut picked by the user; consumed once by AppShell. */
object ShortcutRoute {
  var target: String? = null
    private set
  var version by androidx.compose.runtime.mutableIntStateOf(0)
    private set

  /** route like "scan" or "tool/merge". */
  fun open(route: String) {
    target = route
    version = version + 1
  }

  fun consume() {
    target = null
  }
}

private fun resolveLanguage(settings: SettingsState, systemLocales: String): Strings = when (settings.language) {
  "en" -> En
  "id" -> Id
  else -> if (systemLocales.contains("id")) Id else En
}

@Composable
fun App() {
  val context = LocalContext.current
  var settings by remember { mutableStateOf(Settings.load(context)) }
  val systemLocales = LocalConfiguration.current.locales.toLanguageTags()
  val strings = resolveLanguage(settings, systemLocales)
  fun update(transform: (SettingsState) -> SettingsState) {
    settings = transform(settings)
    Settings.save(context, settings)
  }
  PedeefTheme(settings) {
    androidx.compose.runtime.CompositionLocalProvider(LocalPedeefStrings provides strings) {
      val nav = rememberNavController()
      AppShell(nav, strings, settings, ::update)
    }
  }
}

private enum class Tab { Tools, Files, Settings }

@Composable
private fun AppShell(nav: NavHostController, strings: Strings, settings: SettingsState, update: ((SettingsState) -> SettingsState) -> Unit) {
  var tab by rememberSaveable { mutableStateOf(Tab.Tools) }
  val onTab: (Tab) -> Unit = { tab = it }
  val backStack by nav.currentBackStackEntryAsState()
  val showBar = backStack?.destination?.route == "shell"
  val motion = LocalMotionSpec.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val showMessage: (String) -> Unit = { message ->
    scope.launch { snackbarHostState.currentSnackbarData?.dismiss(); snackbarHostState.showSnackbar(message) }
  }
  fun openViewer(bytes: ByteArray, name: String, persisted: Boolean = true) {
    ViewerPayload.set(bytes, name, persisted)
    nav.navigate("viewer")
  }
  LaunchedEffect(ViewerPayload.version, backStack?.destination?.route, settings.onboardingDone) {
    // If an incoming PDF was saved by the launcher (or reopened from a tool), jump straight into the viewer.
    if (com.nekonaru.pedeef.ui.ViewerPayload.bytes != null && nav.currentDestination?.route == "shell" && settings.onboardingDone) {
      openViewer(ViewerPayload.bytes!!, ViewerPayload.name, ViewerPayload.persisted)
    }
  }
  LaunchedEffect(ShortcutRoute.version, backStack?.destination?.route, settings.onboardingDone) {
    // Launcher shortcut picked: go straight to the target screen, even if it was tapped while another
    // screen was open or while onboarding was still showing.
    val target = ShortcutRoute.target ?: return@LaunchedEffect
    if (nav.currentDestination?.route == "shell" && settings.onboardingDone) {
      ShortcutRoute.consume()
      nav.navigate(target)
    }
  }
  CompositionLocalProvider(LocalSnackbar provides showMessage) {
  androidx.compose.material3.Scaffold(
    modifier = Modifier.safeDrawingPadding(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (showBar) NavigationBar {
        NavigationBarItem(
          selected = tab == Tab.Tools,
          onClick = { onTab(Tab.Tools) },
          icon = { Icon(Icons.Default.Home, contentDescription = null) },
          label = { Text("Pedeef") },
        )
        NavigationBarItem(
          selected = tab == Tab.Files,
          onClick = { onTab(Tab.Files) },
          icon = { Icon(Icons.Default.Folder, contentDescription = null) },
          label = { Text(strings.files) },
        )
        NavigationBarItem(
          selected = tab == Tab.Settings,
          onClick = { onTab(Tab.Settings) },
          icon = { Icon(Icons.Default.Settings, contentDescription = null) },
          label = { Text(strings.settings) },
        )
      }
    },
  ) { padding ->
    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
      if (!settings.onboardingDone) {
        OnboardingScreen(
          onDone = { update { it.copy(onboardingDone = true) } },
          onLanguage = { lang -> update { it.copy(language = lang) } },
          language = if (settings.language == "id") "id" else "en",
        )
        return@Box
      }
      NavHost(navController = nav, startDestination = "shell") {
        composable("shell") {
          AnimatedContent(
            targetState = tab,
            transitionSpec = {
              val forward = targetState.ordinal >= initialState.ordinal
              (
                fadeIn(tween(motion.medium)) + slideInHorizontally(
                  initialOffsetX = { width -> if (forward) width else -width },
                  animationSpec = tween(motion.medium),
                )
                ) togetherWith (
                fadeOut(tween(motion.short)) + slideOutHorizontally(
                  targetOffsetX = { width -> if (forward) -width else width },
                  animationSpec = tween(motion.short),
                )
                )
            },
            label = "tab",
          ) { current ->
            when (current) {
              Tab.Tools -> HomeScreen(
                onOpenTool = { nav.navigate("tool/$it") },
                onScan = { nav.navigate("scan") },
              onBatch = { nav.navigate("batch") },
                onOpenViewer = { bytes, name -> openViewer(bytes, name) },
              )
              Tab.Files -> FilesScreen(onOpenViewer = { bytes, name -> openViewer(bytes, name) })
              Tab.Settings -> SettingsScreen(settings = settings, update = update)
            }
          }
        }
        composable("batch") { com.nekonaru.pedeef.ui.batch.BatchScreen(onBack = { nav.popBackStack() }) }
        composable("tool/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
          val id = entry.arguments?.getString("id").orEmpty()
          ToolScreen(
            toolId = id,
            onBack = { nav.popBackStack() },
            onOpenViewer = { bytes, name -> openViewer(bytes, name) },
          )
        }
        composable("scan") { ScannerScreen(onBack = { nav.popBackStack() }, onOpenViewer = { bytes, name -> openViewer(bytes, name) }) }
        composable("viewer") {
          ViewerScreen(
            onBack = { ViewerPayload.clear(); nav.popBackStack() },
            nightMode = settings.nightMode,
            onToggleNightMode = { update { s -> s.copy(nightMode = !s.nightMode) } },
            gesturesEnabled = settings.gestures,
          )
        }
      }
    }
  }
  }
}
