package jp.shiita.yorimichi.ui.note

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.custom.PaintView
import jp.shiita.yorimichi.data.GoodsResult
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import javax.inject.Inject

class NoteViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val pen: LiveData<PaintView.Pen> get() = _pen
    val penColor: LiveData<Int> get() = _penColor
    val penColors: LiveData<List<GoodsResult.Color>> get() = _penColors
    val canErase: LiveData<Boolean> get() = _canErase

    val uploadSuccessEvent: LiveData<Unit> get() = _uploadSuccessEvent

    val penWidth = MutableLiveData<Float>().apply { value = 20f }

    private val _pen = MutableLiveData<PaintView.Pen>()
    private val _penColor = MutableLiveData<Int>()
    private val _penColors = MutableLiveData<List<GoodsResult.Color>>()
    private val _canErase = MutableLiveData<Boolean>().apply { value = false }

    private val _uploadSuccessEvent = SingleUnitLiveEvent()

    private val disposables = CompositeDisposable()

    override fun onCleared() = disposables.clear()

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

    fun getGoods() {
        repository.getGoods(UserInfo.userId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            val colors = it.colors.apply {
                                // 最初の色を選択済みにする
                                val color = get(0)
                                color.selected
                                _penColor.postValue(color.color)
                            }
                            _penColors.postValue(colors)
                        },
                        onError = {}
                )
                .addTo(disposables)
    }

    fun uploadNote(bytes: ByteArray, placeId: String) {
        repository.postPost(UserInfo.userId, placeId, bytes)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onComplete = { _uploadSuccessEvent.call() },
                        onError = {
                            Log.e(TAG, "onError:postPost", it)
                        }
                )
                .addTo(disposables)
    }

    companion object {
        val TAG: String = NoteViewModel::class.java.simpleName
    }
}