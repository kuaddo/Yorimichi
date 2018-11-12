package jp.shiita.yorimichi.util

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
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
        .addOnSuccessListener { uri -> GlideApp.with(this.context).load(uri).into(this) }

@BindingAdapter("app:tint")
fun ImageView.bindTint(color: Int) = setImageDrawable(drawable.setTintCompat(color))

@BindingAdapter("app:color")
fun WidthSelector.bindColor(color: Int) = setColor(color)

@BindingAdapter("app:widthRatio")
fun WidthSelector.bindWidthRatio(ratio: Float) {
    if (widthRatio != ratio) widthRatio = ratio
}

@InverseBindingAdapter(attribute = "app:widthRatio")
fun WidthSelector.inverseBindWidthRatio(): Float = widthRatio

@BindingAdapter("widthRatioAttrChanged")
fun WidthSelector.bindListener(listener: InverseBindingListener) {
    penWidthChangedListener = listener::onChange
}