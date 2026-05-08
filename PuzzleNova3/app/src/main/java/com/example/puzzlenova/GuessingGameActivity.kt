package com.example.puzzlenova

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.puzzlenova.Difficulty // Import the Difficulty enum
import kotlin.random.Random

class GuessingGameActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var feedbackTextView: TextView
    private lateinit var guessesLeftTextView: TextView
    private lateinit var instructionsTextView: TextView
    private lateinit var guessInput: EditText
    private lateinit var guessButton: Button
    // private lateinit var startButton: Button // Removed
    private lateinit var gameLayout: RelativeLayout
    private lateinit var resetButton: Button
    private lateinit var newGameButton: Button

    // Game State
    private var secretNumber = 0
    private var guessesLeft = 10
    private var minRange = 1
    private var maxRange = 100
    private var gameActive = false
    private var currentDifficulty = Difficulty.NORMAL

    // Neon Colors
    private val colorNeonCyan = Color.parseColor("#00ffff")
    private val colorNeonBlue = Color.parseColor("#33A1FF")
    private val colorNeonRed = Color.parseColor("#FF5733")
    private val colorNeonGreen = Color.parseColor("#39FF14")
    private val colorNeonRedLose = Color.parseColor("#FF3131")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guessing_game)

        // Find views
        feedbackTextView = findViewById(R.id.feedback_text_view)
        guessesLeftTextView = findViewById(R.id.guesses_left_text_view)
        instructionsTextView = findViewById(R.id.instructions_text_view)
        guessInput = findViewById(R.id.guess_input_edit_text)
        guessButton = findViewById(R.id.guess_button)
        // startButton = findViewById(R.id.start_button) // Removed
        gameLayout = findViewById(R.id.game_layout)
        resetButton = findViewById(R.id.reset_button)
        newGameButton = findViewById(R.id.new_game_button)

        // Set click listeners
        // startButton.setOnClickListener { // Removed
        //     showDifficultySelectionDialog()
        // }
        resetButton.setOnClickListener {
            startGame() // Reset with same difficulty
        }
        newGameButton.setOnClickListener {
            quitGame() // Go back to start screen
        }
        guessButton.setOnClickListener {
            checkGuess()
        }

        // Show difficulty dialog immediately on start
        showDifficultySelectionDialog()
    }

    /**
     * Shows the pop-up dialog to select difficulty
     * This function inflates 'dialog_difficulty_selection.xml'
     */
    private fun showDifficultySelectionDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_difficulty_selection, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.difficulty_radio_group)

        // Set the dialog's radio button to the last-used difficulty
        when (currentDifficulty) {
            Difficulty.EASY -> radioGroup.check(R.id.radio_easy)
            Difficulty.NORMAL -> radioGroup.check(R.id.radio_normal)
            Difficulty.HARD -> radioGroup.check(R.id.radio_hard)
        }

        // Build and show the dialog
        AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Start Game") { dialog, _ ->
                // Read selected difficulty from the dialog
                currentDifficulty = when (radioGroup.checkedRadioButtonId) {
                    R.id.radio_easy -> Difficulty.EASY
                    R.id.radio_hard -> Difficulty.HARD
                    else -> Difficulty.NORMAL
                }
                // Now, start the game
                startGame()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // If game layout isn't visible (i.e., we're starting up),
                // canceling should exit the app.
                if (gameLayout.visibility == View.GONE) {
                    finish()
                } else {
                    // If game is visible, just close the dialog and return to game.
                    dialog.dismiss()
                }
            }
            .show()
    }

    /**
     * Starts or Resets the game with the currently selected difficulty.
     */
    private fun startGame() {
        gameActive = true
        // startButton.visibility = View.GONE // Removed
        gameLayout.visibility = View.VISIBLE

        // Set game parameters based on difficulty
        when (currentDifficulty) {
            Difficulty.EASY -> {
                minRange = 1
                maxRange = 50
                guessesLeft = 8 // More guesses for easy
                instructionsTextView.text = "Guess a number between 1 and 50"
            }
            Difficulty.NORMAL -> {
                minRange = 50
                maxRange = 100
                guessesLeft = 7 // Standard guesses
                instructionsTextView.text = "Guess a number between 50 and 100"
            }
            Difficulty.HARD -> {
                minRange = 100
                maxRange = 200
                guessesLeft = 7 // Fewer guesses for hard
                instructionsTextView.text = "Guess a number between 100 and 200"
            }
        }

        // Generate a new secret number
        secretNumber = Random.nextInt(minRange, maxRange + 1)

        // Reset UI
        guessesLeftTextView.text = "Guesses Left: $guessesLeft"
        feedbackTextView.text = "Enter your guess!"
        feedbackTextView.setTextColor(colorNeonCyan)
        guessInput.text.clear()

        // Enable game buttons
        guessInput.isEnabled = true
        guessButton.isEnabled = true
    }

    /**
     * Quits the current game and returns to the start screen.
     */
    private fun quitGame() {
        gameActive = false
        // startButton.text = "START GAME" // Removed
        // startButton.visibility = View.VISIBLE // Removed
        gameLayout.visibility = View.GONE
        showDifficultySelectionDialog() // Show dialog to start a new game
    }

    private fun checkGuess() {
        if (!gameActive) return

        // Get user guess from EditText
        val userGuess = guessInput.text.toString().toIntOrNull()

        // Input validation
        if (userGuess == null) {
            feedbackTextView.text = "Please enter a valid number."
            feedbackTextView.setTextColor(colorNeonRed)
            return
        }

        // --- Game Logic ---
        guessesLeft--
        guessesLeftTextView.text = "Guesses Left: $guessesLeft"

        when {
            userGuess == secretNumber -> {
                winGame()
            }
            userGuess > secretNumber -> {
                feedbackTextView.text = "$userGuess is TOO HIGH!"
                feedbackTextView.setTextColor(colorNeonRed)
            }
            userGuess < secretNumber -> {
                feedbackTextView.text = "$userGuess is TOO LOW!"
                feedbackTextView.setTextColor(colorNeonBlue)
            }
        }

        // Check for loss
        if (guessesLeft <= 0 && userGuess != secretNumber) {
            loseGame()
        }

        // Clear input for next guess
        guessInput.text.clear()
    }

    private fun winGame() {
        feedbackTextView.text = "CORRECT! The number was $secretNumber!"
        feedbackTextView.setTextColor(colorNeonGreen)
        endGame()
    }

    private fun loseGame() {
        feedbackTextView.text = "GAME OVER! The number was $secretNumber."
        feedbackTextView.setTextColor(colorNeonRedLose)
        endGame()
    }

    private fun endGame() {
        gameActive = false
        // Disable game buttons
        guessInput.isEnabled = false
        guessButton.isEnabled = false
        // The user can now press Reset or New Game
    }
}

