package com.example.puzzlenova

import android.graphics.Color

object TileColors {
    // Pair<BackgroundColor, TextColor>
    private val colorMap = mapOf(
        0 to Pair(Color.parseColor("#ccc0b3"), Color.BLACK), // Empty cell

        // Your requested colors:
        2 to Pair(Color.parseColor("#39FF14"), Color.BLACK),    // Neon Light Green
        4 to Pair(Color.parseColor("#008000"), Color.WHITE),    // Neon Dark Green
        8 to Pair(Color.parseColor("#FF5F1F"), Color.WHITE),    // Neon Orange

        // Extended neon theme:
        16 to Pair(Color.parseColor("#FF00FF"), Color.WHITE),   // Neon Pink/Magenta
        32 to Pair(Color.parseColor("#1F51FF"), Color.WHITE),   // Neon Blue
        64 to Pair(Color.parseColor("#FF3131"), Color.WHITE),   // Neon Red
        128 to Pair(Color.parseColor("#FFFF33"), Color.BLACK),  // Neon Yellow
        256 to Pair(Color.parseColor("#0FFFFF"), Color.BLACK),  // Neon Cyan
        512 to Pair(Color.parseColor("#BC13FE"), Color.WHITE),  // Neon Purple
        1024 to Pair(Color.parseColor("#00A170"), Color.WHITE), // Neon Teal
        2048 to Pair(Color.parseColor("#FFD700"), Color.BLACK), // Gold (for winning)

        // Fallback for > 2048
        4096 to Pair(Color.parseColor("#FF0000"), Color.WHITE)
    )

    fun getColorPair(value: Int): Pair<Int, Int> {
        // Return the specific color, or the highest value if it goes over
        return colorMap[value] ?: colorMap[4096]!!
    }
}