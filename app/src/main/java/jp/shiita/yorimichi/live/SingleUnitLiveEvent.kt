package jp.shiita.yorimichi.live

class SingleUnitLiveEvent : SingleLiveEvent<Unit>() {
    fun call() {
        postValue(Unit)
    }
}