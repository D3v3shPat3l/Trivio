package com.devesh.trivio_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class PastResponsesAdapter : RecyclerView.Adapter<PastResponsesAdapter.PastResponseViewHolder>() {

    private var pastResponses: List<QuizResponse> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PastResponseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_past_response, parent, false)
        return PastResponseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PastResponseViewHolder, position: Int) {
        val response = pastResponses[position]
        holder.bind(response)
    }

    override fun getItemCount(): Int {
        return pastResponses.size
    }

    fun submitList(responses: List<QuizResponse>) {
        pastResponses = responses
        notifyDataSetChanged()
    }

    class PastResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.tv_question)
        private val userAnswerText: TextView = itemView.findViewById(R.id.tv_user_answer)
        private val correctAnswerText: TextView = itemView.findViewById(R.id.tv_correct_answer)
        private val responseTimestampText: TextView = itemView.findViewById(R.id.tv_response_timestamp)

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
