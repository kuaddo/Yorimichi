package jp.shiita.yorimichi.ui.notes

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.Post
import jp.shiita.yorimichi.databinding.ItemNoteBinding

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
            binding.imageView.setImageDrawable(null)    // 以前の画像が残る現象の対応
            binding.post = post
            binding.executePendingBindings()
        }
    }
}