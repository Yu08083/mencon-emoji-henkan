package com.emojicode.app.overlay

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

/**
 * 復号結果を表示するフローティング窓。
 * - 角丸ダーク背景＋アクセントの金線
 * - ドラッグで移動可能
 * - 右上の×で閉じる
 * - タップで折りたたみ/展開
 */
class FloatingBubbleView(
    context: Context,
    private val onClose: () -> Unit,
) : FrameLayout(context) {

    private val titleView: TextView
    private val bodyView: TextView
    private val originalView: TextView
    private val toggleSourceBtn: ImageButton
    private val expandedContent: LinearLayout

    private var collapsed = false
    private val originalShown get() = originalView.visibility == View.VISIBLE

    init {
        val dark = isDarkMode()
        val bgColor = if (dark) Color.parseColor("#161D2F") else Color.parseColor("#FAF6EC")
        val borderColor = if (dark) Color.parseColor("#D4A85A") else Color.parseColor("#8B6F47")
        val textColor = if (dark) Color.parseColor("#EBE5D6") else Color.parseColor("#1A2942")
        val mutedColor = if (dark) Color.parseColor("#888C9B") else Color.parseColor("#7A8499")

        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(16f)
            setColor(bgColor)
            setStroke(dp(1.5f).toInt(), borderColor)
        }

        elevation = dp(8f)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14f).toInt(), dp(12f).toInt(), dp(14f).toInt(), dp(12f).toInt())
        }
        addView(root, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // 上段（タイトル＋閉じる）
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        titleView = TextView(context).apply {
            textSize = 11f
            setTextColor(mutedColor)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            letterSpacing = 0.08f
            text = "🔓 EMOJI CODE"
        }
        topRow.addView(titleView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        toggleSourceBtn = ImageButton(context).apply {
            setImageDrawable(makeCircleGlyph(context, "⤓", textColor))
            background = null
            layoutParams = LinearLayout.LayoutParams(dp(28f).toInt(), dp(28f).toInt())
            setOnClickListener { toggleSource() }
        }
        topRow.addView(toggleSourceBtn)

        val closeBtn = ImageButton(context).apply {
            setImageDrawable(makeCircleGlyph(context, "×", textColor))
            background = null
            layoutParams = LinearLayout.LayoutParams(dp(28f).toInt(), dp(28f).toInt())
            setOnClickListener { onClose() }
        }
        topRow.addView(closeBtn)
        root.addView(topRow)

        expandedContent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8f).toInt(), 0, 0)
        }

        bodyView = TextView(context).apply {
            textSize = 15f
            setTextColor(textColor)
            setLineSpacing(0f, 1.35f)
            maxLines = 6
            movementMethod = ScrollingMovementMethod()
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        expandedContent.addView(bodyView)

        originalView = TextView(context).apply {
            textSize = 13f
            setTextColor(mutedColor)
            setLineSpacing(0f, 1.35f)
            maxLines = 3
            ellipsize = android.text.TextUtils.TruncateAt.END
            visibility = View.GONE
            setPadding(0, dp(8f).toInt(), 0, 0)
        }
        expandedContent.addView(originalView)

        root.addView(expandedContent)

        // タイトルクリックで折りたたみ
        topRow.isClickable = true
        topRow.setOnClickListener { toggleCollapse() }
    }

    fun setContent(title: String, decoded: String, original: String) {
        if (title.isNotBlank()) titleView.text = "🔓 $title"
        bodyView.text = decoded
        originalView.text = "元: $original"
    }

    private fun toggleCollapse() {
        collapsed = !collapsed
        expandedContent.visibility = if (collapsed) View.GONE else View.VISIBLE
    }

    private fun toggleSource() {
        originalView.visibility = if (originalShown) View.GONE else View.VISIBLE
    }

    private fun isDarkMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun dp(v: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics)

    private fun makeCircleGlyph(ctx: Context, glyph: String, tint: Int): android.graphics.drawable.Drawable {
        // 単純化: TextView を Drawableにせず、ImageButtonに直接Textを描く代わりに
        // BitmapDrawable を生成
        val size = dp(22f).toInt()
        val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = tint
            textSize = size * 0.78f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val fm = paint.fontMetrics
        val y = size / 2f - (fm.ascent + fm.descent) / 2f
        canvas.drawText(glyph, size / 2f, y, paint)
        return android.graphics.drawable.BitmapDrawable(resources, bmp)
    }

    // ===== ドラッグ移動 =====
    private var initialX = 0
    private var initialY = 0
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var dragged = false
    var onMoved: ((x: Int, y: Int) -> Unit)? = null
    var layoutParamsRef: WindowManager.LayoutParams? = null
    var windowManager: WindowManager? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val params = layoutParamsRef ?: return super.onTouchEvent(event)
        val wm = windowManager ?: return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x; initialY = params.y
                touchStartX = event.rawX; touchStartY = event.rawY
                dragged = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - touchStartX
                val dy = event.rawY - touchStartY
                if (!dragged && (abs(dx) > dp(6f) || abs(dy) > dp(6f))) dragged = true
                if (dragged) {
                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    try { wm.updateViewLayout(this, params) } catch (e: Exception) {}
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragged) onMoved?.invoke(params.x, params.y)
                return dragged  // タップで子要素のクリック処理に流す
            }
        }
        return super.onTouchEvent(event)
    }
}
