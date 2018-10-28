package jp.shiita.yorimichi.ui.note

import android.arch.lifecycle.ViewModel
import javax.inject.Inject

class NoteViewModel @Inject constructor() : ViewModel() {
    val bucket = "gs://yorimichi_posts"
    val cloudStoragePath = "1-20181021233530516912.png"
}