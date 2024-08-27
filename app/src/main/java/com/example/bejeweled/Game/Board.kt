package com.example.bejeweled.Game
import androidx.compose.ui.graphics.Color

enum class JewelColor {
    Red,
    Yellow,
    Green,
    Blue
}

data class Position(val x: Int, val y: Int)

data class Block(val positions: List<Position>, val color: JewelColor) {
    fun isValid() = positions.size > 3
}

class Board {
    // Use a 2D array of nullable Colors
    private val array : Array<Array<JewelColor?>> = Array(9) { row ->
        arrayOfNulls<JewelColor>(9)
    }

    // Example method to initialize some cells
    constructor() {
        initBoard()
    }

    fun setCell(row:Int,col:Int,color:JewelColor?) {
        array[row][col] = color
    }

    fun getState(): Array<Array<JewelColor?>> {
        return array
    }

    private fun detectBlocks(): Array<Block> {
        val rows = array.size
        val cols = array[0].size
        val visited = Array(rows) { BooleanArray(cols) }

        val directions = arrayOf(
            Position(-1, 0), // Up
            Position(1, 0),  // Down
            Position(0, -1), // Left
            Position(0, 1)   // Right
        )

        fun isValidPosition(p: Position): Boolean {
            return p.x in 0 until rows && p.y in 0 until cols
        }

        fun dfs(start: Position, color: JewelColor): List<Position> {
            val stack = mutableListOf(start)
            val blockPositions = mutableListOf<Position>()
            visited[start.x][start.y] = true

            while (stack.isNotEmpty()) {
                val position = stack.removeAt(stack.size - 1)
                blockPositions.add(position)

                for (dir in directions) {
                    val newPos = Position(position.x + dir.x, position.y + dir.y)
                    if (isValidPosition(newPos) && !visited[newPos.x][newPos.y] && array[newPos.x][newPos.y] == color) {
                        visited[newPos.x][newPos.y] = true
                        stack.add(newPos)
                    }
                }
            }
            return blockPositions
        }

        val blocks = mutableListOf<Block>()

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (!visited[i][j]) {
                    val color = array[i][j] ?: continue
                    val blockPositions = dfs(Position(i, j), color)
                    if (blockPositions.size > 3) {
                        blocks.add(Block(blockPositions, color))
                    }
                }
            }
        }

        return blocks.toTypedArray()
    }

    private fun initBoard() {
        while (true) {
            val found = FillBoard()
            if (!found)
                break
            val blocks = detectBlocks()
            for (block in blocks) {
                DeleteBlock(block)
            }
        }
    }

    private fun FillBoard(): Boolean {
        val colors = JewelColor.values()
        var found = false
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (array[row][col] == null) {
                    found = true
                    array[row][col] = colors.random()
                }
            }
        }
        return found
    }

    private fun DeleteBlock(block:Block) {
        for (pos in block.positions) {
            array[pos.x][pos.y] = null
        }
    }

    fun CheckMove() {
        MakeBlocksFall()
        FillBoard()
        while (true) {
            val detectedBlocks = detectBlocks()
            if (detectedBlocks.isEmpty())
                return
            for (block in detectedBlocks) {
                DeleteBlock(block)
            }
            MakeBlocksFall()
            FillBoard()
        }
    }

    private fun MakeBlocksFall() {
        val numRows = array.size
        val numCols = array[0].size

        for (col in 0 until numCols) {
            // Temporary list to store non-null elements
            val column = mutableListOf<JewelColor>()

            // Collect all non-null elements from this column
            for (row in (numRows - 1) downTo 0) {
                array[row][col]?.let { column.add(it) }
            }

            // Fill the column with null values from the top
            for (row in (numRows - 1) downTo 0) {
                array[row][col] = if ((numRows - 1 - row) < column.size) column[(numRows - 1 - row)] else null
            }
        }
    }
}