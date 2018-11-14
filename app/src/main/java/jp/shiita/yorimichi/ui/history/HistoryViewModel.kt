package jp.shiita.yorimichi.ui.history

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.util.toSimpleDateString
import javax.inject.Inject

class HistoryViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val placesAndDates: LiveData<Pair<List<PlaceResult.Place>, List<String>>> get() = _placesAndDates
    val noContent: LiveData<Boolean> get() = _noContent

    private val _placesAndDates = MutableLiveData<Pair<List<PlaceResult.Place>, List<String>>>()
    private val _noContent = MutableLiveData<Boolean>()

    private val disposables = CompositeDisposable()

    override fun onCleared() = disposables.clear()

    fun getHistory() {
        repository.getVisitHistory(UserInfo.userId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            if (it.isEmpty()) _noContent.postValue(true)
                            else {
                                val placeIds = it.map { h -> h.placeUid }
                                val dateStrings = it.map { h -> h.createdAtDateTime.toSimpleDateString() }
                                getPlaces(placeIds, dateStrings)
                            }
                        },
                        onError = {}
                )
    }

    private fun getPlaces(placeIds: List<String>, dateStrings: List<String>) {
        Observable.fromIterable(placeIds)
                .flatMap { getPlace(it) }
                .toList()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { _placesAndDates.postValue(it to dateStrings) },
                        onError = {}
                )
    }

    private fun getPlace(placeId: String): Observable<PlaceResult.Place> =
            repository.getPlaceDetail(placeId)
                    .map { it.result.place }
                    .toObservable()
}