package dev.omid.kavosh

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dev.omid.kavosh.data.AppInfo
import dev.omid.kavosh.ui.LauncherRoot
import dev.omid.kavosh.ui.theme.KavoshTheme

class MainActivity : ComponentActivity() {

    private val app get() = application as LauncherApp

    private val viewModel: LauncherViewModel by viewModels {
        LauncherViewModel.Factory(app.appRepository, app.tagRepository, app.widgetRepository)
    }

    /** Set right before launching the widget-pick flow so the result handler knows which id to keep/drop. */
    private var pendingWidgetId: Int = -1

    private val configureWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK && pendingWidgetId != -1) {
            viewModel.onWidgetAdded(pendingWidgetId)
        } else if (pendingWidgetId != -1) {
            app.appWidgetHost.deleteAppWidgetId(pendingWidgetId)
        }
        pendingWidgetId = -1
    }

    private val pickWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val widgetId = result.data?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (result.resultCode != RESULT_OK || widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (pendingWidgetId != -1) app.appWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = -1
            return@registerForActivityResult
        }

        val awm = AppWidgetManager.getInstance(this)
        val info = awm.getAppWidgetInfo(widgetId)
        pendingWidgetId = widgetId

        if (info?.configure != null) {
            val configureIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            configureWidgetLauncher.launch(configureIntent)
        } else {
            viewModel.onWidgetAdded(widgetId)
            pendingWidgetId = -1
        }
    }

    private val uninstallLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { /* app list refreshes itself via the PACKAGE_REMOVED broadcast in AppRepository */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KavoshTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherRoot(
                        viewModel = viewModel,
                        widgetHost = app.appWidgetHost,
                        onLaunchApp = ::launchApp,
                        onOpenAppInfo = ::openAppInfo,
                        onUninstallApp = ::uninstallApp,
                        onRequestAddWidget = ::startAddWidgetFlow,
                        onWebSearch = ::performWebSearch,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        app.appWidgetHost.startListening()
    }

    override fun onPause() {
        app.appWidgetHost.stopListening()
        super.onPause()
    }

    private fun launchApp(appInfo: AppInfo) {
        try {
            startActivity(app.appRepository.launchIntentFor(appInfo))
        } catch (_: Exception) {
            // App may have been uninstalled between the tap and the launch; the drawer will
            // refresh on the next PACKAGE_REMOVED broadcast, nothing else to do here.
        }
    }

    private fun openAppInfo(appInfo: AppInfo) {
        startActivity(app.appRepository.appInfoDetailsIntent(appInfo))
    }

    private fun uninstallApp(appInfo: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", appInfo.packageName, null)
        }
        uninstallLauncher.launch(intent)
    }

    private fun performWebSearch(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(android.app.SearchManager.QUERY, query)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return
        }
        // Fall back to opening a browser search URL directly when no ACTION_WEB_SEARCH
        // handler is registered (common on AOSP-less / de-Googled devices).
        val fallback = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)),
        )
        if (fallback.resolveActivity(packageManager) != null) {
            startActivity(fallback)
        }
    }

    private fun startAddWidgetFlow() {
        val widgetId = app.appWidgetHost.allocateAppWidgetId()
        pendingWidgetId = widgetId
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putParcelableArrayListExtra(
                AppWidgetManager.EXTRA_CUSTOM_INFO,
                arrayListOf<android.os.Parcelable>(),
            )
            putParcelableArrayListExtra(
                AppWidgetManager.EXTRA_CUSTOM_EXTRAS,
                arrayListOf<android.os.Parcelable>(),
            )
        }
        pickWidgetLauncher.launch(pickIntent)
    }
}
