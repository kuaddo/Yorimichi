package jp.shiita.yorimichi.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.util.getBitmap


class IconButton(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val imageView: AppCompatImageView
    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_icon_button, this, true)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.IconButton, 0, 0).run {
                if (hasValue(R.styleable.IconButton_src)) setSrc(getDrawable(R.styleable.IconButton_src))
                if (hasValue(R.styleable.IconButton_text)) setText(getString(R.styleable.IconButton_text) ?: "")
                if (hasValue(R.styleable.IconButton_iconSize)) setIconSize(getDimensionPixelSize(R.styleable.IconButton_iconSize, 0))
                if (hasValue(R.styleable.IconButton_textSize)) setTextSize(getDimensionPixelSize(R.styleable.IconButton_textSize, 0))
                if (hasValue(R.styleable.IconButton_enabled)) isEnabled = getBoolean(R.styleable.IconButton_enabled, true)
                if (hasValue(R.styleable.IconButton_marginCenter)) textView.layoutParams = textView.layoutParams.also {
                    (it as LayoutParams).marginStart = getLayoutDimension(R.styleable.IconButton_marginCenter, 0)
                }
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

    fun setIconSize(size: Int) {
        val bitmap = imageView.drawable.getBitmap()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false)
        imageView.setImageBitmap(resizedBitmap)
    }

    fun setTextSize(size: Int) = textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
}