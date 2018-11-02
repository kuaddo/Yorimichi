package jp.shiita.yorimichi.ui.note

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.custom.PaintView
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import javax.inject.Inject

class NoteViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val pen: LiveData<PaintView.Pen> get() = _pen
    val penColor: LiveData<Int> get() = _penColor
    val canErase: LiveData<Boolean> get() = _canErase

    val penWidth = MutableLiveData<Float>().apply { value = 20f }

    private val _pen = MutableLiveData<PaintView.Pen>()
    private val _penColor = MutableLiveData<Int>()
    private val _canErase = MutableLiveData<Boolean>().apply { value = false }

    private val disposables = CompositeDisposable()

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

    fun uploadNote(bytes: ByteArray) {
        repository.postPost(UserInfo.userId, TEST_PLACE_UID, bytes)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onComplete = {
                            Log.d(TAG, "onComplete:postPost")
                        },
                        onError = {
                            Log.e(TAG, "onError:postPost", it)
                        }
                )
                .addTo(disposables)
    }

    companion object {
        val TAG: String = NoteViewModel::class.java.simpleName
        const val TEST_PLACE_UID = "testPlaceUid"
    }
}