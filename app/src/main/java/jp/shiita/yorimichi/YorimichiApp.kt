package jp.shiita.yorimichi

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import jp.shiita.yorimichi.di.DaggerAppComponent

class YorimichiApp : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
                .application(this)
                .build()
    }
}