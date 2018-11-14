package jp.shiita.yorimichi.di.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.shiita.yorimichi.di.ViewModelFactory
import jp.shiita.yorimichi.di.ViewModelKey
import jp.shiita.yorimichi.ui.history.HistoryViewModel
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.ui.map.MapViewModel
import jp.shiita.yorimichi.ui.note.NoteViewModel
import jp.shiita.yorimichi.ui.notes.NotesViewModel
import jp.shiita.yorimichi.ui.remind.RemindViewModel
import jp.shiita.yorimichi.ui.search.SearchViewModel
import jp.shiita.yorimichi.ui.setting.SettingViewModel
import jp.shiita.yorimichi.ui.shop.ShopViewModel

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapViewModel::class)
    abstract fun bindMapViewModel(viewModel: MapViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NoteViewModel::class)
    abstract fun bindNoteViewModel(viewModel: NoteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShopViewModel::class)
    abstract fun bindShopViewModel(viewModel: ShopViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    abstract fun bindSettingViewModel(viewModel: SettingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RemindViewModel::class)
    abstract fun bindRemindViewModel(viewModel: RemindViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotesViewModel::class)
    abstract fun bindNotesViewModel(viewModel: NotesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HistoryViewModel::class)
    abstract fun bindHistoryViewModel(viewModel: HistoryViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}