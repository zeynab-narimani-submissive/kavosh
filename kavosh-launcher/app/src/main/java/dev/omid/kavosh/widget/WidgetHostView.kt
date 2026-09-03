package dev.omid.kavosh.widget

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Bridges a system [AppWidgetHostView] into Compose. This is the piece that makes a widget
 * placed by the user (weather, calendar agenda, music controls…) actually render live on
 * the Kavosh home screen, same as it would on any stock launcher.
 */
@Composable
fun WidgetHostView(
    host: KavoshAppWidgetHost,
    appWidgetId: Int,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }

    AndroidView(
        modifier = modifier.fillMaxWidth().height(height),
        factory = {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val view = host.createView(context, appWidgetId, info) as AppWidgetHostView
            view.setAppWidget(appWidgetId, info)
            view
        },
    )
}
