package com.example.puzzlenova

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

// Import the shared data classes
import com.example.puzzlenova.Cell
import com.example.puzzlenova.BlockPuzzle

class BlockFillView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Callback
    var onPuzzleComplete: (() -> Unit)? = null

    // Sizing
    private var gridSize = 5
    private var cellSize = 0f
    private var viewSize = 0

    // Game State
    private var puzzle: BlockPuzzle? = null
    private var isLoaded = false
    private val userPath = mutableListOf<Cell>()
    private var isDrawing = false

    // Paints
    private val emptySquarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333") // Dark Grey
        style = Paint.Style.FILL
    }
    private val pathSquarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555") // Lighter Grey
        style = Paint.Style.FILL
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#39FF14") // Neon Green
    }
    private val endPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700") // Gold
    }

    // --- THIS IS THE UPDATED PAINT COLOR ---
    private val userPathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#39FF14") // Neon Green
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE // <-- THIS IS THE FIX
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00ffff") // Neon Cyan
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        viewSize = size
        setMeasuredDimension(size, size)
    }

    private fun updateSizing() {
        if (viewSize == 0) return
        cellSize = viewSize.toFloat() / gridSize
        userPathPaint.strokeWidth = cellSize * 0.5f
        // Make grid lines thicker for smaller grids
        gridPaint.strokeWidth = if (gridSize <= 5) 4f else if (gridSize <= 7) 3f else 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isLoaded || puzzle == null) return

        // 1. Draw all squares (empty and path)
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val (x, y) = getCellTopLeft(c, r)
                val rect = RectF(x, y, x + cellSize, y + cellSize)

                val paint = if (puzzle!!.pathSquares.contains(Cell(c, r))) {
                    pathSquarePaint
                } else {
                    emptySquarePaint
                }
                canvas.drawRect(rect, paint)
            }
        }

        // 2. DRAW THE GRID LINES
        drawGridLines(canvas)

        // 3. Draw Start and End points
        val startCell = puzzle!!.start
        val endCell = puzzle!!.end
        canvas.drawCircle(getCellCenter(startCell).first, getCellCenter(startCell).second, cellSize * 0.3f, startPaint)

        // Draw End
        canvas.drawCircle(getCellCenter(endCell).first, getCellCenter(endCell).second, cellSize * 0.3f, endPaint)

        // 4. Draw User's Path (NOW GREEN and NO CIRCLE)
        if (userPath.isNotEmpty()) {
            val path = Path()
            val start = getCellCenter(userPath.first())
            path.moveTo(start.first, start.second)
            userPath.drop(1).forEach { cell: Cell ->
                val point = getCellCenter(cell)
                path.lineTo(point.first, point.second)
            }
            canvas.drawPath(path, userPathPaint)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isLoaded || puzzle == null) return false

        val col = (event.x / cellSize).toInt().coerceIn(0, gridSize - 1)
        val row = (event.y / cellSize).toInt().coerceIn(0, gridSize - 1)
        val currentCell = Cell(col, row)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (currentCell == puzzle!!.start) {
                    isDrawing = true
                    userPath.clear()
                    userPath.add(currentCell)
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDrawing) return true

                val lastCell = userPath.last()
                if (currentCell == lastCell) return true

                // Check for backtrack
                if (userPath.size > 1 && currentCell == userPath[userPath.size - 2]) {
                    userPath.removeAt(userPath.size - 1) // Remove last cell
                    invalidate()
                    return true
                }

                // Check for valid move
                if (isValidMove(lastCell, currentCell)) {
                    userPath.add(currentCell)
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isDrawing) {
                    checkWin()
                }
                isDrawing = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isValidMove(lastCell: Cell, newCell: Cell): Boolean {
        // 1. Is it adjacent (not diagonal)?
        val isAdjacent = abs(lastCell.col - newCell.col) + abs(lastCell.row - newCell.row) == 1
        // 2. Is it part of the valid path?
        val isPath = puzzle!!.pathSquares.contains(newCell)
        // 3. Is it already in our path?
        val isNew = !userPath.contains(newCell)

        return isAdjacent && isPath && isNew
    }

    // --- Public API ---

    fun isLoaded(): Boolean = isLoaded

    fun loadPuzzle(puzzle: BlockPuzzle) {
        this.puzzle = puzzle
        this.gridSize = puzzle.gridSize
        isLoaded = true
        updateSizing()
        reset()
    }

    fun reset() {
        userPath.clear()
        isDrawing = false
        invalidate()
    }

    private fun checkWin() {
        val lastCell = userPath.lastOrNull()
        if (lastCell == puzzle!!.end && userPath.size == puzzle!!.pathSquares.size) {
            onPuzzleComplete?.invoke()
        }
    }

    // --- Helpers ---
    private fun getCellTopLeft(col: Int, row: Int) = Pair(col * cellSize, row * cellSize)
    private fun getCellCenter(cell: Cell): Pair<Float, Float> {
        return Pair(cell.col * cellSize + cellSize / 2, cell.row * cellSize + cellSize / 2)
    }
}

