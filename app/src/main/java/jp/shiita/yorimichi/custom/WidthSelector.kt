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
    private val maxRatio = 3f
    private val minRatio = 0.1f
    private var beforeY = 0f
    var penWidthChangedListener: (() -> Unit)? = null

    var widthRatio = 1f
        set(value) {
            field = maxOf(minRatio, minOf(maxRatio, value))
            val ratio = 0.5f - widthRatio / (2 * maxRatio)       // 0 ~ 0.5
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
                if (hasValue(R.styleable.WidthSelector_widthRatio)) widthRatio = getFloat(R.styleable.WidthSelector_widthRatio, 0f)
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
                widthRatio += (beforeY - event.y) / 100
                penWidthChangedListener?.invoke()
                beforeY = event.y
            }
            MotionEvent.ACTION_UP -> {
                widthRatio += (beforeY - event.y) / 100
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