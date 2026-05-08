package com.example.puzzlenova

// (col, row)
// This public data class can now be shared by all your files.
data class Cell(val col: Int, val row: Int)

// Data class to define a single puzzle
// I've moved this here from BlockFillActivity.kt
data class BlockPuzzle(
    val gridSize: Int,
    val start: Cell,
    val end: Cell,
    val pathSquares: Set<Cell> // All valid squares for the path
)

