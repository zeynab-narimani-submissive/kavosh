package dev.omid.kavosh

import android.app.Application
import dev.omid.kavosh.data.AppRepository
import dev.omid.kavosh.data.TagRepository
import dev.omid.kavosh.widget.KavoshAppWidgetHost
import dev.omid.kavosh.widget.WidgetRepository

/**
 * Holds the app's few long-lived singletons. No DI framework — the object graph is tiny
 * enough that a hand-rolled service locator keeps things easy to follow.
 */
class LauncherApp : Application() {

    lateinit var appRepository: AppRepository
        private set
    lateinit var tagRepository: TagRepository
        private set
    lateinit var widgetRepository: WidgetRepository
        private set
    lateinit var appWidgetHost: KavoshAppWidgetHost
        private set

    override fun onCreate() {
        super.onCreate()
        appRepository = AppRepository(this)
        tagRepository = TagRepository(this)
        widgetRepository = WidgetRepository(this)
        appWidgetHost = KavoshAppWidgetHost(this)
    }
}
