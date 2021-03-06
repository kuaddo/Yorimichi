package jp.shiita.yorimichi.ui.main

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.GoodsResult
import jp.shiita.yorimichi.databinding.ItemIconBinding

class IconAdapter(
        context: Context,
        private val icons: MutableList<GoodsResult.Icon>,
        private val changeIcon: (iconId: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            IconViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_icon, parent, false))

    override fun getItemCount(): Int = icons.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IconViewHolder) {
            val icon = icons[position]
            holder.bind(icon)
            if (icon.isPurchased) holder.itemView.setOnClickListener { changeIcon(icon.id) }
        }
    }

    fun reset(icons: List<GoodsResult.Icon>) {
        this.icons.clear()
        this.icons.addAll(icons)
        notifyDataSetChanged()
    }

    class IconViewHolder(private val binding: ItemIconBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(icon: GoodsResult.Icon) {
            binding.icon = icon
            binding.executePendingBindings()
        }
    }
}