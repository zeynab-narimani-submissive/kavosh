package dev.omid.kavosh.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.omid.kavosh.R
import dev.omid.kavosh.data.AppInfo
import dev.omid.kavosh.widget.KavoshAppWidgetHost
import dev.omid.kavosh.widget.WidgetHostView

/**
 * The idle home screen: clock, any widgets the user placed, a search pill, and the pinned-app
 * dock. A vertical swipe-up anywhere on the screen opens the app drawer, mirroring the gesture
 * every stock launcher (and Kvaesitso) uses.
 */
@Composable
fun HomeScreen(
    pinnedApps: List<AppInfo>,
    placedWidgetIds: List<Int>,
    widgetHost: KavoshAppWidgetHost,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onAddWidget: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragAccumulator = 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        dragAccumulator += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        if (dragAccumulator < -80f) onOpenDrawer()
                    },
                )
            },
    ) {
        IconButton(
            onClick = onAddWidget,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 12.dp),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_widget),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ClockFace(modifier = Modifier.fillMaxWidth())

            SearchPill(onClick = onOpenSearch, modifier = Modifier.padding(horizontal = 32.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(placedWidgetIds, key = { it }) { widgetId ->
                    WidgetHostView(host = widgetHost, appWidgetId = widgetId, height = 180.dp)
                }
            }

            Dock(
                pinnedApps = pinnedApps,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun SearchPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(28.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.search_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
