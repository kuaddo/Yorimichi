package jp.shiita.yorimichi.ui.note

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.PenColor
import jp.shiita.yorimichi.databinding.ItemColorBinding

class ColorAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val colors: List<PenColor>

    init {
        val ta = context.resources.obtainTypedArray(R.array.colorsPen)
        colors = (0 until ta.length()).map { PenColor(ta.getColor(it, 0), it % 2 == 0) }
        ta.recycle()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder =
            ColorViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_color, parent, false))

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ColorViewHolder) holder.bind(colors[position])
    }

    class ColorViewHolder(private val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(penColor: PenColor) {
            binding.penColor = penColor
        }
    }
}