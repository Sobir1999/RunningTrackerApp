package com.example.runningtrackerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.db.model.Run
import com.example.runningtrackerapp.utility.TrackingUtility
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter: RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    val differCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this,differCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    inner class RunViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_run,parent,false)
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply{
            Glide.with(this).load(run.img).into(ivRunImage)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd:MM:yy",Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)
            val evgSpeed = "${run.avgSpeedInKMH}kmh"
            tvAvgSpeed.text = evgSpeed
            val totalDistance = "${run.distanceInMeters / 1000f}km"
            tvDistance.text = totalDistance
            val caloriesBurned = "${run.caloriesBurned}kcal"
            tvCalories.text = caloriesBurned
            tvTime.text = TrackingUtility().getFormattedStopWatchTimer(run.timeInMillis)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}