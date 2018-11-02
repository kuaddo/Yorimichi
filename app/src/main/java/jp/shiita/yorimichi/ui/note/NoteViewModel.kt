package jp.shiita.yorimichi.ui.note

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import javax.inject.Inject

class NoteViewModel @Inject constructor() : ViewModel() {
    val penColor: LiveData<Int> get() = _penColor
    val canErase: LiveData<Boolean> get() = _canErase

    private val _penColor = MutableLiveData<Int>()
    private val _canErase = MutableLiveData<Boolean>().apply { value = false }

    fun setPenColor(color: Int) {
        _penColor.postValue(color)
        _canErase.postValue(false)
    }

    fun setErase() {
        _penColor.postValue(-1)     // white
        _canErase.postValue(true)
    }
}