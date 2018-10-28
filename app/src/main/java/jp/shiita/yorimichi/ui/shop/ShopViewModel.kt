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
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import javax.inject.Inject

class ShopViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val posts: LiveData<List<Post>> get() = _posts

    private val _posts = MutableLiveData<List<Post>>()

    private val disposables = CompositeDisposable()

    private val testPlaceUid = "testPlaceUid"

    override fun onCleared() = disposables.clear()

    fun postPost(bytes: ByteArray) {
        repository.postPost(UserInfo.userId, testPlaceUid, bytes)
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

    fun getUserPosts() {
        repository.getUserPosts(UserInfo.userId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { posts ->
                            if (posts.isEmpty()) return@subscribeBy
                            posts.forEach { Log.d(TAG, it.toString()) }
                            _posts.postValue(posts)
                        },
                        onError = {
                            Log.e(TAG, "onError:getUserPosts", it)
                        }
                )
                .addTo(disposables)
    }

    fun getPlacePosts() {
        repository.getPlacePosts(testPlaceUid)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { posts ->
                            if (posts.isEmpty()) return@subscribeBy
                            posts.forEach { Log.d(TAG, it.toString()) }
                            _posts.postValue(posts)
                        },
                        onError = {
                            Log.e(TAG, "onError:getPlacePosts", it)
                        }
                )
                .addTo(disposables)
    }

    companion object {
        val TAG: String = ShopViewModel::class.java.simpleName
    }
}