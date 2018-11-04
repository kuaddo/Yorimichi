package jp.shiita.yorimichi.ui.map

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
        private val selectPlace: (position: Int) -> Unit,
        private val goto: (placeId: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            PlaceViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_search_result, parent, false))

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PlaceViewHolder) {
            holder.bind(places[position], goto)
            holder.itemView.setOnClickListener { selectPlace(position) }
        }
    }

    fun getItem(position: Int) = places[position]

    fun reset(places: List<PlaceResult.Place>) {
        this.places.clear()
        this.places.addAll(places)
        notifyDataSetChanged()
    }

    fun select(position: Int) {
        val index = places.indexOfFirst { it.selected }
        if (index == position) return

        if (index != -1) {
            places[index].selected = false
            notifyItemChanged(index)
        }
        places[position].selected = true
        notifyItemChanged(position)
    }

    fun getSelectedPosition(): Int {
        val place = places.firstOrNull { it.selected } ?: return -1
        return places.indexOf(place)
    }

    fun sortByDistAsc() {
        places.sortBy { it.getDistance() }
        notifyDataSetChanged()
    }

    fun sortByDistDesc() {
        places.sortByDescending { it.getDistance() }
        notifyDataSetChanged()
    }

    class PlaceViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: PlaceResult.Place,
                 goto: (placeId: String) -> Unit) {
            binding.place = place
            binding.gotoButton.setOnClickListener { goto(place.placeId) }
            binding.executePendingBindings()
        }
    }
}