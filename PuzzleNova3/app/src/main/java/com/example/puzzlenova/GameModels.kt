package com.example.puzzlenova

// Represents the four swipe directions
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

// Represents game difficulty by changing the board size
enum class Difficulty(val size: Int) {
    EASY(5),    // 5x5 Grid
    NORMAL(4), // 4x4 Grid
    HARD(3)     // 3x3 Grid
}