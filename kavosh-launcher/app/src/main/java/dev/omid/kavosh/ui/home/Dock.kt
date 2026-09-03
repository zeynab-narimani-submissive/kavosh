package dev.omid.kavosh.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.omid.kavosh.data.AppInfo
import dev.omid.kavosh.ui.drawer.AppIcon

/** Tag used to mark an app as pinned to the dock; toggled from the drawer's context menu. */
const val PINNED_TAG = "پین شده"

@Composable
fun Dock(
    pinnedApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (pinnedApps.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        pinnedApps.take(6).forEach { app ->
            AppIcon(
                app = app,
                showLabel = false,
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) },
            )
        }
    }
}
