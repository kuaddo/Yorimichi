package jp.shiita.yorimichi.live

/**
 * [SingleUnitLiveEvent]で不十分である、複数のオブザーバがある場合に利用
 */
open class UnitLiveEvent : LiveEvent<Unit>() {
    fun call() {
        postValue(Unit)
    }
}