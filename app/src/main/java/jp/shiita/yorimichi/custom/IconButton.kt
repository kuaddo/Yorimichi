package jp.shiita.yorimichi.custom

import android.content.Context
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import jp.shiita.yorimichi.R

class IconButton(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val imageView: ImageView
    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_icon_button, this, true)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        imageView.isEnabled = enabled
        textView.isEnabled = enabled
    }

    fun setSrc(@DrawableRes resId: Int) = imageView.setImageResource(resId)

    fun setText(text: String) {
        textView.text = text
    }
}