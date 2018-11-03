package jp.shiita.yorimichi.di.module.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.yorimichi.ui.main.MainFragment
import jp.shiita.yorimichi.ui.map.MapFragment
import jp.shiita.yorimichi.ui.mypage.MyPageFragment
import jp.shiita.yorimichi.ui.note.NoteFragment
import jp.shiita.yorimichi.ui.search.SearchFragment
import jp.shiita.yorimichi.ui.setting.SettingFragment
import jp.shiita.yorimichi.ui.shop.ShopFragment

@Suppress("unused")
@Module
abstract class MainModule {
    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributeMapFragment(): MapFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeMyPageFragment(): MyPageFragment

    @ContributesAndroidInjector
    abstract fun contributeNoteFragment(): NoteFragment

    @ContributesAndroidInjector
    abstract fun contributeShopFragment(): ShopFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingFragment(): SettingFragment
}