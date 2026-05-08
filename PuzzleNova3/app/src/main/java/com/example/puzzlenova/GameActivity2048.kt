package com.example.puzzlenova

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class GameActivity2048 : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var scoreTextView: TextView
    private lateinit var highScoreTextView: TextView
    private lateinit var gameBoard: Board
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var currentDifficulty: Difficulty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2048_game)

        sharedPrefs = getSharedPreferences("2048_PREFS", Context.MODE_PRIVATE)

        gameView = findViewById(R.id.game_view)
        scoreTextView = findViewById(R.id.score_text_view)
        highScoreTextView = findViewById(R.id.high_score_text_view)

        loadHighScore()

        // --- Button Setup ---
        findViewById<Button>(R.id.new_game_button).setOnClickListener {
            showDifficultySelectionDialog()
        }

        findViewById<Button>(R.id.reset_button).setOnClickListener {
            if (::gameBoard.isInitialized) {
                gameBoard.reset()
                gameView.invalidate()
            } else {
                showDifficultySelectionDialog()
            }
        }

        // Start the first game
        showDifficultySelectionDialog()
    }

    private fun loadHighScore() {
        val highscore = sharedPrefs.getInt("HIGHSCORE", 0)
        highScoreTextView.text = highscore.toString()
    }

    private fun updateHighScore(currentScore: Int) {
        val currentHighScore = sharedPrefs.getInt("HIGHSCORE", 0)
        if (currentScore > currentHighScore) {
            with(sharedPrefs.edit()) {
                putInt("HIGHSCORE", currentScore)
                apply()
            }
            highScoreTextView.text = currentScore.toString()
        }
    }

    fun showDifficultySelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_difficulty_selection, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group)
        radioGroup.check(R.id.radio_normal)

        AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Start Game") { dialog, _ ->
                val selectedId = radioGroup.checkedRadioButtonId

                // --- THIS IS THE FIX ---
                // We swapped the mapping for EASY and HARD
                val difficulty = when (selectedId) {
                    R.id.radio_easy -> Difficulty.HARD // <-- Was EASY
                    R.id.radio_hard -> Difficulty.EASY // <-- Was HARD
                    else -> Difficulty.NORMAL
                }
                // --- END OF FIX ---

                startGame(difficulty)
                dialog.dismiss()
            }
            .show()
    }

    private fun startGame(difficulty: Difficulty) {
        currentDifficulty = difficulty
        gameBoard = Board(difficulty)

        // Set up listeners on the new board
        gameBoard.setOnScoreUpdateListener { newScore ->
            scoreTextView.text = newScore.toString()
            updateHighScore(newScore)
        }
        gameBoard.setOnGameOverListener { message, finalScore ->
            showGameOverDialog(message, finalScore)
        }

        gameView.setBoard(gameBoard)
        scoreTextView.text = "0"
        gameView.invalidate()
    }

    fun showGameOverDialog(message: String, finalScore: Int) {
        AlertDialog.Builder(this)
            .setTitle(message)
            .setMessage("Final Score: $finalScore\nHigh Score: ${highScoreTextView.text}")
            .setPositiveButton("Play Again") { dialog, _ ->
                showDifficultySelectionDialog()
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}