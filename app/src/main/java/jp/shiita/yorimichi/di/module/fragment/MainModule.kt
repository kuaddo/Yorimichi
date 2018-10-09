package jp.shiita.yorimichi.di.module.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.yorimichi.ui.BlankFragment
import jp.shiita.yorimichi.ui.main.MainFragment
import jp.shiita.yorimichi.ui.search.SearchFragment
import jp.shiita.yorimichi.ui.searchresult.SearchResultFragment

@Suppress("unused")
@Module
abstract class MainModule {
    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchResultFragment(): SearchResultFragment

    @ContributesAndroidInjector
    abstract fun contributeBlankFragment(): BlankFragment
}