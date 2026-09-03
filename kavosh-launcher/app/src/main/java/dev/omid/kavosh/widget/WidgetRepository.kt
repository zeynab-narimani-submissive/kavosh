package dev.omid.kavosh.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore by preferencesDataStore(name = "kavosh_widgets")

/**
 * Persists which AppWidget IDs the user has placed on the home screen, plus a simple
 * top-to-bottom order. The actual widget rendering lives in [KavoshAppWidgetHost] /
 * [WidgetHostView] — this class only remembers *which* widgets exist across restarts.
 */
class WidgetRepository(private val context: Context) {

    private val widgetOrderKey = stringSetPreferencesKey("placed_widget_ids")
    private fun widgetPositionKey(widgetId: Int) = intPreferencesKey("widget_pos::$widgetId")

    fun placedWidgetIdsFlow(): Flow<List<Int>> =
        context.widgetDataStore.data.map { prefs ->
            val ids = prefs[widgetOrderKey].orEmpty().mapNotNull { it.toIntOrNull() }
            ids.sortedBy { id -> prefs[widgetPositionKey(id)] ?: Int.MAX_VALUE }
        }

    suspend fun addWidget(widgetId: Int) {
        context.widgetDataStore.edit { prefs ->
            val current = prefs[widgetOrderKey].orEmpty()
            prefs[widgetOrderKey] = current + widgetId.toString()
            prefs[widgetPositionKey(widgetId)] = current.size
        }
    }

    suspend fun removeWidget(widgetId: Int) {
        context.widgetDataStore.edit { prefs ->
            val current = prefs[widgetOrderKey].orEmpty()
            prefs[widgetOrderKey] = current - widgetId.toString()
        }
    }
}
