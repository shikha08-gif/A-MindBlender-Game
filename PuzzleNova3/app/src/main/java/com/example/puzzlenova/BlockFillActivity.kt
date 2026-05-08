package com.example.puzzlenova

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

// Import the shared data classes
import com.example.puzzlenova.Cell
import com.example.puzzlenova.BlockPuzzle


class BlockFillActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var boardView: BlockFillView
    private lateinit var levelTextView: TextView
    private lateinit var resetButton: Button
    private lateinit var newGameButton: Button

    // Game State
    private var currentDifficulty = Difficulty.NORMAL
    private var currentLevelIndex = 0
    private lateinit var currentPuzzleList: List<BlockPuzzle>
    private val levelTransitionHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_fill)

        // Find views
        boardView = findViewById(R.id.block_fill_view)
        levelTextView = findViewById(R.id.level_text_view)
        resetButton = findViewById(R.id.reset_button)
        newGameButton = findViewById(R.id.new_game_button)

        // Set button listeners
        resetButton.setOnClickListener { boardView.reset() }
        newGameButton.setOnClickListener { newGame() }

        // Set Win callback
        boardView.onPuzzleComplete = {
            handlePuzzleComplete()
        }

        // Start the first game
        newGame()
    }

    private fun newGame() {
        // Stop any pending level transitions
        levelTransitionHandler.removeCallbacksAndMessages(null)
        showDifficultySelectionDialog()
    }

    private fun loadGame() {
        currentLevelIndex = 0
        currentPuzzleList = when (currentDifficulty) {
            Difficulty.EASY -> easyPuzzles
            Difficulty.NORMAL -> normalPuzzles
            Difficulty.HARD -> hardPuzzles
        }
        loadCurrentLevel()
    }

    private fun loadCurrentLevel() {
        if (currentLevelIndex >= currentPuzzleList.size) {
            showAllLevelsCompleteDialog()
            return
        }

        val puzzle = currentPuzzleList[currentLevelIndex]
        boardView.loadPuzzle(puzzle)
        updateLevelText()
    }

    private fun handlePuzzleComplete() {
        currentLevelIndex++
        if (currentLevelIndex < currentPuzzleList.size) {
            // Automatically load the next level after a short delay
            Toast.makeText(this, "Level Complete!", Toast.LENGTH_SHORT).show()
            levelTransitionHandler.postDelayed({
                loadCurrentLevel()
            }, 1000) // 1 second delay
        } else {
            // All levels for this difficulty are finished
            showAllLevelsCompleteDialog()
        }
    }

    private fun showAllLevelsCompleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("You Win!")
            .setMessage("You've completed all ${currentDifficulty.name.lowercase()} puzzles!")
            .setCancelable(false)
            .setPositiveButton("New Game") { dialog, _ ->
                newGame() // Go back to difficulty selection
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                finish()
            }
            .show()
    }

    private fun updateLevelText() {
        val difficultyName = currentDifficulty.name.capitalize(java.util.Locale.ROOT)
        levelTextView.text = "$difficultyName Level: ${currentLevelIndex + 1} / ${currentPuzzleList.size}"
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

    // --- PUZZLE DATABASE ---
    // (col, row)
    // Using the public Cell() class now

    // 5 Easy Puzzles (5x5 grid)
    private val easyPuzzles = listOf(
        BlockPuzzle(5, Cell(0,0), Cell(4,0), setOf(Cell(0,0), Cell(1,0), Cell(2,0), Cell(3,0), Cell(4,0))), // 1. Line
        BlockPuzzle(5, Cell(0,0), Cell(0,4), setOf(Cell(0,0), Cell(0,1), Cell(0,2), Cell(0,3), Cell(0,4))), // 2. V-Line
        BlockPuzzle(5, Cell(0,0), Cell(2,2), setOf(Cell(0,0), Cell(1,0), Cell(2,0), Cell(2,1), Cell(2,2))), // 3. Corner
        BlockPuzzle(5, Cell(0,0), Cell(3,1), setOf(Cell(0,0), Cell(1,0), Cell(1,1), Cell(2,1), Cell(3,1))), // 4. S-shape
        BlockPuzzle(5, Cell(1,1), Cell(3,3), setOf(Cell(1,1), Cell(2,1), Cell(3,1), Cell(3,2), Cell(3,3), Cell(2,3), Cell(1,3), Cell(1,2), Cell(2,2))) // 5. 3x3 Box
    )

    // 5 Normal Puzzles (Mixed grids)
    private val normalPuzzles = listOf(
        // --- THIS IS THE PUZZLE FROM YOUR SCREENSHOT ---
        BlockPuzzle(6, Cell(0,1), Cell(4,1), setOf(
            Cell(1,1),
            Cell(0,1), Cell(1,1), Cell(2,1), Cell(3,1), Cell(4,1),
            Cell(0,2), Cell(1,2), Cell(3,2), Cell(4,2),
            Cell(0,3), Cell(1,3), Cell(2,3),
            Cell(0,4), Cell(1,4), Cell(2,4),
            Cell(1,5), Cell(2,5)
        )), // 1. 6x6 from screenshot
        BlockPuzzle(6, Cell(0,0), Cell(5,0), setOf(Cell(0,0), Cell(1,0), Cell(2,0), Cell(3,0), Cell(4,0), Cell(5,0), Cell(5,1), Cell(4,1), Cell(3,1), Cell(2,1), Cell(1,1), Cell(0,1))), // 2. 6x6 2 lines
        BlockPuzzle(6, Cell(0,4), Cell(1,4), setOf(
            Cell(1,0), Cell(2,0), Cell(3,0), Cell(4,0),
            Cell(0,1), Cell(1,1), Cell(4,1), Cell(5,1),
            Cell(0,2), Cell(5,2),
            Cell(0,3), Cell(5,3),
            Cell(0,4), Cell(1,4), Cell(2,4), Cell(3,4), Cell(4,4), Cell(5,4)
        )),
        BlockPuzzle(6, Cell(0,0), Cell(0,3), setOf(Cell(0,0), Cell(1,0), Cell(2,0), Cell(2,1), Cell(2,2), Cell(2,3), Cell(1,3), Cell(0,3), Cell(0,1), Cell(0,2), Cell(1,1), Cell(1,2))), // 4. 6x6 U-shape
        BlockPuzzle(5, Cell(3,0), Cell(0,4), setOf(
            Cell(3,0),
            Cell(0,1), Cell(1,1), Cell(2,1), Cell(3,1),
            Cell(0,2), Cell(1,2), Cell(2,2), Cell(3,2),
            Cell(0,3), Cell(1,3), Cell(2,3), Cell(3,3),
            Cell(0,4), Cell(1,4), Cell(2,4)
        ))
    )

    // 5 Hard Puzzles (Mixed grids)
    private val hardPuzzles = listOf(
        BlockPuzzle(7, Cell(0,0), Cell(3,3), setOf(
            Cell(0,0), Cell(1,0), Cell(2,0), Cell(3,0), Cell(4,0), Cell(5,0), Cell(6,0),
            Cell(6,1), Cell(6,2), Cell(6,3), Cell(6,4), Cell(6,5), Cell(6,6),
            Cell(5,6), Cell(4,6), Cell(3,6), Cell(2,6), Cell(1,6), Cell(0,6),
            Cell(0,5), Cell(0,4), Cell(0,3), Cell(0,2), Cell(0,1),
            Cell(1,1), Cell(2,1), Cell(3,1), Cell(4,1), Cell(5,1),
            Cell(5,2), Cell(5,3), Cell(5,4), Cell(5,5),
            Cell(4,5), Cell(3,5), Cell(2,5), Cell(1,5),
            Cell(1,4), Cell(1,3), Cell(1,2),
            Cell(2,2), Cell(3,2), Cell(4,2),
            Cell(4,3), Cell(3,3)
        )),
        BlockPuzzle(7, Cell(0,3), Cell(6,6), setOf(Cell(0,3), Cell(0,2), Cell(0,1), Cell(0,0), Cell(1,0), Cell(2,0), Cell(3,0), Cell(4,0), Cell(5,0), Cell(6,0), Cell(6,1), Cell(5,1), Cell(4,1), Cell(3,1), Cell(2,1), Cell(1,1), Cell(1,2), Cell(2,2), Cell(3,2), Cell(4,2), Cell(5,2), Cell(6,2), Cell(6,3), Cell(5,3), Cell(4,3), Cell(3,3), Cell(2,3), Cell(1,3), Cell(1,4), Cell(2,4), Cell(3,4), Cell(4,4), Cell(5,4), Cell(6,4), Cell(6,5), Cell(5,5), Cell(4,5), Cell(3,5), Cell(2,5), Cell(1,5), Cell(0,5), Cell(0,6), Cell(1,6), Cell(2,6), Cell(3,6), Cell(4,6), Cell(5,6), Cell(6,6))), // 2. New 7x7 Maze
        BlockPuzzle(7, Cell(0,0), Cell(6,0), setOf(Cell(0,0), Cell(1,0), Cell(2,0), Cell(2,1), Cell(1,1), Cell(0,1), Cell(0,2), Cell(1,2), Cell(2,2), Cell(3,2), Cell(4,2), Cell(4,1), Cell(4,0), Cell(5,0), Cell(6,0), Cell(6,4), Cell(5,4), Cell(4,4), Cell(3,4), Cell(3,3), Cell(2,3), Cell(1,3), Cell(0,3), Cell(0,4), Cell(0,5), Cell(1,5), Cell(2,5), Cell(3,5), Cell(4,5), Cell(5,5), Cell(6,5), Cell(6,6), Cell(5,6), Cell(4,6), Cell(3,6), Cell(2,6), Cell(1,6) )), // 3. 7x7 Snake Maze
        BlockPuzzle(7, Cell(3,3), Cell(0,0), setOf(Cell(3,3), Cell(2,3), Cell(1,3), Cell(0,3), Cell(0,2), Cell(0,1), Cell(0,0), Cell(1,0), Cell(2,0), Cell(3,0), Cell(4,0), Cell(5,0), Cell(6,0), Cell(6,1), Cell(6,2), Cell(6,3), Cell(6,4), Cell(6,5), Cell(6,6), Cell(5,6), Cell(4,6), Cell(3,6), Cell(2,6), Cell(1,6), Cell(0,5), Cell(0,4), Cell(1,4), Cell(2,4), Cell(3,4), Cell(4,4), Cell(5,4), Cell(5,5), Cell(4,5), Cell(3,5), Cell(2,5), Cell(1,5), Cell(1,1), Cell(2,1), Cell(3,1), Cell(4,1), Cell(5,1), Cell(4,2), Cell(5,2), Cell(2,2), Cell(1,2))), // 4. 7x7 Maze 2
        BlockPuzzle(7, Cell(0,0), Cell(6,6), setOf(
            Cell(0,0), Cell(1,0), Cell(2,0), Cell(3,0), Cell(4,0), Cell(5,0),
            Cell(1,1), Cell(2,1), Cell(3,1), Cell(4,1), Cell(5,1), Cell(6,1),
            Cell(0,2), Cell(1,2), Cell(2,2), Cell(3,2), Cell(4,2), Cell(5,2), Cell(6,2),
            Cell(0,3), Cell(1,3), Cell(2,3), Cell(3,3), Cell(4,3), Cell(5,3), Cell(6,3),
            Cell(0,4), Cell(1,4), Cell(2,4), Cell(3,4), Cell(4,4), Cell(5,4), Cell(6,4),
            Cell(0,5), Cell(1,5), Cell(2,5), Cell(3,5), Cell(4,5), Cell(5,5), Cell(6,5),
            Cell(0,6), Cell(1,6), Cell(2,6), Cell(3,6), Cell(4,6), Cell(5,6), Cell(6,6)
        ))
    )
}

