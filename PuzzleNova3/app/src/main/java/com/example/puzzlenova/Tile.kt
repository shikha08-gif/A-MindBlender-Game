package com.example.puzzlenova

data class Tile(
    val value: Int,
    val id: Int,
    var isNew: Boolean = true,
    var mergedFrom: Pair<Tile, Tile>? = null
) {
    companion object {
        private var nextId = 0
        fun new(value: Int, mergedFrom: Pair<Tile, Tile>? = null) =
            Tile(value, nextId++, false, mergedFrom)
    }

    fun clearAnimationFlags() {
        isNew = false
        mergedFrom = null
    }
}