package jp.shiita.yorimichi.ui.setting

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.Category
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.databinding.ItemCategorySettingBinding

class CategorySettingAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)
    private val categories = context.resources
            .getStringArray(R.array.place_types)
            .map { Category(it, it in UserInfo.autoSearchCategory) }.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            CategorySettingViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_category_setting, parent, false))

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategorySettingViewHolder) {
            val category = categories[position]
            holder.bind(category)
        }
    }

    fun reflectCategories() {
        UserInfo.autoSearchCategory.clear()
        UserInfo.autoSearchCategory.addAll(categories.filter(Category::selected).map { it.name })
    }

    class CategorySettingViewHolder(private val binding: ItemCategorySettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.category = category
            binding.executePendingBindings()
        }
    }
}