package com.emojicode.app.notif

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.emojicode.app.EmojiCodeApp
import com.emojicode.app.R
import com.emojicode.app.cipher.Decoder
import com.emojicode.app.overlay.FloatingOverlayService
import com.emojicode.app.storage.CipherRepository
import com.emojicode.app.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * LINEを含む各種SNSアプリの通知をリスンし、暗号メッセージを検知。
 * 復号した結果をフローティング窓 or 別通知で表示する。
 *
 * 注意: ユーザーが「設定 > 通知へのアクセス」で許可しないと有効化されない。
 */
class LineNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repo: CipherRepository

    // 直近に処理した通知の重複防止
    private val seenKeys = ArrayDeque<String>()
    private val seenLimit = 50

    override fun onCreate() {
        super.onCreate()
        repo = CipherRepository(applicationContext)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (!TARGET_PACKAGES.contains(sbn.packageName)) return

        // 自分自身の通知は無視
        if (sbn.packageName == applicationContext.packageName) return

        val key = sbn.key + "@" + sbn.postTime
        if (seenKeys.contains(key)) return
        seenKeys.addLast(key)
        while (seenKeys.size > seenLimit) seenKeys.removeFirst()

        scope.launch { handle(sbn) }
    }

    private suspend fun handle(sbn: StatusBarNotification) {
        if (!repo.autoDetect.first()) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val combined = listOf(text, bigText).firstOrNull { it.isNotBlank() } ?: return

        val cipher = repo.currentCipher()
        if (!Decoder.containsCipherEmoji(combined, cipher)) return

        val decoded = Decoder.decode(combined, cipher)
        if (decoded == combined) return  // 変換結果が同じなら無視

        // フローティング窓 or 通知
        if (repo.overlayEnabled.first() && FloatingOverlayService.canShow(applicationContext)) {
            FloatingOverlayService.start(applicationContext, title, decoded, combined)
        } else {
            postDecodedNotification(title.ifBlank { sbn.packageName }, decoded)
        }
    }

    private fun postDecodedNotification(title: String, body: String) {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(this, EmojiCodeApp.CHANNEL_DECODED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔓 $title")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), notif)
    }

    companion object {
        /** 通知を検知する対象アプリ */
        val TARGET_PACKAGES = setOf(
            "jp.naver.line.android",        // LINE
            // ここに追加すれば他のアプリでも動く
            // "com.whatsapp",
            // "org.telegram.messenger",
        )
    }
}
