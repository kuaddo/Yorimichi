package jp.shiita.yorimichi.ui.shop

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.Post
import jp.shiita.yorimichi.databinding.FragShopBinding
import jp.shiita.yorimichi.databinding.ItemNoteBinding
import jp.shiita.yorimichi.ui.main.MainViewModel
import jp.shiita.yorimichi.util.observe
import javax.inject.Inject

class ShopFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel
            by lazy { ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)}
    private val viewModel: ShopViewModel
            by lazy { ViewModelProviders.of(this, viewModelFactory).get(ShopViewModel::class.java) }
    private lateinit var binding: FragShopBinding
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_shop, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        mainViewModel.setupActionBar(R.string.title_shop)

        noteAdapter = NoteAdapter(context!!, mutableListOf())
        binding.notesRecyclerView.adapter = noteAdapter

        observe()
    }

    private fun observe() {
        viewModel.posts.observe(this) { noteAdapter.reset(it) }
        viewModel.pointsEvent.observe(this) { mainViewModel.setPoints(it) }
    }

    // テストのための実装なのでここで定義する
    class NoteAdapter(context: Context, private val posts: MutableList<Post>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val inflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder =
                NotesViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_note, parent, false))

        override fun getItemCount(): Int = posts.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is NotesViewHolder) holder.bind(posts[position])
        }

        fun reset(posts: List<Post>) {
            this.posts.clear()
            this.posts.addAll(posts)
            notifyDataSetChanged()
        }

        class NotesViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(post: Post) {
                binding.post = post
                binding.executePendingBindings()
            }
        }
    }

    companion object {
        val TAG: String = ShopFragment::class.java.simpleName
        fun newInstance() = ShopFragment()
    }
}