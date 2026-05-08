package com.example.puzzlenova

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class WordSearchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Callback
    var onWordFound: ((String) -> Unit)? = null

    // Sizing
    private var gridSize = 10
    private var cellSize = 0f
    private var viewSize = 0
    private var letterBounds = android.graphics.Rect()

    // Game State
    private var grid: Array<Array<Char>> = emptyArray()
    private var words = listOf<String>()
    private var wordLocations = mutableMapOf<String, List<Pair<Int, Int>>>()
    private val foundWords = mutableSetOf<String>()

    // Drawing
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#88aaff") // Light Neon Blue
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val gridBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#003366") // Dark Neon Blue
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00ffff") // Neon Cyan
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    // --- THIS IS THE UPDATED COLOR ---
    private val foundPathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8039FF14") // 50% Opaque Lite Green
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
    }

    // Touch
    private var touchStartCell: Pair<Int, Int>? = null
    private var touchCurrentCell: Pair<Int, Int>? = null
    private val touchPath = Path()
    private val foundPaths = mutableListOf<Path>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewSize = minOf(w, h)
        cellSize = viewSize.toFloat() / gridSize

        textPaint.textSize = cellSize * 0.7f // Larger font
        foundPathPaint.strokeWidth = cellSize * 0.5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (grid.isEmpty()) return

        val gridTotalSize = viewSize.toFloat()

        // 1. DRAW THE NEW BOX BACKGROUND
        canvas.drawRect(0f, 0f, gridTotalSize, gridTotalSize, gridBackgroundPaint)

        // 2. Draw Grid Lines
        drawGridLines(canvas)

        // 3. Draw Letters
        drawLetters(canvas)

        // 4. Draw Found Word Paths (NOW IN LITE GREEN)
        foundPaths.forEach { canvas.drawPath(it, foundPathPaint) }

        // 5. Draw Current Selection Path (will also be lite green)
        if (touchStartCell != null && touchCurrentCell != null) {
            canvas.drawPath(touchPath, foundPathPaint)
        }
    }

    private fun drawGridLines(canvas: Canvas) {
        val gridTotalSize = viewSize.toFloat()
        for (i in 0..gridSize) {
            val pos = i * cellSize
            // Vertical lines
            canvas.drawLine(pos, 0f, pos, gridTotalSize, gridPaint)
            // Horizontal lines
            canvas.drawLine(0f, pos, gridTotalSize, pos, gridPaint)
        }
    }

    private fun drawLetters(canvas: Canvas) {
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val letter = grid[r][c].toString()
                val x = (c * cellSize) + (cellSize / 2)
                val y = (r * cellSize) + (cellSize / 2) - (textPaint.ascent() + textPaint.descent()) / 2
                canvas.drawText(letter, x, y, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (grid.isEmpty()) return false
        val col = (event.x / cellSize).toInt().coerceIn(0, gridSize - 1)
        val row = (event.y / cellSize).toInt().coerceIn(0, gridSize - 1)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartCell = Pair(col, row)
                touchPath.reset()
                touchPath.moveTo(getCellCenter(col, row).first, getCellCenter(col, row).second)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchStartCell != null) {
                    val (startCol, startRow) = touchStartCell!!

                    val (snappedCol, snappedRow) = snapToAxis(startCol, startRow, col, row)

                    if (snappedCol != touchCurrentCell?.first || snappedRow != touchCurrentCell?.second) {
                        touchCurrentCell = Pair(snappedCol, snappedRow)
                        touchPath.reset()
                        touchPath.moveTo(getCellCenter(startCol, startRow).first, getCellCenter(startCol, startRow).second)
                        touchPath.lineTo(getCellCenter(snappedCol, snappedRow).first, getCellCenter(snappedCol, snappedRow).second)
                        invalidate()
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (touchStartCell != null && touchCurrentCell != null) {
                    checkWordFound()
                }
                touchStartCell = null
                touchCurrentCell = null
                touchPath.reset()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun snapToAxis(startCol: Int, startRow: Int, currentCol: Int, currentRow: Int): Pair<Int, Int> {
        val dx = currentCol - startCol
        val dy = currentRow - startRow

        return when {
            dx == 0 && dy == 0 -> Pair(startCol, startRow) // Start cell
            kotlin.math.abs(dx) > kotlin.math.abs(dy) -> Pair(currentCol, startRow) // Horizontal
            kotlin.math.abs(dy) > kotlin.math.abs(dx) -> Pair(startCol, currentRow) // Vertical
            else -> Pair(currentCol, currentRow) // Diagonal
        }
    }

    private fun checkWordFound() {
        val (startCol, startRow) = touchStartCell ?: return
        val (endCol, endRow) = touchCurrentCell ?: return

        val selectedCells = mutableListOf<Pair<Int, Int>>()
        if (startRow == endRow) { // Horizontal
            for (c in minOf(startCol, endCol)..maxOf(startCol, endCol)) {
                selectedCells.add(Pair(c, startRow))
            }
        } else if (startCol == endCol) { // Vertical
            for (r in minOf(startRow, endRow)..maxOf(startRow, endRow)) {
                selectedCells.add(Pair(startCol, r))
            }
        } else if (kotlin.math.abs(endCol - startCol) == kotlin.math.abs(endRow - startRow)) { // Diagonal
            val dCol = if (endCol > startCol) 1 else -1
            val dRow = if (endRow > startRow) 1 else -1
            for (i in 0..kotlin.math.abs(endCol - startCol)) {
                selectedCells.add(Pair(startCol + i * dCol, startRow + i * dRow))
            }
        }

        for ((word, locations) in wordLocations) {
            if (foundWords.contains(word)) continue

            val sortedLocations = locations.sortedWith(compareBy({ it.second }, { it.first }))
            val sortedSelected = selectedCells.sortedWith(compareBy({ it.second }, { it.first }))

            if (sortedLocations == sortedSelected || sortedLocations == sortedSelected.reversed()) {
                if (foundWords.add(word)) {
                    // Add a permanent path for the found word
                    val start = getCellCenter(startCol, startRow)
                    val end = getCellCenter(endCol, endRow)
                    val path = Path().apply {
                        moveTo(start.first, start.second)
                        lineTo(end.first, end.second)
                    }
                    foundPaths.add(path)
                    onWordFound?.invoke(word)
                }
                break
            }
        }
    }

    // --- Public API ---

    fun isGridGenerated(): Boolean = grid.isNotEmpty()

    fun setup(size: Int, wordList: List<String>) {
        gridSize = size
        words = wordList
        foundWords.clear()
        wordLocations.clear()
        foundPaths.clear()

        // Initialize grid with placeholders
        grid = Array(gridSize) { Array(gridSize) { '.' } }

        // Place words
        for (word in words) {
            var placed = false
            for (i in 0 until 100) { // Try 100 times
                if (placeWord(word)) {
                    placed = true
                    break
                }
            }
            if (!placed) {
                // Could not place word (this is rare but possible)
                // In a real app, you might want better collision logic
            }
        }

        // Fill remaining grid with random letters
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (grid[r][c] == '.') {
                    grid[r][c] = ('A'..'Z').random()
                }
            }
        }

        // Recalculate cell size and redraw
        onSizeChanged(width, height, width, height)
        invalidate()
    }

    private fun placeWord(word: String): Boolean {
        val direction = Random.nextInt(3) // 0: H, 1: V, 2: Diag
        val reversed = Random.nextBoolean()
        val wordToPlace = if (reversed) word.reversed() else word

        val locations = mutableListOf<Pair<Int, Int>>()

        when (direction) {
            0 -> { // Horizontal
                val row = Random.nextInt(gridSize)
                val col = Random.nextInt(gridSize - word.length + 1)
                for (i in wordToPlace.indices) {
                    if (grid[row][col + i] != '.' && grid[row][col + i] != wordToPlace[i]) return false
                }
                for (i in wordToPlace.indices) {
                    grid[row][col + i] = wordToPlace[i]
                    locations.add(Pair(col + i, row))
                }
            }
            1 -> { // Vertical
                val row = Random.nextInt(gridSize - word.length + 1)
                val col = Random.nextInt(gridSize)
                for (i in wordToPlace.indices) {
                    if (grid[row + i][col] != '.' && grid[row + i][col] != wordToPlace[i]) return false
                }
                for (i in wordToPlace.indices) {
                    grid[row + i][col] = wordToPlace[i]
                    locations.add(Pair(col, row + i))
                }
            }
            2 -> { // Diagonal (Down-Right)
                val row = Random.nextInt(gridSize - word.length + 1)
                val col = Random.nextInt(gridSize - word.length + 1)
                for (i in wordToPlace.indices) {
                    if (grid[row + i][col + i] != '.' && grid[row + i][col + i] != wordToPlace[i]) return false
                }
                for (i in wordToPlace.indices) {
                    grid[row + i][col + i] = wordToPlace[i]
                    locations.add(Pair(col + i, row + i))
                }
            }
        }
        wordLocations[word] = locations
        return true
    }

    // --- Helpers ---
    private fun getCellCenter(col: Int, row: Int): Pair<Float, Float> {
        return Pair(col * cellSize + cellSize / 2, row * cellSize + cellSize / 2)
    }
}

