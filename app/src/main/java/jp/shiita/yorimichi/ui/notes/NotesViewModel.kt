package jp.shiita.yorimichi.ui.notes

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.data.Post
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.SingleLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.ui.shop.ShopViewModel
import jp.shiita.yorimichi.util.map
import jp.shiita.yorimichi.util.toSimpleString
import javax.inject.Inject

class NotesViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val posts: LiveData<List<Post>> get() = _posts
    val noContent: LiveData<Boolean> get() = _posts.map { it.isEmpty() }
    val placeText: LiveData<String> get() = _placeText
    val dateTimeText: LiveData<String> get() = _dateTimeText
    val pageText: LiveData<String> get() = _currentPage.map { "${it + 1} / $postsSize" }
    val canBack: LiveData<Boolean> get() = _currentPage.map { it > 0 }
    val canForward: LiveData<Boolean> get() = _currentPage.map { it < postsSize - 1 }

    val scrollBackEvent: LiveData<Int> get() = _scrollBackEvent
    val scrollForwardEvent: LiveData<Int> get() = _scrollForwardEvent

    private val _posts = MutableLiveData<List<Post>>()
    private val _placeText = MutableLiveData<String>()
    private val _dateTimeText = MutableLiveData<String>()
    private val _currentPage = MutableLiveData<Int>().apply { value = 0 }

    private val _scrollBackEvent = SingleLiveEvent<Int>()
    private val _scrollForwardEvent = SingleLiveEvent<Int>()

    private val disposables = CompositeDisposable()
    private var postsSize = 0

    override fun onCleared() = disposables.clear()

    fun setPlaceText(placeText: String) = _placeText.postValue(placeText)

    fun setCurrentPage(page: Int) {
        _currentPage.postValue(page)
        _dateTimeText.postValue(posts.value!![page].createdAtDateTime.toSimpleString())
    }

    fun scrollBack() = _scrollBackEvent.postValue(maxOf(0, _currentPage.value?.minus(1) ?: 0))

    fun scrollForward() = _scrollForwardEvent.postValue(minOf(postsSize, _currentPage.value?.plus(1) ?: 0))

    fun getPlacePosts(placeId: String, dateTime: String) {
        repository.getPlacePosts(placeId, dateTime)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = {
                            postsSize = it.size
                            _posts.postValue(it)
                        },
                        onError = {
                            Log.e(ShopViewModel.TAG, "onError:getPlacePosts", it)
                        }
                )
                .addTo(disposables)
    }
}