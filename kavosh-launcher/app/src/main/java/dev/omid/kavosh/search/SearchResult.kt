package dev.omid.kavosh.search

import dev.omid.kavosh.data.AppInfo

sealed interface SearchResult {
    data class App(val app: AppInfo, val score: Int) : SearchResult
    data class Calculation(val expression: String, val result: Double) : SearchResult
    data class WebSearch(val query: String) : SearchResult
}
