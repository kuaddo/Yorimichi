package jp.shiita.yorimichi.ui.note

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.PenColor
import jp.shiita.yorimichi.databinding.ItemColorBinding

class ColorAdapter(
        context: Context,
        private val onClickColor: (color: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val penColors: List<PenColor>

    init {
        val ta = context.resources.obtainTypedArray(R.array.colorsPen)
        penColors = (0 until ta.length()).map { PenColor(ta.getColor(it, 0), it % 2 != 0, false) }
        penColors[0].selected = true
        ta.recycle()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder =
            ColorViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_color, parent, false))

    override fun getItemCount(): Int = penColors.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ColorViewHolder) {
            val penColor = penColors[position]
            holder.bind(penColor)
            if (!penColor.locked) holder.itemView.setOnClickListener {
                onClickColor(penColor.color)
                val index = penColors.indexOfFirst { it.selected }
                if (index == position) return@setOnClickListener

                penColors[index].selected = false
                penColors[position].selected = true
                notifyItemChanged(index)
                notifyItemChanged(position)
            }
        }
    }

    class ColorViewHolder(private val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(penColor: PenColor) {
            binding.penColor = penColor
            binding.executePendingBindings()
        }
    }
}