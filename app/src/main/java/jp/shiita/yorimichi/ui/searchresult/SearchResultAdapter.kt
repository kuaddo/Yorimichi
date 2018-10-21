package jp.shiita.yorimichi.ui.searchresult

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.databinding.ItemSearchResultBinding

class SearchResultAdapter(
        context: Context,
        private val locations: MutableList<LatLng>,
        private val showMarker: (position: Int) -> Unit,
        private val hideMarker: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            SearchResultViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_search_result, parent, false))

    override fun getItemCount(): Int = locations.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SearchResultViewHolder) holder.binding.latLng = locations[position]
        showMarker(position)
    }

    fun addAll(locations: List<LatLng>) {
        val start = itemCount
        this.locations.addAll(locations)
        notifyItemRangeInserted(start, locations.size)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        hideMarker(holder.adapterPosition)
    }

    class SearchResultViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)
}