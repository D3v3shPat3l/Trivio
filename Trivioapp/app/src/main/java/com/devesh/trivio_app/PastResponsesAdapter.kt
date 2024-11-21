package com.devesh.trivio_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// Adapter class for displaying a list of past quiz responses in a RecyclerView.
class PastResponsesAdapter : RecyclerView.Adapter<PastResponsesAdapter.PastResponseViewHolder>() {

    private var pastResponses: List<QuizResponse> = listOf()

    // Creates a new view holder for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PastResponseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_past_response, parent, false)
        return PastResponseViewHolder(view)
    }

    // Binds the data for each past response to the corresponding view holder
    override fun onBindViewHolder(holder: PastResponseViewHolder, position: Int) {
        val response = pastResponses[position]
        holder.bind(response)
    }

    // Returns the total number of past responses in the list
    override fun getItemCount(): Int {
        return pastResponses.size
    }

    // Updates the list of past responses and notifies the adapter of the data change
    fun submitList(responses: List<QuizResponse>) {
        pastResponses = responses
        notifyDataSetChanged()
    }

    // View holder class that holds the views for each item in the RecyclerView
    class PastResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.tv_question)
        private val userAnswerText: TextView = itemView.findViewById(R.id.tv_user_answer)
        private val correctAnswerText: TextView = itemView.findViewById(R.id.tv_correct_answer)
        private val responseTimestampText: TextView = itemView.findViewById(R.id.tv_response_timestamp)

        // Binds the data from the QuizResponse object to the views in the item layout
        fun bind(response: QuizResponse) {
            questionText.text = "Question: ${response.questionText}"
            userAnswerText.text = "Your Answer: ${response.answer}"
            correctAnswerText.text = "Correct Answer: ${if (response.isCorrect) "True" else "False"}"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date(response.timestamp)
            responseTimestampText.text = "Date: ${sdf.format(date)}"
        }
    }
}
