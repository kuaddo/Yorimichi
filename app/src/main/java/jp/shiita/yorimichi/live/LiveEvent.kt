package jp.shiita.yorimichi.live

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.annotation.MainThread
import java.util.concurrent.CopyOnWriteArrayList

/**
 * [SingleLiveEvent]で不十分である、複数のオブザーバがある場合に利用
 */
open class LiveEvent<T> : MutableLiveData<T>() {
    private val dispatchedTagList = CopyOnWriteArrayList<String>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        super.observe(owner, Observer<T> {
            val internalTag = owner::class.java.name
            if (!dispatchedTagList.contains(internalTag)) {
                dispatchedTagList.add(internalTag)
                observer.onChanged(it)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        dispatchedTagList.clear()
        super.setValue(t)
    }
}