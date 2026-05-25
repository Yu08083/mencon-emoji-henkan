package com.emojicode.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class EmojiCodeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_OVERLAY,
                "オーバーレイサービス",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "フローティング窓を表示中の常駐通知"
                setShowBadge(false)
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DECODED,
                "復号メッセージ",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "LINEから検知した暗号メッセージの復号結果"
            }
        )
    }

    companion object {
        const val CHANNEL_OVERLAY = "overlay_service"
        const val CHANNEL_DECODED = "decoded_messages"
    }
}
