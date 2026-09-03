package dev.omid.kavosh.ui.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.omid.kavosh.data.AppInfo
import dev.omid.kavosh.ui.home.PINNED_TAG

/**
 * Long-press context menu for an app icon: pin/unpin to dock, edit tags, open system app-info,
 * uninstall (hidden for system apps, since PackageManager will refuse those anyway).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: AppInfo,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onEditTags: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = app.label,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            val isPinned = PINNED_TAG in app.tags
            ListItem(
                headlineContent = { Text(if (isPinned) "برداشتن از داک" else "پین به داک") },
                leadingContent = { Icon(Icons.Filled.PushPin, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onTogglePin),
            )
            ListItem(
                headlineContent = { Text("مدیریت برچسب‌ها") },
                leadingContent = { Icon(Icons.Filled.Sell, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onEditTags),
            )
            ListItem(
                headlineContent = { Text("اطلاعات اپ") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onAppInfo),
            )
            if (!app.isSystemApp) {
                ListItem(
                    headlineContent = { Text("حذف نصب") },
                    leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onUninstall),
                )
            }
        }
    }
}
