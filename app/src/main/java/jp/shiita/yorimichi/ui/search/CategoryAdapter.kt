package jp.shiita.yorimichi.ui.search

import android.content.Context
import android.content.res.Resources
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import jp.shiita.yorimichi.R

class CategoryAdapter(
        context: Context,
        private val categories: MutableList<Pair<String, Boolean>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val resources = context.resources
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            CategoryViewHolder(inflater.inflate(R.layout.item_category, parent, false), resources)

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoryViewHolder) {
            val category = categories[position]
            holder.bind(category)
            holder.itemView.setOnClickListener {
                categories[position] = category.copy(second = !category.second)
                notifyItemChanged(position)
            }
        }
    }

    class CategoryViewHolder(view: View, private val resources: Resources) : RecyclerView.ViewHolder(view) {
        val text = view as TextView

        fun bind(category: Pair<String, Boolean>) {
            text.text = category.first
            if (category.second) {
                text.setTextColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
                text.setBackgroundResource(R.drawable.back_category_selected)
            }
            else {
                text.setTextColor(ResourcesCompat.getColor(resources, R.color.colorTextSecondary, null))
                text.setBackgroundResource(R.drawable.back_category)
            }
        }
    }
}