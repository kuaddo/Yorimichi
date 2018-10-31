package jp.shiita.yorimichi.ui.searchresult

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.databinding.ItemSearchResultBinding

class PlaceAdapter(
        context: Context,
        private val places: MutableList<PlaceResult.Place>,
        private val onSelected: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            PlaceViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_search_result, parent, false))

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PlaceViewHolder) {
            holder.bind(places[position])
            holder.itemView.setOnClickListener {
                onSelected(position)
                select(position)
            }
        }
    }

    fun reset(places: List<PlaceResult.Place>) {
        this.places.clear()
        this.places.addAll(places)
        notifyDataSetChanged()
    }

    fun select(position: Int) {
        val indices = places
                .mapIndexed { i, place -> if (place.selected) i else -1 }
                .filterNot { it == -1 }
        if (position in indices) return

        places[position].selected = true
        notifyItemChanged(position)
        indices.forEach {
            places[it].selected = false
            notifyItemChanged(it)
        }
    }

    fun getSelectedPosition(): Int {
        val place = places.firstOrNull { it.selected } ?: return -1
        return places.indexOf(place)
    }

    fun sortDistAsc() {
        places.sortBy { it.getDistance() }
        notifyDataSetChanged()
    }

    fun sortDistDesc() {
        places.sortByDescending { it.getDistance() }
        notifyDataSetChanged()
    }

    class PlaceViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: PlaceResult.Place) {
            binding.place = place
        }
    }
}