package com.devesh.trivio_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for displaying leaderboard data in a RecyclerView
class LeaderboardAdapter(private val data: List<Map<String, Any>>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    // ViewHolder class to hold references to the views for each leaderboard item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tv_user_name)
        val userScore: TextView = itemView.findViewById(R.id.tv_user_score)
    }

    // Inflates the layout for each leaderboard item and returns a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the views for each item in the leaderboard
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = data[position]
        val rank = position + 1
        holder.userName.text = "$rank. ${entry["username"]}"
        holder.userScore.text = "${entry["score"]} points"
    }

    // Returns the total number of items in the leaderboard
    override fun getItemCount(): Int = data.size
}
