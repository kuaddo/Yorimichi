package jp.shiita.yorimichi.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import jp.shiita.yorimichi.R


class IconButton(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val imageView: ImageView
    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_icon_button, this, true)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.IconButton, 0, 0).run {
                if (hasValue(R.styleable.IconButton_src)) setSrc(getDrawable(R.styleable.IconButton_src))
                if (hasValue(R.styleable.IconButton_text)) setText(getString(R.styleable.IconButton_text) ?: "")
                if (hasValue(R.styleable.IconButton_enabled)) isEnabled = getBoolean(R.styleable.IconButton_enabled, true)
                recycle()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        imageView.isEnabled = enabled
        textView.isEnabled = enabled
    }

    fun setSrc(drawable: Drawable?) = imageView.setImageDrawable(drawable)

    fun setSrc(@DrawableRes resId: Int) = imageView.setImageResource(resId)

    fun setText(text: String) {
        textView.text = text
    }
}