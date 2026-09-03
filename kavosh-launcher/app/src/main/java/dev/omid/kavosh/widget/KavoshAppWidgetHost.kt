package dev.omid.kavosh.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

/**
 * Our AppWidgetHost identity. Any app that wants to *host* third-party widgets (clocks,
 * weather, calendar agenda, etc. — the whole reason Kvaesitso-style launchers feel native)
 * needs exactly this: a stable host id, start()/stop() tied to the activity lifecycle, and
 * allocateAppWidgetId() before asking the system to bind a widget.
 *
 * HOST_ID is arbitrary but must stay constant for this app across versions, since Android
 * uses (package, hostId) to track which widgets belong to us.
 */
class KavoshAppWidgetHost(context: Context) : AppWidgetHost(context, HOST_ID) {

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?,
    ): AppWidgetHostView {
        // A plain AppWidgetHostView is enough; Kavosh doesn't need a custom resize handle
        // implementation for v1, but this is the seam where that would go later.
        return AppWidgetHostView(context)
    }

    companion object {
        const val HOST_ID = 0x4B415653 // "KAVS" as an int, just needs to be stable & unique
    }
}

fun AppWidgetManager.isBindAllowed(appWidgetId: Int, providerInfo: AppWidgetProviderInfo): Boolean =
    bindAppWidgetIdIfAllowed(appWidgetId, providerInfo.provider)
