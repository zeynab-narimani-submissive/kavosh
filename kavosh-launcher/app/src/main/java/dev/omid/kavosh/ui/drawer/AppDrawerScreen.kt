package dev.omid.kavosh.ui.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.omid.kavosh.R
import dev.omid.kavosh.data.AppInfo

@Composable
fun AppDrawerScreen(
    apps: List<AppInfo>,
    allTags: Set<String>,
    activeTagFilter: String?,
    onTagFilterChange: (String?) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered = remember(apps, activeTagFilter) {
        if (activeTagFilter == null) apps else apps.filter { activeTagFilter in it.tags }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (allTags.isNotEmpty()) {
            TagFilterRow(
                tags = allTags.sorted(),
                activeTag = activeTagFilter,
                onTagSelected = onTagFilterChange,
            )
        }

        Text(
            text = stringResource(R.string.all_apps),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(filtered, key = { it.key }) { app ->
                AppIcon(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                )
            }
        }
    }
}

@Composable
private fun TagFilterRow(
    tags: List<String>,
    activeTag: String?,
    onTagSelected: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = activeTag == null,
                onClick = { onTagSelected(null) },
                label = { Text("همه") },
            )
        }
        lazyRowItems(tags) { tag ->
            FilterChip(
                selected = activeTag == tag,
                onClick = { onTagSelected(if (activeTag == tag) null else tag) },
                label = { Text(tag) },
            )
        }
    }
}

// Small shim so this file doesn't need a Context/LocalContext import just to read one string.
@Composable
private fun stringResourceCompat(id: Int): String =
    androidx.compose.ui.res.stringResource(id)
