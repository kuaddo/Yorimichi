package jp.shiita.yorimichi.util

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage

@BindingAdapter("app:bucket", "app:image", requireAll = true)
fun ImageView.bindImage(bucket: String, image: String) = FirebaseStorage
        .getInstance(bucket)
        .getReference(image)
        .downloadUrl
        .addOnSuccessListener { uri -> GlideApp.with(this.context).load(uri).into(this) }

@BindingAdapter("app:tint")
fun ImageView.bindTint(color: Int) {
    setImageDrawable(drawable.setTintCompat(color))
}
