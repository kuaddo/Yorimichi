package jp.shiita.yorimichi.custom

import android.content.Context
import android.support.constraint.Guideline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.util.setTintCompat

class WidthSelector(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val indicator: ImageView
    private val top: Guideline
    private val bottom: Guideline
    private val left: Guideline
    private val right: Guideline
    private val maxWidth = 100f
    private val minWidth = 1f
    private var beforeY = 0f
    var penWidthChangedListener: (() -> Unit)? = null

    var penWidth = 20f
        set(value) {
            field = maxOf(minWidth, minOf(maxWidth, value))
            val ratio = 0.5f - penWidth / (2 * maxWidth)       // 0 ~ 0.5
            top.setGuidelinePercent(ratio)
            bottom.setGuidelinePercent(1 - ratio)
            left.setGuidelinePercent(ratio)
            right.setGuidelinePercent(1 - ratio)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_width_selector, this, true)
        indicator = findViewById(R.id.indicator)
        top = findViewById(R.id.guidelineTop)
        bottom = findViewById(R.id.guidelineBottom)
        left = findViewById(R.id.guidelineLeft)
        right = findViewById(R.id.guidelineRight)

        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.WidthSelector, 0, 0).run {
                if (hasValue(R.styleable.WidthSelector_penWidth)) penWidth = getFloat(R.styleable.WidthSelector_penWidth, 0f)
                if (hasValue(R.styleable.WidthSelector_color)) setColor(getColor(R.styleable.WidthSelector_color, 0))
                recycle()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                beforeY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                penWidth += (beforeY - event.y) / 3
                penWidthChangedListener?.invoke()
                beforeY = event.y
            }
            MotionEvent.ACTION_UP -> {
                penWidth += (beforeY - event.y) / 3
                penWidthChangedListener?.invoke()
            }
        }
        return true
    }

    fun setColor(color: Int) {
        indicator.setImageDrawable(indicator.drawable.setTintCompat(color))
        indicator.invalidate()
    }
}