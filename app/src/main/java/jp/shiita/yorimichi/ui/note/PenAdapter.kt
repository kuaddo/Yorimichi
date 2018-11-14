package jp.shiita.yorimichi.ui.note

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.custom.PaintView

class PenAdapter(
        context: Context,
        private val onClickPen: (pen: PaintView.Pen) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val pens: MutableList<Triple<PaintView.Pen, Int, Boolean>> = mutableListOf(
            Triple(PaintView.Pen.Normal, R.drawable.pencil, true),
            Triple(PaintView.Pen.Flat, R.drawable.pencil, false))

    init {
        onClickPen(pens[0].first)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder =
            PenViewHolder(inflater.inflate(R.layout.item_pen, parent, false))

    override fun getItemCount(): Int = pens.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PenViewHolder) {
            val pen = pens[position]
            holder.bind(pen.second, pen.third)
            holder.itemView.setOnClickListener {
                onClickPen(pen.first)
                val index = pens.indexOfFirst { pen -> pen.third }
                if (index == position) return@setOnClickListener

                if (index != -1) {
                    pens[index] = pens[index].copy(third = false)
                    notifyItemChanged(index)
                }
                pens[position] = pens[position].copy(third = true)
                notifyItemChanged(position)
            }
        }
    }

    fun resetSelected() {
        val index = pens.indexOfFirst { it.third }
        if (index == -1) return
        pens[index] = pens[index].copy(third = false)
        notifyItemChanged(index)
    }

    class PenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val selectedImage: ImageView = view.findViewById(R.id.selectedImage)
        private val penImage: ImageView = view.findViewById(R.id.penImage)

        fun bind(resId: Int, selected: Boolean) {
            penImage.setImageResource(resId)
            if (selected) selectedImage.visibility = View.VISIBLE
            else          selectedImage.visibility = View.GONE
        }
    }
}