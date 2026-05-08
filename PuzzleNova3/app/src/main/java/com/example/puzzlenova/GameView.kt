package com.example.puzzlenova

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.min

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var board: Board? = null
    private var boardSize = 4
    private var cellSize = 0f
    private var padding = 16f

    // Paints
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#bbada0") // Game board background
    }
    private val cellBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ccc0b3") // Empty cell background
    }
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.BLACK
        isFakeBoldText = true
    }

    // Gesture detector for swipes
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false

            val dx = e2.x - e1.x
            val dy = e2.y - e1.y

            if (abs(dx) > abs(dy)) {
                // Horizontal swipe
                if (abs(dx) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (dx > 0) {
                        board?.move(Direction.RIGHT)
                    } else {
                        board?.move(Direction.LEFT)
                    }
                    invalidate() // Redraw the board
                    return true
                }
            } else {
                // Vertical swipe
                if (abs(dy) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (dy > 0) {
                        board?.move(Direction.DOWN)
                    } else {
                        board?.move(Direction.UP)
                    }
                    invalidate() // Redraw the board
                    return true
                }
            }
            return false
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || true
    }

    fun setBoard(board: Board) {
        this.board = board
        this.boardSize = board.size
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val viewSize = min(w, h)
        cellSize = (viewSize - padding * (boardSize + 1)) / boardSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (board == null) return

        // 1. Draw the main board background
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 15f, 15f, bgPaint)

        // 2. Draw all cells (empty backgrounds + tiles)
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val left = padding + c * (cellSize + padding)
                val top = padding + r * (cellSize + padding)
                val rect = RectF(left, top, left + cellSize, top + cellSize)

                // Draw the empty cell background first
                canvas.drawRoundRect(rect, 15f, 15f, cellBgPaint)

                // Check if there is a tile in this cell
                val tile = board?.grid?.get(r)?.get(c)
                if (tile != null) {
                    drawTile(canvas, tile, rect)
                }
            }
        }
    }

    private fun drawTile(canvas: Canvas, tile: Tile, rect: RectF) {
        // Get the custom colors
        val (bgColor, textColor) = TileColors.getColorPair(tile.value)
        tilePaint.color = bgColor
        textPaint.color = textColor

        // Set text size based on number value (smaller text for bigger numbers)
        textPaint.textSize = when {
            tile.value < 100 -> cellSize * 0.4f
            tile.value < 1000 -> cellSize * 0.3f
            else -> cellSize * 0.25f
        }

        // Draw the tile background
        canvas.drawRoundRect(rect, 15f, 15f, tilePaint)

        // Draw the text
        val textX = rect.centerX()
        val textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(tile.value.toString(), textX, textY, textPaint)
    }
}