package jp.shiita.yorimichi.ui.note

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.GoodResult
import jp.shiita.yorimichi.databinding.ItemColorBinding

class ColorAdapter(
        context: Context,
        private val colors: MutableList<GoodResult.Color>,
        private val onClickColor: (color: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder =
            ColorViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_color, parent, false))

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ColorViewHolder) {
            val color = colors[position]
            holder.bind(color)
            if (color.isPurchased) {
                holder.itemView.setOnClickListener {
                    onClickColor(color.color)
                    val index = colors.indexOfFirst { it.selected }
                    if (index == position) return@setOnClickListener

                    if (index != -1) {
                        colors[index].selected = false
                        notifyItemChanged(index)
                    }
                    colors[position].selected = true
                    notifyItemChanged(position)
                }
            }
            else {
                holder.itemView.setOnClickListener(null)
            }
        }
    }

    fun reset(colors: List<GoodResult.Color>) {
        this.colors.clear()
        this.colors.addAll(colors)
        notifyDataSetChanged()
    }

    fun resetSelected() {
        val index = colors.indexOfFirst { it.selected }
        if (index == -1) return
        colors[index].selected = false
        notifyItemChanged(index)
    }

    class ColorViewHolder(private val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(color: GoodResult.Color) {
            binding.color = color
            binding.executePendingBindings()
        }
    }
}