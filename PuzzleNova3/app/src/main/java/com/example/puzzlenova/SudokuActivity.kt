package com.example.puzzlenova

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SudokuActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var boardView: SudokuBoardView
    private lateinit var resetButton: Button
    private lateinit var newGameButton: Button

    private var currentDifficulty = Difficulty.NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        // Find views
        boardView = findViewById(R.id.sudoku_board_view)
        resetButton = findViewById(R.id.reset_button)
        newGameButton = findViewById(R.id.new_game_button)

        // Set button listeners
        resetButton.setOnClickListener { boardView.resetGame() }
        newGameButton.setOnClickListener { newGame() }

        // Set number pad listeners
        findViewById<Button>(R.id.num_1_button).setOnClickListener { boardView.setNumber(1) }
        findViewById<Button>(R.id.num_2_button).setOnClickListener { boardView.setNumber(2) }
        findViewById<Button>(R.id.num_3_button).setOnClickListener { boardView.setNumber(3) }
        findViewById<Button>(R.id.num_4_button).setOnClickListener { boardView.setNumber(4) }
        findViewById<Button>(R.id.num_5_button).setOnClickListener { boardView.setNumber(5) }
        findViewById<Button>(R.id.num_6_button).setOnClickListener { boardView.setNumber(6) }
        findViewById<Button>(R.id.num_7_button).setOnClickListener { boardView.setNumber(7) }
        findViewById<Button>(R.id.num_8_button).setOnClickListener { boardView.setNumber(8) }
        findViewById<Button>(R.id.num_9_button).setOnClickListener { boardView.setNumber(9) }
        findViewById<Button>(R.id.clear_button).setOnClickListener { boardView.setNumber(0) } // 0 means clear

        // Set Win callback
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
        // This is a pre-defined, solved puzzle.
        val fullPuzzle = "534678912672195348198342567859761423426853791713924856961537284287419635345286179"

        // Convert string to IntArray
        val puzzle = fullPuzzle.map { it.toString().toInt() }.toIntArray()

        // Determine how many clues to show based on difficulty
        val cluesToShow = when (currentDifficulty) {
            Difficulty.EASY -> 45 // Lots of clues
            Difficulty.NORMAL -> 35
            Difficulty.HARD -> 25 // Few clues
        }

        boardView.loadPuzzle(puzzle, cluesToShow)
    }

    private fun showWinDialog() {
        AlertDialog.Builder(this)
            .setTitle("You Win!")
            .setMessage("Congratulations, you solved the puzzle!")
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
                if (!boardView.isGameLoaded()) {
                    finish() // Exit if no game is loaded
                }
                dialog.dismiss()
            }
            .show()
    }
}
