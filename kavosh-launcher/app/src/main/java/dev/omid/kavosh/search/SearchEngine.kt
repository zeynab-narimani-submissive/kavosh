package dev.omid.kavosh.search

import dev.omid.kavosh.data.AppInfo

/**
 * Turns a raw query string into a ranked list of [SearchResult]s: matching apps first
 * (fuzzy, typo-tolerant), then an inline calculator result if the query looks like math,
 * then a "search the web" fallback so the query is never a dead end.
 */
object SearchEngine {

    fun search(query: String, apps: List<AppInfo>): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val results = mutableListOf<SearchResult>()

        Calculator.tryEvaluate(trimmed)?.let { value ->
            results += SearchResult.Calculation(trimmed, value)
        }

        apps.asSequence()
            .mapNotNull { app -> fuzzyScore(trimmed, app.label)?.let { score -> app to score } }
            .sortedByDescending { it.second }
            .take(30)
            .forEach { (app, score) -> results += SearchResult.App(app, score) }

        // Also let tags act as search terms: "کار" -> everything tagged "کار".
        val tagMatches = apps.filter { app ->
            app.tags.any { tag -> tag.contains(trimmed, ignoreCase = true) }
        }.filterNot { app -> results.any { it is SearchResult.App && it.app.key == app.key } }
        tagMatches.forEach { app -> results += SearchResult.App(app, score = 50) }

        results += SearchResult.WebSearch(trimmed)

        return results
    }

    /**
     * Typo-tolerant, subsequence-based fuzzy match (same family of algorithm as fzf/Kvaesitso's
     * fuzzy search). Returns null when [query] doesn't match at all, otherwise a score where
     * higher = better (exact prefix matches score highest, scattered subsequence matches lowest).
     */
    private fun fuzzyScore(query: String, target: String): Int? {
        val q = query.lowercase()
        val t = target.lowercase()

        if (t == q) return 1000
        if (t.startsWith(q)) return 800 - (t.length - q.length)
        if (t.contains(q)) return 500 - t.indexOf(q)

        // Word-boundary match: "gm" matches "Google Maps"
        val initials = t.split(" ", "-", "_")
            .mapNotNull { it.firstOrNull() }
            .joinToString("")
        if (initials.startsWith(q)) return 400

        // Subsequence fuzzy match with a penalty for gaps between matched characters.
        var qi = 0
        var lastMatch = -1
        var gapPenalty = 0
        for (ti in t.indices) {
            if (qi >= q.length) break
            if (t[ti] == q[qi]) {
                if (lastMatch != -1) gapPenalty += (ti - lastMatch - 1)
                lastMatch = ti
                qi++
            }
        }
        if (qi != q.length) return null // not all query chars matched, in order
        return (200 - gapPenalty).coerceAtLeast(1)
    }
}
