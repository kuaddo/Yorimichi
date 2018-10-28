package jp.shiita.yorimichi.scheduler

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

object ImmediateSchedulerProvider : BaseSchedulerProvider {
    override fun computation(): Scheduler = Schedulers.trampoline()
    override fun io(): Scheduler = Schedulers.trampoline()
    override fun ui(): Scheduler = Schedulers.trampoline()
}