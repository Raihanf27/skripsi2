package project.capstone.percobaan_capstone.history

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import project.capstone.percobaan_capstone.R

class HistoryAdapter(private var historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.historyImage)
        val weightText: TextView = view.findViewById(R.id.historyWeight)
        val categoryText: TextView = view.findViewById(R.id.historyCategory)
        val timestampText: TextView = view.findViewById(R.id.historyTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        Glide.with(holder.itemView.context).load(item.imageUrl).into(holder.imageView)
        holder.weightText.text = "Bobot: ${item.predictedWeight} kg"
        holder.categoryText.text = "Kategori: ${item.weightCategory}"
        holder.timestampText.text = item.timestamp
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newList: List<HistoryItem>) {
        Log.d("HistoryAdapter", "📌 Data sebelum diperbarui: $historyList")
        Log.d("HistoryAdapter", "📌 Data baru yang diterima: $newList")

        historyList = newList
        notifyDataSetChanged()

        Log.d("HistoryAdapter", "✅ Data telah diperbarui, jumlah item: ${historyList.size}")
    }
}
