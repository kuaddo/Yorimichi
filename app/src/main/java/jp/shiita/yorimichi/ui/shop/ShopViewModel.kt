package jp.shiita.yorimichi.ui.shop

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.data.Post
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import javax.inject.Inject

class ShopViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val pointsEvent: LiveData<Unit> get() = _pointsEvent

    private val _posts = MutableLiveData<List<Post>>()
    private val _pointsEvent = SingleUnitLiveEvent()

    private val disposables = CompositeDisposable()

    override fun onCleared() = disposables.clear()

    fun visitPlace() {
        val placeId = "testPlaceId"
        repository.visitPlace(UserInfo.userId, placeId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onComplete = { Log.d(TAG, "onComplete:visitPlace") },
                        onError = { Log.e(TAG, "onError:visitPlace", it) }
                )
                .addTo(disposables)
    }

    fun getVisitHistory() {
        repository.getVisitHistory(UserInfo.userId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { Log.e(TAG, "onSuccess:getVisitHistory $it")},
                        onError = { Log.e(TAG, "onError:getVisitHistory", it) }
                )
                .addTo(disposables)
    }

    fun addTenPoints() {
        repository.addPoints(UserInfo.userId, 10)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            UserInfo.points = it.points
                            _pointsEvent.call()
                        },
                        onError = {}
                )
                .addTo(disposables)
    }

    fun purchaseAllIcon() {
        repository.getGoods(UserInfo.userId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            it.icons.forEach { icon ->
                                if (!icon.isPurchased) purchaseGoods(icon.id)
                            }
                        },
                        onError = {}
                )
                .addTo(disposables)
    }

    fun purchaseAllPenColor() {
        repository.getGoods(UserInfo.userId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            it.colors.forEach { color ->
                                if (!color.isPurchased) purchaseGoods(color.id)
                            }
                        },
                        onError = {}
                )
                .addTo(disposables)
    }

    private fun purchaseGoods(goodsId: Int) {
        repository.purchaseGoods(UserInfo.userId, goodsId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { Log.d(TAG, "purchase $goodsId") },
                        onError = {}
                )
                .addTo(disposables)
    }

    companion object {
        val TAG: String = ShopViewModel::class.java.simpleName
    }
}