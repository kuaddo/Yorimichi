package jp.shiita.yorimichi.di.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.shiita.yorimichi.di.ViewModelFactory
import jp.shiita.yorimichi.di.ViewModelKey
import jp.shiita.yorimichi.ui.main.MainViewModel

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindAddEditNoteViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}