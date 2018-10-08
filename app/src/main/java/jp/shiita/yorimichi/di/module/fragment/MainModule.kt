package jp.shiita.yorimichi.di.module.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.yorimichi.ui.main.MainFragment

@Suppress("unused")
@Module
abstract class MainModule {
    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment
}