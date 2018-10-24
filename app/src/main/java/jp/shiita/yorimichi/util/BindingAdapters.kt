package jp.shiita.yorimichi.util

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage

@BindingAdapter("app:bucket", "app:cloud_storage_path", requireAll = true)
fun ImageView.bindImage(bucket: String, path: String) = FirebaseStorage
        .getInstance(bucket)
        .getReference(path)
        .downloadUrl
        .addOnSuccessListener { uri -> GlideApp.with(this.context).load(uri).into(this) }