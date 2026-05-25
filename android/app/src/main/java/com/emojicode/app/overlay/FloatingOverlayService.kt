package com.emojicode.app.overlay

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.emojicode.app.EmojiCodeApp
import com.emojicode.app.R
import com.emojicode.app.ui.MainActivity

/**
 * 画面オーバーレイで復号結果を表示する常駐サービス。
 *
 * Android 10+ では SYSTEM_ALERT_WINDOW を取得するためにユーザーが
 * 「他のアプリの上に重ねて表示」許可を与える必要がある。
 */
class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: FloatingBubbleView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundIfNeeded()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: ""
        val body = intent?.getStringExtra(EXTRA_BODY) ?: ""
        val original = intent?.getStringExtra(EXTRA_ORIGINAL) ?: ""
        showBubble(title, body, original)
        return START_NOT_STICKY
    }

    private fun startForegroundIfNeeded() {
        val openIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, EmojiCodeApp.CHANNEL_OVERLAY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("メンコン絵文字")
            .setContentText("LINEの暗号を見張っています")
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(FOREGROUND_ID, notif)
    }

    private fun showBubble(title: String, body: String, original: String) {
        if (bubbleView == null) {
            createBubble()
        }
        bubbleView?.setContent(title, body, original)
    }

    private fun createBubble() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels * 0.86f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (resources.displayMetrics.density * 80).toInt()
        }

        val view = FloatingBubbleView(this, onClose = { dismissBubble() })
        view.windowManager = windowManager
        view.layoutParamsRef = params
        try {
            windowManager.addView(view, params)
            bubbleView = view
            layoutParams = params
        } catch (e: Exception) {
            // パーミッションが取り消されていた等
            stopSelf()
        }
    }

    private fun dismissBubble() {
        bubbleView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
        }
        bubbleView = null
        stopSelf()
    }

    override fun onDestroy() {
        bubbleView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
        }
        bubbleView = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_ORIGINAL = "original"
        private const val FOREGROUND_ID = 9001

        fun start(context: Context, title: String, body: String, original: String) {
            val intent = Intent(context, FloatingOverlayService::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                putExtra(EXTRA_ORIGINAL, original)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** オーバーレイ表示権限があるか */
        fun canShow(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        }

        fun openSettings(context: Context) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:" + context.packageName)
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(intent)
        }
    }
}
