package dev.omid.kavosh.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Single source of truth for "which apps exist on this device and how do we launch them".
 * Re-queries PackageManager whenever apps are installed/updated/removed, and merges in
 * user-assigned tags from [TagRepository].
 */
class AppRepository(private val context: Context) {

    private val packageManager: PackageManager get() = context.packageManager

    /** Emits the full app list once at start, then again on every install/uninstall/update. */
    private fun rawAppListFlow(): Flow<List<AppInfo>> = callbackFlow {
        fun query() = trySend(loadInstalledApps()).isSuccess

        query()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                query()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter)

        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun loadInstalledApps(): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)

        return resolved
            .asSequence()
            .distinctBy { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
            .filterNot { it.activityInfo.packageName == context.packageName }
            .map { info ->
                val activityInfo = info.activityInfo
                val isSystem = (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                AppInfo(
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name,
                    label = info.loadLabel(packageManager).toString(),
                    icon = info.loadIcon(packageManager),
                    isSystemApp = isSystem,
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** Full app list with tags merged in, kept up to date as either apps or tags change. */
    fun appsFlow(tagRepository: TagRepository): Flow<List<AppInfo>> =
        combine(rawAppListFlow(), tagRepository.tagsByAppFlow()) { apps, tagsByApp ->
            apps.map { app -> app.copy(tags = tagsByApp[app.key].orEmpty()) }
        }.distinctUntilChanged()

    fun launchIntentFor(app: AppInfo): Intent =
        Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = android.content.ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }

    fun appInfoDetailsIntent(app: AppInfo): Intent =
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", app.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
}
