package dev.omid.kavosh.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawable.toBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.omid.kavosh.R
import dev.omid.kavosh.data.AppInfo
import dev.omid.kavosh.search.SearchResult

/**
 * The universal search screen: one text field, results ranked as apps first, then an inline
 * calculator answer if the query looks like math, then "search the web" as a guaranteed
 * fallback so a query is never a dead end.
 */
@Composable
fun SearchOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<SearchResult>,
    onAppClick: (AppInfo) -> Unit,
    onWebSearch: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(top = 48.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                val topApp = results.filterIsInstance<SearchResult.App>().firstOrNull()
                if (topApp != null) onAppClick(topApp.app) else onWebSearch(query)
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
        )

        if (results.isEmpty() && query.isNotBlank()) {
            Text(
                text = stringResource(R.string.no_results),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp),
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(results, key = { it.stableKey() }) { result ->
                when (result) {
                    is SearchResult.App -> AppResultRow(result, onClick = { onAppClick(result.app) })
                    is SearchResult.Calculation -> CalculationResultRow(result)
                    is SearchResult.WebSearch -> WebSearchResultRow(result, onClick = { onWebSearch(result.query) })
                }
            }
        }
    }
}

private fun SearchResult.stableKey(): String = when (this) {
    is SearchResult.App -> "app:${app.key}"
    is SearchResult.Calculation -> "calc:$expression"
    is SearchResult.WebSearch -> "web:$query"
}

@Composable
private fun AppResultRow(result: SearchResult.App, onClick: () -> Unit) {
    val density = LocalDensity.current
    val iconSizePx = with(density) { 40.dp.roundToPx() }
    val bitmap = remember(result.app.key) {
        result.app.icon.toBitmap(width = iconSizePx, height = iconSizePx)
    }
    ListItem(
        headlineContent = { Text(result.app.label) },
        leadingContent = {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun CalculationResultRow(result: SearchResult.Calculation) {
    val formatted = if (result.result == result.result.toLong().toDouble()) {
        result.result.toLong().toString()
    } else {
        result.result.toString()
    }
    ListItem(
        headlineContent = { Text(formatted, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text("${result.expression} =") },
        leadingContent = { Icon(Icons.Filled.Calculate, contentDescription = null) },
    )
}

@Composable
private fun WebSearchResultRow(result: SearchResult.WebSearch, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.search_web, result.query)) },
        leadingContent = { Icon(Icons.Filled.Language, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
