package dev.omid.kavosh.data

import android.graphics.drawable.Drawable

/**
 * Immutable snapshot of a launchable app, as read from PackageManager.
 * [key] is the stable identity used everywhere else in the app (tags, pins, search index).
 */
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable,
    val isSystemApp: Boolean,
    val tags: Set<String> = emptySet(),
) {
    val key: String get() = "$packageName/$activityName"
}
