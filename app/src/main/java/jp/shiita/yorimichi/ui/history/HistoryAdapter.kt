package jp.shiita.yorimichi.ui.history

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.PlaceResult
import jp.shiita.yorimichi.databinding.ItemHistoryBinding

class HistoryAdapter(
        context: Context,
        private val places: MutableList<PlaceResult.Place>,
        private val dateStrings: MutableList<String>,
        private val selectPlace: (place: PlaceResult.Place) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            HistoryViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_history, parent, false))

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HistoryViewHolder) {
            val place = places[position]
            holder.bind(place, dateStrings[position])
            holder.itemView.setOnClickListener { selectPlace(place) }
        }
    }

    fun reset(places: List<PlaceResult.Place>, dateStrings: List<String>) {
        this.places.clear()
        this.places.addAll(places)
        this.dateStrings.clear()
        this.dateStrings.addAll(dateStrings)
        notifyDataSetChanged()
    }

    class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: PlaceResult.Place, dateString: String) {
            binding.place = place
            binding.dateString = dateString
            binding.executePendingBindings()
        }
    }
}