package com.devesh.trivio_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

// DialogFragment to display the summary of the quiz, including the user's score and options to play again or go to the menu.
class QuizSummaryDialog(
    private val score: Int,
    private val totalQuestions: Int,
    private val onPlayAgain: () -> Unit,
    private val onGoToMenu: () -> Unit
) : DialogFragment() {

    // Called when the fragment's view is being created. Inflates the layout and sets up the UI components.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_quiz_summary, container, false)

        val scoreTextView = view.findViewById<TextView>(R.id.tv_score)
        val playAgainButton = view.findViewById<Button>(R.id.btn_play_again)
        val menuButton = view.findViewById<Button>(R.id.btn_menu)
        scoreTextView.text = "You scored $score out of $totalQuestions!"

        playAgainButton.setOnClickListener {
            dismiss()
            onPlayAgain()
        }

        menuButton.setOnClickListener {
            dismiss()
            onGoToMenu()
        }

        return view
    }

    // Adjust the dialog's layout size when it starts
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
