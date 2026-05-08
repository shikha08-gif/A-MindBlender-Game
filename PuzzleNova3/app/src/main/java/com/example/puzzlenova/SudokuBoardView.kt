package com.example.puzzlenova

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SudokuBoardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Callback
    var onGameWon: (() -> Unit)? = null

    // Sizing
    private var cellSize = 0f
    private var viewSize = 0

    // Game State
    private var fixedGrid = IntArray(81) // The puzzle clues
    private var userGrid = IntArray(81) // The user's numbers
    private var solution = IntArray(81) // The full solution
    private var selectedRow = -1
    private var selectedCol = -1
    private var isLoaded = false
    private val errorCells = mutableSetOf<Int>() // Index 0-80

    // Paints
    private val thickLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00ffff") // Neon Cyan
        strokeWidth = 6f
    }
    private val thinLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4C1D95") // Dark purple
        strokeWidth = 2f
    }
    private val fixedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF") // White
        textAlign = Paint.Align.CENTER
        textSize = 64f // Will be resized
    }
    private val userTextPaint = Paint(fixedTextPaint).apply {
        color = Color.parseColor("#88aaff") // Light Neon Blue
    }
    private val selectedCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4000ffff") // Transparent Neon Blue
        style = Paint.Style.FILL
    }
    private val errorCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40ff0000") // Transparent Red
        style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        viewSize = size
        cellSize = size / 9f
        // Adjust text size based on cell size
        fixedTextPaint.textSize = cellSize * 0.6f
        userTextPaint.textSize = cellSize * 0.6f
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isLoaded) return

        // 1. Draw Cell Highlights (Selected and Errors)
        drawHighlights(canvas)

        // 2. Draw Grid Lines
        drawGridLines(canvas)

        // 3. Draw Numbers
        drawNumbers(canvas)
    }

    private fun drawHighlights(canvas: Canvas) {
        // Highlight selected cell
        if (selectedRow != -1 && selectedCol != -1) {
            val (x, y) = getCellTopLeft(selectedCol, selectedRow)
            canvas.drawRect(x, y, x + cellSize, y + cellSize, selectedCellPaint)
        }

        // Highlight error cells
        errorCells.forEach { index ->
            val (col, row) = indexToPos(index)
            val (x, y) = getCellTopLeft(col, row)
            canvas.drawRect(x, y, x + cellSize, y + cellSize, errorCellPaint)
        }
    }

    private fun drawGridLines(canvas: Canvas) {
        for (i in 0..9) {
            val paint = if (i % 3 == 0) thickLinePaint else thinLinePaint
            // Vertical lines
            canvas.drawLine(i * cellSize, 0f, i * cellSize, viewSize.toFloat(), paint)
            // Horizontal lines
            canvas.drawLine(0f, i * cellSize, viewSize.toFloat(), i * cellSize, paint)
        }
    }

    private fun drawNumbers(canvas: Canvas) {
        val textOffset = (cellSize / 2) - (fixedTextPaint.ascent() + fixedTextPaint.descent()) / 2
        for (i in 0 until 81) {
            val (col, row) = indexToPos(i)
            val x = (col * cellSize) + (cellSize / 2)
            val y = (row * cellSize) + textOffset

            if (fixedGrid[i] != 0) {
                // Draw fixed clue numbers
                canvas.drawText(fixedGrid[i].toString(), x, y, fixedTextPaint)
            } else if (userGrid[i] != 0) {
                // Draw user-entered numbers
                canvas.drawText(userGrid[i].toString(), x, y, userTextPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / cellSize).toInt()
            val row = (event.y / cellSize).toInt()
            if (col in 0..8 && row in 0..8) {
                selectedCol = col
                selectedRow = row
                invalidate() // Redraw to show selection
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // --- Public API ---

    fun isGameLoaded(): Boolean = isLoaded

    fun loadPuzzle(fullSolution: IntArray, cluesToShow: Int) {
        solution = fullSolution
        fixedGrid = IntArray(81)
        userGrid = IntArray(81)
        selectedCol = -1
        selectedRow = -1

        // Randomly select cells to show as clues
        val cells = (0..80).shuffled()
        for (i in 0 until cluesToShow) {
            val index = cells[i]
            fixedGrid[index] = solution[index]
        }
        isLoaded = true
        checkAllErrors()
        invalidate()
    }

    fun resetGame() {
        // Clears user numbers, keeps fixed clues
        userGrid = IntArray(81)
        checkAllErrors()
        invalidate()
    }

    fun setNumber(number: Int) {
        // Can only place numbers in empty cells (not fixed clues)
        if (selectedRow != -1 && selectedCol != -1) {
            val index = posToIndex(selectedCol, selectedRow)
            if (fixedGrid[index] == 0) {
                userGrid[index] = number
                checkAllErrors()
                invalidate()
                checkWin()
            }
        }
    }

    // --- Win/Error Checking ---

    private fun checkAllErrors() {
        errorCells.clear()
        for (i in 0 until 81) {
            val num = if (fixedGrid[i] != 0) fixedGrid[i] else userGrid[i]
            if (num != 0) {
                if (hasConflict(i, num)) {
                    errorCells.add(i)
                }
            }
        }
    }

    private fun hasConflict(index: Int, num: Int): Boolean {
        val (col, row) = indexToPos(index)

        // Check Row
        for (c in 0..8) {
            val i = posToIndex(c, row)
            if (i != index && getNumber(i) == num) return true
        }
        // Check Column
        for (r in 0..8) {
            val i = posToIndex(col, r)
            if (i != index && getNumber(i) == num) return true
        }
        // Check 3x3 Box
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                val i = posToIndex(c, r)
                if (i != index && getNumber(i) == num) return true
            }
        }
        return false
    }

    private fun checkWin() {
        if (errorCells.isNotEmpty()) return // Can't win with errors
        for (i in 0 until 81) {
            if (getNumber(i) == 0) return // Can't win with empty cells
        }
        // If no errors and no empty cells, you win!
        onGameWon?.invoke()
    }

    // Helper functions
    private fun getNumber(index: Int) = if (fixedGrid[index] != 0) fixedGrid[index] else userGrid[index]
    private fun posToIndex(col: Int, row: Int) = (row * 9) + col
    private fun indexToPos(index: Int) = Pair(index % 9, index / 9) // (col, row)
    private fun getCellTopLeft(col: Int, row: Int) = Pair(col * cellSize, row * cellSize)
}
