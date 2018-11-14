package jp.shiita.yorimichi.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.LayerDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.util.getBitmap

class StarView(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {
    private val star = ResourcesCompat.getDrawable(resources, R.drawable.ic_star, null)!!.getBitmap()
    private val bitmapWidth = star.width
    private val bitmapHeight = star.height
    private val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)
    private val backDrawable = ResourcesCompat.getDrawable(resources, R.color.colorDarkGrey, null)
    private val foreDrawable = ResourcesCompat.getDrawable(resources, R.color.colorStar, null)
    private val layerDrawable = LayerDrawable(arrayOf(backDrawable, foreDrawable)).apply { bounds = Rect(0, 0, bitmapWidth, bitmapHeight) }

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.StarView, 0, 0).run {
                if (hasValue(R.styleable.StarView_ratio)) setRatio(getFloat(R.styleable.StarView_ratio, 0f))
                recycle()
            }
        }
    }

    fun setRatio(ratio: Float) {
        require(ratio in 0..1)

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