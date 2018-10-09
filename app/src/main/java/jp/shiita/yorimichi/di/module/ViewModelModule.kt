package jp.shiita.yorimichi.di.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.shiita.yorimichi.di.ViewModelFactory
import jp.shiita.yorimichi.di.ViewModelKey
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.ui.mypage.MyPageViewModel
import jp.shiita.yorimichi.ui.search.SearchViewModel

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyPageViewModel::class)
    abstract fun bindMyPageViewModel(viewModel: MyPageViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}