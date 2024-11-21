package com.devesh.trivio_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

// Adapter class for displaying a list of achievements in a RecyclerView
class AchievementAdapter(private val achievements: List<Achievement>) :
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    // Called when the RecyclerView needs a new ViewHolder to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    // Binds data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.nameTextView.text = achievement.name
        holder.statusTextView.text = if (achievement.unlocked) "Unlocked" else "Locked"
        holder.descriptionTextView.text = achievement.description

        val iconName = "achievement${position + 1}"

        val iconResId = if (achievement.unlocked) {

            holder.itemView.context.resources.getIdentifier(
                "${iconName}_unlocked", "drawable", holder.itemView.context.packageName
            )
        } else {

            holder.itemView.context.resources.getIdentifier(
                "${iconName}_locked", "drawable", holder.itemView.context.packageName
            )
        }

        if (iconResId != 0) {
            holder.iconImageView.setImageResource(iconResId)
        } else {
            holder.iconImageView.setImageResource(R.drawable.app_logo)
        }
    }

    // Returns the total number of items in the data set
    override fun getItemCount(): Int {
        return achievements.size
    }

    // ViewHolder class for holding views that represent an individual achievement item
    inner class AchievementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tv_achievement_name)
        val statusTextView: TextView = view.findViewById(R.id.tv_achievement_status)
        val descriptionTextView: TextView = view.findViewById(R.id.tv_achievement_description)
        val iconImageView: ImageView = view.findViewById(R.id.iv_achievement_icon)
    }
}
