package com.example.puzzlenova

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SlidingTileActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var boardView: SlidingTileView
    private lateinit var movesTextView: TextView
    private lateinit var resetButton: Button
    private lateinit var newGameButton: Button

    // Game State
    private var currentDifficulty = Difficulty.NORMAL
    private var moves = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sliding_tile)

        // Find views
        boardView = findViewById(R.id.sliding_tile_view)
        movesTextView = findViewById(R.id.moves_text_view)
        resetButton = findViewById(R.id.reset_button)
        newGameButton = findViewById(R.id.new_game_button)

        // Set button listeners
        resetButton.setOnClickListener { resetGame() }
        newGameButton.setOnClickListener { newGame() }

        // Set callbacks from the custom view
        boardView.onTileMoved = {
            moves++
            updateMovesText()
        }
        boardView.onGameWon = {
            showWinDialog()
        }

        // Start the first game
        newGame()
    }

    private fun newGame() {
        showDifficultySelectionDialog()
    }

    private fun loadGame() {
        moves = 0
        updateMovesText()

        val gridSize = when (currentDifficulty) {
            Difficulty.EASY -> 3 // 3x3
            Difficulty.NORMAL -> 4 // 4x4
            Difficulty.HARD -> 5 // 5x5
        }
        boardView.loadGame(gridSize)
    }

    private fun resetGame() {
        // Just re-shuffle the board, don't change difficulty
        moves = 0
        updateMovesText()
        boardView.shuffle()
    }

    private fun updateMovesText() {
        movesTextView.text = "Moves: $moves"
    }

    private fun showWinDialog() {
        AlertDialog.Builder(this)
            .setTitle("You Win!")
            .setMessage("Congratulations, you solved the puzzle in $moves moves!")
            .setCancelable(false)
            .setPositiveButton("Play Again") { dialog, _ ->
                newGame() // Go back to difficulty selection
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                finish()
            }
            .show()
    }

    private fun showDifficultySelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_difficulty_selection, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group)

        when (currentDifficulty) {
            Difficulty.EASY -> radioGroup.check(R.id.radio_easy)
            Difficulty.NORMAL -> radioGroup.check(R.id.radio_normal)
            Difficulty.HARD -> radioGroup.check(R.id.radio_hard)
        }

        AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Start Game") { dialog, _ ->
                currentDifficulty = when (radioGroup.checkedRadioButtonId) {
                    R.id.radio_easy -> Difficulty.EASY
                    R.id.radio_hard -> Difficulty.HARD
                    else -> Difficulty.NORMAL
                }
                loadGame()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                if (!boardView.isLoaded()) {
                    finish() // Exit if no game is loaded
                }
                dialog.dismiss()
            }
            .show()
    }
}
