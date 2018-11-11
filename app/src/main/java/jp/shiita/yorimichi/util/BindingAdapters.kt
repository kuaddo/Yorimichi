package jp.shiita.yorimichi.util

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.support.annotation.ColorInt
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage
import jp.shiita.yorimichi.custom.WidthSelector

@BindingAdapter("app:url")
fun ImageView.bindImageUrl(url: String) = GlideApp.with(context).load(url).into(this)

@BindingAdapter("app:bucket", "app:image", requireAll = true)
fun ImageView.bindImageCloudStrage(bucket: String, image: String) = FirebaseStorage
        .getInstance(bucket)
        .getReference(image)
        .downloadUrl
        .addOnSuccessListener { uri -> GlideApp.with(context).load(uri).into(this) }

@BindingAdapter("app:bucket", "app:image", "app:tint", requireAll = true)
fun ImageView.bindImageCloudStrageTint(bucket: String, image: String, @ColorInt tintColor: Int) = FirebaseStorage
        .getInstance(bucket)
        .getReference(image)
        .downloadUrl
        .addOnSuccessListener { uri ->
            GlideApp.with(context)
                    .load(uri)
                    .transform(TintTransformation(tintColor))
                    .into(this)
        }

@BindingAdapter("app:tint")
fun ImageView.bindTint(@ColorInt tintColor: Int) = setImageDrawable(drawable.setTintCompat(tintColor))

@BindingAdapter("app:color")
fun WidthSelector.bindColor(@ColorInt tintColor: Int) = setColor(tintColor)

@BindingAdapter("app:penWidth")
fun WidthSelector.bindPenWidth(width: Float) {
    if (penWidth != width) penWidth = width
}

@InverseBindingAdapter(attribute = "app:penWidth")
fun WidthSelector.inverseBindPenWidth(): Float = penWidth

@BindingAdapter("penWidthAttrChanged")
fun WidthSelector.bindListener(listener: InverseBindingListener) {
    penWidthChangedListener = listener::onChange
}