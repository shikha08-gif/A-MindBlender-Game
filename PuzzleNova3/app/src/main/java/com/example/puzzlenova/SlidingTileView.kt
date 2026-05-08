package com.example.puzzlenova

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class SlidingTileView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Callbacks
    var onTileMoved: (() -> Unit)? = null
    var onGameWon: (() -> Unit)? = null

    // Sizing
    private var gridSize = 4
    private var cellSize = 0f
    private var viewSize = 0

    // Game State
    private var tiles = IntArray(gridSize * gridSize)
    private var emptyRow = 0
    private var emptyCol = 0
    private var isLoaded = false

    // Paints
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00ffff") // Neon Cyan
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#003366") // Dark Neon Blue
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00ffff") // Neon Cyan
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val textRect = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        viewSize = size
        setMeasuredDimension(size, size)
    }

    private fun updateSizing() {
        cellSize = viewSize.toFloat() / gridSize
        textPaint.textSize = cellSize * 0.4f
        gridPaint.strokeWidth = if (gridSize == 3) 6f else if (gridSize == 4) 4f else 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isLoaded) return

        canvas.drawRect(0f, 0f, viewSize.toFloat(), viewSize.toFloat(), tilePaint)

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val tileValue = tiles[index]

                if (tileValue == 0) continue // Skip drawing the empty tile

                val x = col * cellSize
                val y = row * cellSize

                // Draw tile background
                textRect.set(x, y, x + cellSize, y + cellSize)
                textRect.inset(gridPaint.strokeWidth, gridPaint.strokeWidth) // Inset for grid lines
                canvas.drawRect(textRect, tilePaint)

                // Draw tile border
                canvas.drawRect(textRect, gridPaint)

                // Draw number
                val textX = x + cellSize / 2
                val textY = y + cellSize / 2 - (textPaint.ascent() + textPaint.descent()) / 2
                canvas.drawText(tileValue.toString(), textX, textY, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isLoaded || event.action != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event)
        }

        val col = (event.x / cellSize).toInt().coerceIn(0, gridSize - 1)
        val row = (event.y / cellSize).toInt().coerceIn(0, gridSize - 1)

        // Check if the tap is adjacent to the empty slot
        val dx = kotlin.math.abs(col - emptyCol)
        val dy = kotlin.math.abs(row - emptyRow)

        if (dx + dy == 1) { // Is adjacent (but not diagonal)
            // Swap tiles
            val tappedIndex = row * gridSize + col
            val emptyIndex = emptyRow * gridSize + emptyCol

            tiles[emptyIndex] = tiles[tappedIndex]
            tiles[tappedIndex] = 0

            // Update empty slot position
            emptyRow = row
            emptyCol = col

            onTileMoved?.invoke()
            invalidate()

            if (isSolved()) {
                onGameWon?.invoke()
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    // --- Public API ---

    fun isLoaded(): Boolean = isLoaded

    fun loadGame(size: Int) {
        gridSize = size
        tiles = IntArray(gridSize * gridSize)
        updateSizing()
        resetBoardState()
        shuffle()
        isLoaded = true
        invalidate()
    }

    fun shuffle() {
        resetBoardState()
        // Perform 100 * (gridSize*gridSize) random moves to shuffle
        for (i in 0 until 100 * gridSize * gridSize) {
            val validMoves = mutableListOf<Pair<Int, Int>>() // (col, row)
            if (emptyRow > 0) validMoves.add(Pair(emptyCol, emptyRow - 1)) // Up
            if (emptyRow < gridSize - 1) validMoves.add(Pair(emptyCol, emptyRow + 1)) // Down
            if (emptyCol > 0) validMoves.add(Pair(emptyCol - 1, emptyRow)) // Left
            if (emptyCol < gridSize - 1) validMoves.add(Pair(emptyCol + 1, emptyRow)) // Right

            val (randomCol, randomRow) = validMoves.random()

            // Swap
            val tappedIndex = randomRow * gridSize + randomCol
            val emptyIndex = emptyRow * gridSize + emptyCol
            tiles[emptyIndex] = tiles[tappedIndex]
            tiles[tappedIndex] = 0
            emptyRow = randomRow
            emptyCol = randomCol
        }

        // Ensure it's not accidentally solved
        if (isSolved()) {
            shuffle()
        }

        invalidate()
    }

    // --- Internal Logic ---

    private fun resetBoardState() {
        // Create a solved board
        for (i in 0 until (gridSize * gridSize - 1)) {
            tiles[i] = i + 1
        }
        tiles[tiles.size - 1] = 0 // Last tile is the empty one
        emptyRow = gridSize - 1
        emptyCol = gridSize - 1
    }

    private fun isSolved(): Boolean {
        for (i in 0 until (gridSize * gridSize - 1)) {
            if (tiles[i] != i + 1) return false
        }
        return true // All tiles are in order
    }
}
