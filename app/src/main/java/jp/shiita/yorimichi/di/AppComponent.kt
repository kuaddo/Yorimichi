package jp.shiita.yorimichi.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import jp.shiita.yorimichi.YorimichiApp
import jp.shiita.yorimichi.di.module.ActivityModule
import jp.shiita.yorimichi.di.module.DataModule
import jp.shiita.yorimichi.di.module.ViewModelModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityModule::class,
    ViewModelModule::class,
    DataModule::class
])
interface AppComponent : AndroidInjector<YorimichiApp> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: YorimichiApp): Builder
        fun build(): AppComponent
    }

    override fun inject(app: YorimichiApp)
}