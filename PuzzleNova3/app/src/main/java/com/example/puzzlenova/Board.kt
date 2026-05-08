package com.example.puzzlenova

// Make sure to import the models from our new 2048 file
import com.example.puzzlenova.Difficulty
import com.example.puzzlenova.Direction
import kotlin.random.Random

class Board(val difficulty: Difficulty) {

    val size = difficulty.size
    val grid: Array<Array<Tile?>> = Array(size) { Array(size) { null } }

    var score = 0
        private set

    private var hasWon = false
    private var onScoreUpdate: ((Int) -> Unit)? = null
    private var onGameOver: ((message: String, score: Int) -> Unit)? = null

    init {
        addRandomTile()
        addRandomTile()
    }

    fun setOnScoreUpdateListener(listener: (Int) -> Unit) {
        onScoreUpdate = listener
    }

    fun setOnGameOverListener(listener: (message: String, score: Int) -> Unit) {
        onGameOver = listener
    }

    // Resets the board for a new game
    fun reset() {
        for (r in 0 until size) {
            for (c in 0 until size) {
                grid[r][c] = null
            }
        }
        score = 0
        hasWon = false
        onScoreUpdate?.invoke(score)
        addRandomTile()
        addRandomTile()
    }

    // Main function called by GameView after a swipe
    fun move(direction: Direction) {
        // Clear any animation flags from the previous move
        grid.forEach { row -> row.forEach { it?.clearAnimationFlags() } }

        val tempGrid = Array(size) { r -> grid[r].copyOf() } // Snapshot to check if anything moved

        when (direction) {
            Direction.LEFT -> moveLeft()
            Direction.RIGHT -> moveRight()
            Direction.UP -> moveUp()
            Direction.DOWN -> moveDown()
        }

        val moved = !tempGrid.contentDeepEquals(grid)

        if (moved) {
            addRandomTile()
        }

        // Check for game end conditions
        if (!hasWon && grid.any { row -> row.any { it?.value == 2048 } }) {
            hasWon = true
            onGameOver?.invoke("You Win!", score)
        } else if (!canMove()) {
            onGameOver?.invoke("Game Over", score)
        }
    }

    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (grid[r][c] == null) {
                    emptyCells.add(r to c)
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells.random()
            val value = if (Random.nextFloat() < 0.9f) 2 else 4 // 90% chance of 2, 10% of 4
            grid[r][c] = Tile.new(value)
        }
    }

    // --- Movement Logic ---

    //
    // --- THIS IS THE CORRECTED FUNCTION ---
    //
    private fun slideLine(line: List<Tile?>): List<Tile?> {
        val nonNull = line.filterNotNull()
        val result = mutableListOf<Tile?>()
        var i = 0
        while (i < nonNull.size) {
            val current = nonNull[i]
            if (i + 1 < nonNull.size && nonNull[i + 1].value == current.value) {
                // *** MERGE HAPPENS HERE ***
                val newValue = current.value * 2
                val newTile = Tile.new(newValue, mergedFrom = Pair(current, nonNull[i + 1]))
                result.add(newTile)

                // *** SCORE IS ADDED HERE ***
                score += newValue
                onScoreUpdate?.invoke(score)

                i += 2 // Skip both tiles that were merged
            } else {
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // *** THIS LINE FIXES THE BUG ***
                // It handles tiles that do not merge.
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                result.add(current)
                i += 1
            }
        }
        // Pad the rest of the line with nulls
        while (result.size < size) {
            result.add(null)
        }
        return result
    }

    private fun moveLeft() {
        for (r in 0 until size) {
            val row = grid[r].toList()
            val newRow = slideLine(row)
            for (c in 0 until size) {
                grid[r][c] = newRow[c]
            }
        }
    }

    private fun moveRight() {
        for (r in 0 until size) {
            val row = grid[r].toList().reversed()
            val newRow = slideLine(row).reversed()
            for (c in 0 until size) {
                grid[r][c] = newRow[c]
            }
        }
    }

    private fun moveUp() {
        for (c in 0 until size) {
            val col = (0 until size).map { r -> grid[r][c] }
            val newCol = slideLine(col)
            for (r in 0 until size) {
                grid[r][c] = newCol[r]
            }
        }
    }

    private fun moveDown() {
        for (c in 0 until size) {
            val col = (0 until size).map { r -> grid[r][c] }.reversed()
            val newCol = slideLine(col).reversed()
            for (r in 0 until size) {
                grid[r][c] = newCol[r]
            }
        }
    }

    // --- Game Over Check ---
    private fun canMove(): Boolean {
        // 1. Check for any empty cells
        if (grid.any { row -> row.any { it == null } }) {
            return true
        }

        // 2. Check for possible merges (horizontal and vertical)
        for (r in 0 until size) {
            for (c in 0 until size) {
                val value = grid[r][c]?.value ?: continue
                // Check right
                if (c + 1 < size && grid[r][c + 1]?.value == value) return true
                // Check down
                if (r + 1 < size && grid[r + 1][c]?.value == value) return true
            }
        }
        return false
    }
}