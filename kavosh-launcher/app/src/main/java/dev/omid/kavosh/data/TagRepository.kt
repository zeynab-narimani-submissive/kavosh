package dev.omid.kavosh.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tagDataStore by preferencesDataStore(name = "kavosh_tags")

/**
 * Stores the user's own tags for apps (e.g. "کار", "بازی", "شبکه‌های اجتماعی").
 * Backed by Jetpack DataStore so it's async, crash-safe, and observable.
 *
 * Storage shape: one preference key per app (its [AppInfo.key]) holding a string set of tags,
 * plus one registry key listing every tag name that has ever been used (so the tag picker /
 * drawer-by-tag view can list tags even for apps not yet loaded).
 */
class TagRepository(private val context: Context) {

    private fun appKeyPref(appKey: String) = stringSetPreferencesKey("app_tags::$appKey")
    private val allTagsPref = stringSetPreferencesKey("all_tags_registry")

    /** appKey -> set of tag names, for every app that has at least one tag. */
    fun tagsByAppFlow(): Flow<Map<String, Set<String>>> =
        context.tagDataStore.data.map { prefs ->
            prefs.asMap()
                .entries
                .filter { it.key.name.startsWith("app_tags::") }
                .associate { (key, value) ->
                    key.name.removePrefix("app_tags::") to (value as? Set<String> ?: emptySet())
                }
        }

    fun allTagsFlow(): Flow<Set<String>> =
        context.tagDataStore.data.map { it[allTagsPref].orEmpty() }

    suspend fun setTags(appKey: String, tags: Set<String>) {
        context.tagDataStore.edit { prefs ->
            if (tags.isEmpty()) {
                prefs.remove(appKeyPref(appKey))
            } else {
                prefs[appKeyPref(appKey)] = tags
            }
            val registry = prefs[allTagsPref].orEmpty()
            prefs[allTagsPref] = registry + tags
        }
    }

    suspend fun addTag(appKey: String, tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isEmpty()) return
        context.tagDataStore.edit { prefs ->
            val current = prefs[appKeyPref(appKey)].orEmpty()
            prefs[appKeyPref(appKey)] = current + trimmed
            prefs[allTagsPref] = prefs[allTagsPref].orEmpty() + trimmed
        }
    }

    suspend fun removeTag(appKey: String, tag: String) {
        context.tagDataStore.edit { prefs ->
            val current = prefs[appKeyPref(appKey)].orEmpty()
            prefs[appKeyPref(appKey)] = current - tag
        }
    }
}
