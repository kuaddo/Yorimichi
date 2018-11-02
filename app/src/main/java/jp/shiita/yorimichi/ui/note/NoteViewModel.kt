package jp.shiita.yorimichi.ui.note

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.shiita.yorimichi.custom.PaintView
import javax.inject.Inject

class NoteViewModel @Inject constructor() : ViewModel() {
    val pen: LiveData<PaintView.Pen> get() = _pen
    val penColor: LiveData<Int> get() = _penColor
    val canErase: LiveData<Boolean> get() = _canErase

    val penWidth = MutableLiveData<Float>().apply { value = 20f }

    private val _pen = MutableLiveData<PaintView.Pen>()
    private val _penColor = MutableLiveData<Int>()
    private val _canErase = MutableLiveData<Boolean>().apply { value = false }

    fun setPenColor(color: Int) {
        _penColor.postValue(color)
    }

    fun setPen(pen: PaintView.Pen) {
        _pen.postValue(pen)
        _canErase.postValue(false)
    }

    fun setErase() {
        _pen.postValue(PaintView.Pen.ERASER)
        _canErase.postValue(true)
    }
}