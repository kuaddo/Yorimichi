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
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import jp.shiita.yorimichi.ui.shop.ShopViewModel
import jp.shiita.yorimichi.util.map
import javax.inject.Inject

class NotesViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val posts: LiveData<List<Post>> get() = _posts
    val noContent: LiveData<Boolean> get() = _posts.map { it.isEmpty() }

    private val _posts = MutableLiveData<List<Post>>()

    private val disposables = CompositeDisposable()

    override fun onCleared() = disposables.clear()

    fun getPlacePosts(placeId: String) {
        repository.getPlacePosts(placeId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { _posts.postValue(it) },
                        onError = {
                            Log.e(ShopViewModel.TAG, "onError:getPlacePosts", it)
                        }
                )
                .addTo(disposables)
    }
}