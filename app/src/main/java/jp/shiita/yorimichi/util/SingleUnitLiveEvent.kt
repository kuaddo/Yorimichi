package jp.shiita.yorimichi.util

class SingleUnitLiveEvent : SingleLiveEvent<Unit>() {
    fun call() {
        postValue(Unit)
    }
}