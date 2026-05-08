package com.example.puzzlenova

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameHubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Find the CardViews and set click listeners for each game
        setupGameCardListeners()
    }

    /**
     * Sets up click listeners for all game cards.
     */
    private fun setupGameCardListeners() {
        // Card: 2048
        findViewById<androidx.cardview.widget.CardView>(R.id.card_2048).setOnClickListener {
            val intent = Intent(this, GameActivity2048 ::class.java)
            startActivity(intent)
        }

        // Card: Sudoku
        findViewById<androidx.cardview.widget.CardView>(R.id.card_sudoku).setOnClickListener {
            val intent = Intent(this, SudokuActivity ::class.java)
            startActivity(intent)
        }

        // Card: Maths Quiz
        findViewById<androidx.cardview.widget.CardView>(R.id.card_maths_quiz).setOnClickListener {
            val intent = Intent(this, MathsQuiz ::class.java)
            startActivity(intent)
        }

        // Card: Sliding Puzzle
        findViewById<androidx.cardview.widget.CardView>(R.id.card_sliding_puzzle).setOnClickListener {
            val intent = Intent(this, WordSearchActivity ::class.java)
            startActivity(intent)
        }

        // Card: Number Guessing
        findViewById<androidx.cardview.widget.CardView>(R.id.card_number_guessing).setOnClickListener {
            val intent = Intent(this, GuessingGameActivity ::class.java)
            startActivity(intent)
        }

        // Card: Memory Match
        findViewById<androidx.cardview.widget.CardView>(R.id.card_memory_match).setOnClickListener {
            val intent = Intent(this, MemoryMatchActivity ::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.card_sliding_tiles).setOnClickListener {
            val intent = Intent(this, SlidingTileActivity ::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.card_block_fill).setOnClickListener {
            val intent = Intent(this, BlockFillActivity ::class.java)
            startActivity(intent)
        }
    }
}
