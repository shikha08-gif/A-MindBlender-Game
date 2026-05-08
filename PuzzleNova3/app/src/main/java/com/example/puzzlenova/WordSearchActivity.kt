package com.example.puzzlenova

import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class WordSearchActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var wordSearchView: WordSearchView
    private lateinit var wordsListTextView: TextView
    private lateinit var scoreTextView: TextView // Added for score
    private lateinit var resetButton: Button
    private lateinit var newGameButton: Button

    // Game State
    private var currentDifficulty = Difficulty.NORMAL
    private lateinit var currentWordList: List<String>
    private val foundWords = mutableSetOf<String>()

    // Word lists for different difficulties
    private val easyWords = listOf(
        "CAT", "DOG", "SUN", "RUN", "BIG", "CAR", "SKY", "FLY", "HOT", "RED",
        "BED", "EAT", "FUN", "MAT", "SIT", "TOP", "TEN", "TWO", "YES", "NO"
    )
    private val normalWords = listOf(
        "PHONE", "APPLE", "BEACH", "HOUSE", "MUSIC", "WATER", "GREEN", "HAPPY", "SMILE", "EARTH",
        "TRAIN", "LIGHT", "STORM", "TIGER", "OCEAN", "PIZZA", "DANCE", "CHAIR", "TABLE", "MAGIC"
    )

    // --- UPDATED HARD WORDS (not Android related) ---
    private val hardWords = listOf(
        "ASTEROID", "SYMPHONY", "VOLCANO", "GALAXY", "JUPITER", "ELEPHANT", "CHOCOLATE", "MICROSCOPE",
        "TELESCOPE", "ADVENTURE", "UNIVERSE", "MYSTERY", "TREASURE", "ORCHESTRA", "KANGAROO",
        "EQUATOR", "PYRAMID", "VIOLIN", "ZEBRA", "GUITAR", "PENGUIN", "QUARTZ"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_search)

        // Find views
        wordSearchView = findViewById(R.id.word_search_view)
        wordsListTextView = findViewById(R.id.words_list_text_view)
        scoreTextView = findViewById(R.id.score_text_view) // Added for score
        resetButton = findViewById(R.id.reset_button)
        newGameButton = findViewById(R.id.new_game_button)

        // Set button listeners
        resetButton.setOnClickListener { resetGame() }
        newGameButton.setOnClickListener { newGame() }

        // Set the callback for when a word is found
        wordSearchView.onWordFound = { word ->
            wordFound(word)
        }

        // Start the first game
        newGame()
    }

    private fun newGame() {
        showDifficultySelectionDialog()
    }

    private fun resetGame() {
        // --- UPDATED ---
        // Re-loads the game with 6 new random words
        loadGame()
    }

    private fun loadGame() {
        val gridSize: Int
        val wordPool: List<String>

        when (currentDifficulty) {
            Difficulty.EASY -> {
                gridSize = 8
                wordPool = easyWords
            }
            Difficulty.NORMAL -> {
                gridSize = 12
                wordPool = normalWords
            }
            Difficulty.HARD -> {
                gridSize = 13 // Changed from 15
                wordPool = hardWords
            }
        }

        // --- UPDATED ---
        // Randomly select 6 words from the pool
        currentWordList = wordPool.shuffled().take(6)

        // Setup the game
        foundWords.clear()
        wordSearchView.setup(gridSize, currentWordList)
        updateWordsListUI()
    }

    private fun wordFound(word: String) {
        if (foundWords.add(word)) {
            // New word found
            updateWordsListUI()
            if (foundWords.size == currentWordList.size) {
                // All words found!
                showWinDialog()
            }
        }
    }

    private fun updateWordsListUI() {
        // Update score
        scoreTextView.text = "Found: ${foundWords.size} / ${currentWordList.size}"

        // Update strikethrough list
        val fullText = SpannableString(currentWordList.joinToString("  "))

        var startIndex = 0
        for (word in currentWordList) {
            if (foundWords.contains(word)) {
                fullText.setSpan(
                    StrikethroughSpan(),
                    startIndex,
                    startIndex + word.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            startIndex += word.length + 2 // +2 for the spaces
        }
        wordsListTextView.text = fullText
    }

    private fun showWinDialog() {
        AlertDialog.Builder(this)
            .setTitle("You Win!")
            .setMessage("Congratulations, you found all the words!")
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
                if (wordSearchView.isGridGenerated()) {
                    dialog.dismiss() // Just close dialog
                } else {
                    finish() // Exit app if no game has started
                }
            }
            .show()
    }
}

