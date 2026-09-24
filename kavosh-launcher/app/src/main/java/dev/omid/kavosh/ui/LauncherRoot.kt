package dev.omid.kavosh.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.omid.kavosh.LauncherViewModel
import dev.omid.kavosh.data.AppInfo
import dev.omid.kavosh.ui.drawer.AppContextMenu
import dev.omid.kavosh.ui.drawer.AppDrawerScreen
import dev.omid.kavosh.ui.drawer.TagEditDialog
import dev.omid.kavosh.ui.home.HomeScreen
import dev.omid.kavosh.ui.home.PINNED_TAG
import dev.omid.kavosh.ui.search.SearchOverlay
import dev.omid.kavosh.widget.KavoshAppWidgetHost

private enum class Screen { HOME, DRAWER, SEARCH }

/**
 * Top-level screen coordinator. Home, the app drawer, and search are all overlays of each
 * other rather than a back-stack of separate destinations — that's what makes swipe-up-to-open
 * / swipe-down-to-dismiss feel instant instead of like a page navigation.
 */
@Composable
fun LauncherRoot(
    viewModel: LauncherViewModel,
    widgetHost: KavoshAppWidgetHost,
    onLaunchApp: (AppInfo) -> Unit,
    onOpenAppInfo: (AppInfo) -> Unit,
    onUninstallApp: (AppInfo) -> Unit,
    onRequestAddWidget: () -> Unit,
    onWebSearch: (String) -> Unit,
) {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var contextMenuApp by remember { mutableStateOf<AppInfo?>(null) }
    var tagEditApp by remember { mutableStateOf<AppInfo?>(null) }

    val apps: List<AppInfo> by viewModel.apps.collectAsStateWithLifecycle()
    val allTags: Set<String> by viewModel.allTags.collectAsStateWithLifecycle()
    val activeTagFilter: String? by viewModel.activeTagFilter.collectAsStateWithLifecycle()
    val searchQuery: String by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val placedWidgetIds: List<Int> by viewModel.placedWidgetIds.collectAsStateWithLifecycle()

    val pinnedApps: List<AppInfo> = remember(apps) { apps.filter { app -> PINNED_TAG in app.tags } }
    val searchFocusRequester = remember { FocusRequester() }

    BackHandler(enabled = screen != Screen.HOME) {
        viewModel.clearSearch()
        screen = Screen.HOME
    }

    fun launchAndReturnHome(app: AppInfo) {
        onLaunchApp(app)
        viewModel.clearSearch()
        screen = Screen.HOME
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HomeScreen(
            pinnedApps = pinnedApps,
            placedWidgetIds = placedWidgetIds,
            widgetHost = widgetHost,
            onOpenDrawer = { screen = Screen.DRAWER },
            onOpenSearch = { screen = Screen.SEARCH },
            onAddWidget = onRequestAddWidget,
            onAppClick = ::launchAndReturnHome,
            onAppLongClick = { app -> contextMenuApp = app },
        )

        AnimatedVisibility(
            visible = screen == Screen.DRAWER,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                AppDrawerScreen(
                    apps = apps,
                    allTags = allTags,
                    activeTagFilter = activeTagFilter,
                    onTagFilterChange = viewModel::setTagFilter,
                    onAppClick = ::launchAndReturnHome,
                    onAppLongClick = { app -> contextMenuApp = app },
                )
            }
        }

        AnimatedVisibility(
            visible = screen == Screen.SEARCH,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                SearchOverlay(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    results = searchResults,
                    onAppClick = ::launchAndReturnHome,
                    onWebSearch = { q ->
                        onWebSearch(q)
                        viewModel.clearSearch()
                        screen = Screen.HOME
                    },
                    focusRequester = searchFocusRequester,
                )
            }
        }

        contextMenuApp?.let { app ->
            AppContextMenu(
                app = app,
                onDismiss = { contextMenuApp = null },
                onTogglePin = {
                    val newTags = if (PINNED_TAG in app.tags) app.tags - PINNED_TAG else app.tags + PINNED_TAG
                    viewModel.setTagsForApp(app, newTags)
                    contextMenuApp = null
                },
                onEditTags = {
                    tagEditApp = app
                    contextMenuApp = null
                },
                onAppInfo = {
                    onOpenAppInfo(app)
                    contextMenuApp = null
                },
                onUninstall = {
                    onUninstallApp(app)
                    contextMenuApp = null
                },
            )
        }

        tagEditApp?.let { app ->
            TagEditDialog(
                app = app,
                existingTags = allTags,
                onDismiss = { tagEditApp = null },
                onSave = { tags ->
                    viewModel.setTagsForApp(app, tags)
                    tagEditApp = null
                },
            )
        }
    }

    LaunchedEffect(screen) {
        if (screen == Screen.SEARCH) {
            searchFocusRequester.requestFocus()
        }
    }
}
