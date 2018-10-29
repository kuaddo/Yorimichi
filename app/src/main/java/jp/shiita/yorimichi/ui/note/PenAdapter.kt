package jp.shiita.yorimichi.ui.note

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import jp.shiita.yorimichi.R

class PenAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val pens: List<Pair<PenType, Int>> = listOf(
            PenType.PENCIL to R.drawable.pencil,
            PenType.PENCIL to R.drawable.pencil,
            PenType.PENCIL to R.drawable.pencil)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder =
            PenViewHolder(inflater.inflate(R.layout.item_pen, parent, false))

    override fun getItemCount(): Int = pens.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PenViewHolder) holder.bind(pens[position].second)
    }

    enum class PenType { PENCIL }

    class PenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view as ImageView

        fun bind(resId: Int) {
            imageView.setImageResource(resId)
        }
    }
}