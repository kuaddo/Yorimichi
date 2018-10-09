package jp.shiita.yorimichi.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.yorimichi.di.module.fragment.MainModule
import jp.shiita.yorimichi.ui.main.MainActivity

@Suppress("unused")
@Module
abstract class ActivityModule {
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun contributeNotesActivity(): MainActivity
}