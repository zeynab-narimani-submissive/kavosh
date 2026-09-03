package dev.omid.kavosh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.omid.kavosh.data.AppInfo
import dev.omid.kavosh.data.AppRepository
import dev.omid.kavosh.data.TagRepository
import dev.omid.kavosh.search.SearchEngine
import dev.omid.kavosh.search.SearchResult
import dev.omid.kavosh.widget.WidgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The one ViewModel behind the whole launcher UI. Everything the screens need — the app
 * list, live search results, which widgets are placed, which tag filter is active — is
 * exposed here as [StateFlow]s so Compose can collect them directly.
 */
class LauncherViewModel(
    private val appRepository: AppRepository,
    private val tagRepository: TagRepository,
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    val apps: StateFlow<List<AppInfo>> =
        appRepository.appsFlow(tagRepository)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTags: StateFlow<Set<String>> =
        tagRepository.allTagsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val placedWidgetIds: StateFlow<List<Int>> =
        widgetRepository.placedWidgetIdsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<SearchResult>> =
        combine(_searchQuery, apps) { query, appList -> SearchEngine.search(query, appList) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _activeTagFilter = MutableStateFlow<String?>(null)
    val activeTagFilter: StateFlow<String?> = _activeTagFilter

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun setTagFilter(tag: String?) {
        _activeTagFilter.value = tag
    }

    fun setTagsForApp(app: AppInfo, tags: Set<String>) {
        viewModelScope.launch { tagRepository.setTags(app.key, tags) }
    }

    fun onWidgetAdded(appWidgetId: Int) {
        viewModelScope.launch { widgetRepository.addWidget(appWidgetId) }
    }

    fun onWidgetRemoved(appWidgetId: Int) {
        viewModelScope.launch { widgetRepository.removeWidget(appWidgetId) }
    }

    class Factory(
        private val appRepository: AppRepository,
        private val tagRepository: TagRepository,
        private val widgetRepository: WidgetRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LauncherViewModel(appRepository, tagRepository, widgetRepository) as T
        }
    }
}
