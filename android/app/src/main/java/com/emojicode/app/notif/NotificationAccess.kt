package com.emojicode.app.notif

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

object NotificationAccess {
    /** 「通知へのアクセス」がアプリに付与されているか */
    fun isGranted(context: Context): Boolean {
        val pkg = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        ) ?: return false
        if (TextUtils.isEmpty(flat)) return false
        val target = ComponentName(context, LineNotificationListener::class.java)
        return flat.split(":").any {
            ComponentName.unflattenFromString(it) == target
        }
    }

    /** 設定画面を開く */
    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
