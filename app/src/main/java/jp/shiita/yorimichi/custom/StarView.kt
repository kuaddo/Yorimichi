package jp.shiita.yorimichi.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.ImageView
import jp.shiita.yorimichi.R

class StarView(context: Context, attrs: AttributeSet? = null) : ImageView(context, attrs) {
    private val star = (ContextCompat.getDrawable(context, R.drawable.ic_star) as BitmapDrawable).bitmap
    private val bitmapWidth = star.width
    private val bitmapHeight = star.height
    private val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)
    private val backDrawable = ContextCompat.getDrawable(context, R.color.colorDarkGrey)
    private val foreDrawable = ContextCompat.getDrawable(context, R.color.colorStar)
    private val layerDrawable = LayerDrawable(arrayOf(backDrawable, foreDrawable)).apply { bounds = Rect(0, 0, bitmapWidth, bitmapHeight) }

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.StarView, 0, 0).run {
                if (hasValue(R.styleable.StarView_ratio)) setRatio(getFloat(R.styleable.StarView_ratio, 0f).toDouble())
                recycle()
            }
        }
    }

    fun setRatio(ratio: Double) {
        foreDrawable?.bounds = Rect(0, 0, (bitmapWidth * ratio).toInt(), bitmapHeight)
        layerDrawable.draw(canvas)
        canvas.drawBitmap(star, 0f, 0f, DST_IN_PAINT)
        setImageBitmap(bitmap)
    }

    companion object {
        private const val PAINT_FLAGS = Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
        private val DST_IN_PAINT = Paint(PAINT_FLAGS).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) }
    }
}