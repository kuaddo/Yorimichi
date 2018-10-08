package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {
    val text: LiveData<String>
        get() = _text

    private val _text = MutableLiveData<String>()

    fun start() {
        _text.postValue("hello world")
    }
}