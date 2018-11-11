package jp.shiita.yorimichi.util

import android.graphics.*
import android.support.annotation.ColorInt
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import java.security.MessageDigest


class TintTransformation(@ColorInt private val tintColor: Int) : BitmapTransformation() {
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height

        val config = if (toTransform.config != null) toTransform.config else Bitmap.Config.ARGB_8888
        val bitmap = pool.get(width, height, config)

        Paint().apply {
            isAntiAlias = true
            if (tintColor != Color.TRANSPARENT)
                colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            Canvas(bitmap).drawBitmap(toTransform, 0f, 0f, this)
        }

        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        messageDigest.update(tintColor.toByte())
    }

    override fun equals(other: Any?): Boolean =
            other is TintTransformation && tintColor == other.tintColor

    override fun hashCode(): Int =
            Util.hashCode(ID.hashCode(), Util.hashCode(tintColor))

    companion object {
        private val ID = TintTransformation::class.java.name
        private val ID_BYTES = ID.toByteArray(Charsets.UTF_8)
    }
}