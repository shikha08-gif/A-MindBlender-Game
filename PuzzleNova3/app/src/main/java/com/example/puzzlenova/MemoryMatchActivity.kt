package com.example.puzzlenova

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MemoryMatchActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var cardsRecyclerView: RecyclerView
    private lateinit var movesTextView: TextView
    private lateinit var pairsTextView: TextView
    private lateinit var newGameButton: Button
    private lateinit var resetButton: Button
    private lateinit var gameLayout: RelativeLayout

    // Game Logic
    private var moves = 0
    private var pairsFound = 0
    private var totalPairs = 0
    private var firstFlippedCardIndex: Int? = null
    private var isChecking = false // Prevent clicking while checking
    // 10 pairs for the 5x4 grid, 8 for 4x4, 6 for 4x3
    private val allEmojis = listOf("😀", "🥳", "🐶", "🚀", "🍕", "🌟", "🔥", "🤖", "🦄", "🥦", "🎉", "👑")
    private var cards: MutableList<MemoryCard> = mutableListOf()
    private lateinit var adapter: MemoryCardAdapter
    private var currentDifficulty = Difficulty.NORMAL

    // Data class to hold the state of each card
    data class MemoryCard(val emoji: String, var isFaceUp: Boolean = false, var isMatched: Boolean = false)

    // Neon Colors
    private val colorFlipped = Color.parseColor("#4C1D95") // Dark purple
    private val colorUnflipped = Color.parseColor("#003366") // Dark blue
    private val colorMatched = Color.parseColor("#222222") // Dark grey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_match)

        // Find views
        cardsRecyclerView = findViewById(R.id.cards_recycler_view)
        movesTextView = findViewById(R.id.moves_text_view)
        pairsTextView = findViewById(R.id.pairs_text_view)
        newGameButton = findViewById(R.id.new_game_button)
        resetButton = findViewById(R.id.reset_button)
        gameLayout = findViewById(R.id.game_layout)

        // Setup RecyclerView
        adapter = MemoryCardAdapter(this, cards) { position ->
            onCardClicked(position)
        }
        cardsRecyclerView.adapter = adapter

        // Setup Button Listeners
        resetButton.setOnClickListener {
            startGame(false) // false = don't create new cards, just reset/reshuffle
        }
        newGameButton.setOnClickListener {
            quitGame() // Go back to difficulty select
        }

        // Show difficulty dialog on start
        showDifficultySelectionDialog()
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
                startGame(true) // true = new game, create new cards
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                if (gameLayout.visibility == View.GONE) {
                    finish() // Exit app if game hasn't started
                } else {
                    dialog.dismiss() // Just close dialog if game is running
                }
            }
            .show()
    }

    private fun startGame(isNewGame: Boolean) {
        // Reset game state
        moves = 0
        pairsFound = 0
        firstFlippedCardIndex = null
        isChecking = false
        gameLayout.visibility = View.VISIBLE

        val numColumns = 4 // All difficulties use 4 columns
        var numCards: Int

        // Set card count and total pairs based on difficulty
        when (currentDifficulty) {
            Difficulty.EASY -> { // 4x3 grid
                numCards = 12
                totalPairs = 6
            }
            Difficulty.NORMAL -> { // 4x4 grid
                numCards = 16
                totalPairs = 8
            }
            Difficulty.HARD -> { // 5x4 grid
                numCards = 20
                totalPairs = 10
            }
        }

        // Set the layout manager for the grid
        cardsRecyclerView.layoutManager = GridLayoutManager(this, numColumns)

        // Update UI
        movesTextView.text = "Moves: 0"
        pairsTextView.text = "Pairs: 0 / $totalPairs"

        if (isNewGame) {
            // New Game: Create a new shuffled list
            val emojisForGame = allEmojis.take(totalPairs)
            val allEmojis = (emojisForGame + emojisForGame).shuffled()
            cards = allEmojis.map { MemoryCard(it) }.toMutableList()
        } else {
            // Reset: Just shuffle existing cards and flip them down
            cards.shuffle()
            cards.forEach {
                it.isFaceUp = false
                it.isMatched = false
            }
        }

        // Update the adapter with new cards
        adapter.updateCards(cards)
    }

    private fun quitGame() {
        gameLayout.visibility = View.GONE
        showDifficultySelectionDialog()
    }

    private fun onCardClicked(position: Int) {
        val card = cards[position]

        // Ignore click if card is matched, already face up, or checking
        if (card.isMatched || card.isFaceUp || isChecking) {
            return
        }

        // Flip the card
        card.isFaceUp = true
        adapter.notifyItemChanged(position)

        // Check if this is the first or second card flipped
        val clickedIndex = firstFlippedCardIndex
        if (clickedIndex == null) {
            // This is the first card
            firstFlippedCardIndex = position
        } else {
            // This is the second card
            moves++
            movesTextView.text = "Moves: $moves"
            isChecking = true // Lock the board
            checkForMatch(clickedIndex, position)
        }
    }

    private fun checkForMatch(index1: Int, index2: Int) {
        val card1 = cards[index1]
        val card2 = cards[index2]

        if (card1.emoji == card2.emoji) {
            // It's a match!
            card1.isMatched = true
            card2.isMatched = true
            pairsFound++
            pairsTextView.text = "Pairs: $pairsFound / $totalPairs"
            isChecking = false // Unlock board
            checkGameWon()
        } else {
            // Not a match, flip back after a delay
            Handler(Looper.getMainLooper()).postDelayed({
                card1.isFaceUp = false
                card2.isFaceUp = false
                adapter.notifyItemChanged(index1)
                adapter.notifyItemChanged(index2)
                isChecking = false // Unlock board
            }, 1000) // 1 second delay
        }

        // Reset the first card index
        firstFlippedCardIndex = null
    }

    private fun checkGameWon() {
        if (pairsFound == totalPairs) {
            // All pairs found
            // You could show a "You Win!" message here
            // The "New Game" button is already visible, so the user can restart
        }
    }

    // --- Adapter for the RecyclerView ---
    inner class MemoryCardAdapter(
        private val context: Context,
        private var cards: MutableList<MemoryCard>,
        private val onCardClick: (Int) -> Unit
    ) : RecyclerView.Adapter<MemoryCardAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cardTextView: TextView = itemView.findViewById(R.id.card_text_view)
            val cardView: CardView = itemView as CardView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = cards.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val card = cards[position]

            // Show emoji or card back
            if (card.isFaceUp || card.isMatched) {
                holder.cardTextView.text = card.emoji
                holder.cardView.setCardBackgroundColor(colorFlipped)
            } else {
                holder.cardTextView.text = "👀"
                holder.cardView.setCardBackgroundColor(colorUnflipped)
            }

            // Set background color for matched cards
            if (card.isMatched) {
                holder.cardView.setCardBackgroundColor(colorMatched)
                holder.cardTextView.alpha = 0.5f // Fade out
            } else {
                holder.cardTextView.alpha = 1.0f
            }

            // Set click listener
            holder.itemView.setOnClickListener {
                onCardClick(position)
            }
        }

        fun updateCards(newCards: MutableList<MemoryCard>) {
            cards = newCards
            notifyDataSetChanged() // Full refresh for a new game
        }
    }
}

