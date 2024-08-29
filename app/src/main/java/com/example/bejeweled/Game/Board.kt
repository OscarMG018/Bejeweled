package com.example.bejeweled.Game
import androidx.compose.ui.graphics.Color
import kotlin.math.*

enum class JewelColor {
    Red,
    Yellow,
    Green,
    Blue,
    FireryRed,
    FireryYellow,
    FireryGreen,
    FireryBlue,
    FlashRed,
    FlashYellow,
    FlashGreen,
    FlashBlue,
    ColorCube;

    fun getGroup(): ColorGroup {
        return when (this) {
            Red, FireryRed, FlashRed -> ColorGroup.Red
            Yellow, FireryYellow, FlashYellow -> ColorGroup.Yellow
            Green, FireryGreen, FlashGreen -> ColorGroup.Green
            Blue, FireryBlue, FlashBlue -> ColorGroup.Blue
            ColorCube -> ColorGroup.ColorCube
        }
    }

    fun getFireryVersion(): JewelColor {
        return when(this) {
            Red, FireryRed, FlashRed -> FireryRed
            Yellow, FireryYellow, FlashYellow -> FireryYellow
            Green, FireryGreen, FlashGreen -> FireryGreen
            Blue, FireryBlue, FlashBlue -> FireryBlue
            ColorCube -> ColorCube
        }
    }

    fun getFlashVersion(): JewelColor {
        return when(this) {
            Red, FireryRed, FlashRed -> FlashRed
            Yellow, FireryYellow, FlashYellow -> FlashYellow
            Green, FireryGreen, FlashGreen -> FlashGreen
            Blue, FireryBlue, FlashBlue -> FlashBlue
            ColorCube -> ColorCube
        }
    }

    fun isFirery(): Boolean {
        return this in listOf(FireryRed, FireryYellow, FireryGreen, FireryBlue)
    }

    fun isFlash(): Boolean {
        return this in listOf(FlashRed, FlashYellow, FlashGreen, FlashBlue)
    }
}

enum class ColorGroup {
    Red, Yellow, Green, Blue, ColorCube
}

data class Position(val x: Int, val y: Int) {
    fun isOrthogonallyAdjacent(other: Position): Boolean {
        return (this.x == other.x && (this.y == other.y + 1 || this.y == other.y - 1)) ||
                (this.y == other.y && (this.x == other.x + 1 || this.x == other.x - 1))
    }
}

data class Block(val positions: List<Position>,val color:JewelColor) {
    fun isValid() = positions.size > 2
    fun center(): Position? {
        if (positions.isEmpty()) return null

        val avgX = positions.map { it.x }.average()
        val avgY = positions.map { it.y }.average()

        // Round the average coordinates to the nearest integer
        val centerX = avgX.roundToInt()
        val centerY = avgY.roundToInt()

        return Position(centerX, centerY)
    }
}

class Board {
    // Use a 2D array of nullable Colors
    private val array : Array<Array<JewelColor?>> = Array(9) { row ->
        arrayOfNulls<JewelColor>(9)
    }
    var points:Int = 0
    var moves:Int = 0
    var comboMultiplier:Float = 1f
    var Lastpos1:Position = Position(-1,-1)
    var Lastpos2:Position = Position(-1,-1)

    // Example method to initialize some cells
    constructor() {
        initBoard()
    }

    fun Swap(pos1:Position,pos2:Position) {
        Lastpos1 = pos1
        Lastpos2 = pos2
        val aux:JewelColor? = array[pos1.x][pos1.y]
        array[pos1.x][pos1.y] = array[pos2.x][pos2.y]
        array[pos2.x][pos2.y] = aux
    }

    fun getCell(row:Int,col:Int) : JewelColor? {
        return array[row][col]
    }

    fun setCell(row:Int,col:Int,color:JewelColor?) {
        array[row][col] = color
    }

    fun getState(): Array<Array<JewelColor?>> {
        return array
    }

    fun detectBlocks(): List<Block> {
        val matchingBlocks = mutableListOf<Block>()

        // Check horizontal lines
        for (row in 0 until 9) {
            var startCol = 0
            while (startCol < 9) {
                val color = array[row][startCol]
                if (color == null) {
                    startCol++
                    continue
                }

                var endCol = startCol
                while (endCol < 9 && AreSameColor(array[row][endCol]!!, color)) {
                    endCol++
                }

                if (endCol - startCol >= 3) {
                    val positions = mutableListOf<Position>()
                    for (col in startCol until endCol) {
                        positions.add(Position(row, col))
                    }
                    matchingBlocks.add(Block(positions,color))
                }

                startCol = endCol
            }
        }

        // Check vertical lines
        for (col in 0 until 9) {
            var startRow = 0
            while (startRow < 9) {
                val color = array[startRow][col]
                if (color == null) {
                    startRow++
                    continue
                }

                var endRow = startRow
                while (endRow < 9 && AreSameColor(array[endRow][col]!!, color)) {
                    endRow++
                }

                if (endRow - startRow >= 3) {
                    val positions = mutableListOf<Position>()
                    for (row in startRow until endRow) {
                        positions.add(Position(row, col))
                    }
                    matchingBlocks.add(Block(positions,color))
                }

                startRow = endRow
            }
        }

        return matchingBlocks.filter { it.isValid() }
    }

    fun AreSameColor(color1:JewelColor,color2:JewelColor) : Boolean {
        return color1.getGroup() == color2.getGroup()
    }

    fun initBoard() {
        Lastpos1 = Position(-1,-1)
        Lastpos2 = Position(-1,-1)
        points = 0
        moves = 0
        comboMultiplier = 1f
        EamtyBoard()
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

    fun FillBoard(): Boolean {
        val colors: Array<JewelColor> = arrayOf(JewelColor.Red, JewelColor.Blue, JewelColor.Green, JewelColor.Yellow)
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

    fun EamtyBoard() {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                array[row][col] = null
            }
        }
    }

    fun DeleteBlock(block:Block) {
        for (positon in block.positions) {
            array[positon.x][positon.y] = null
        }
    }

    fun BlockPoints(block: Block) {
        if (block.positions.size == 3) {
            points += (60*comboMultiplier).roundToInt()
        }
        else if (block.positions.size == 4) {
            points += (120*comboMultiplier).roundToInt()
        }
        else {
            points += (200*(block.positions.size-4)*comboMultiplier).roundToInt()
        }
    }

    fun MakeBlocksFall() {
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

    fun activateSpecialGem(position: Position, multiplier: Float = 1f, color: ColorGroup = ColorGroup.Red, destroyedPositions: MutableList<Position> = mutableListOf<Position>()) : List<Position> {
        destroyedPositions.add(position)
        when {
            array[position.x][position.y]!!.isFirery() -> ActivateFireryGem(position,multiplier * 1.5f,destroyedPositions)
            array[position.x][position.y]!!.isFlash() -> ActivateFlashGem(position,multiplier * 1.5f,destroyedPositions)
            array[position.x][position.y]!!.getGroup() == ColorGroup.ColorCube ->
                ActivateColorCube(color,multiplier * 1.5f,destroyedPositions)
        }
        return destroyedPositions
    }

    fun ActivateFlashGem(position: Position, multiplier: Float = 1f,destroyedPositions: MutableList<Position> = mutableListOf<Position>()): List<Position> {
        val color = array[position.x][position.y]?.getGroup() ?: return destroyedPositions

        // Destroy row
        for (y in 0 until 9) {
            for (dx in -1..1) {
                if (position.x+dx in array.indices && y in array[0].indices) {
                    val pos = Position(position.x + dx, y)
                    points += (1000 * comboMultiplier * multiplier).roundToInt()
                    if (!destroyedPositions.contains(pos))
                        activateSpecialGem(pos, multiplier, color, destroyedPositions)
                }
            }
        }

        // Destroy column
        for (x in 0 until 9) {
            for (dy in -1..1) {
                if (x in array.indices && position.y + dy in array[0].indices) {
                    val pos = Position(x,  position.y + dy)
                    points += (1000 * comboMultiplier * multiplier).roundToInt()//52650,20150
                    if (!destroyedPositions.contains(pos))
                        activateSpecialGem(pos, multiplier, color, destroyedPositions)
                }
            }
        }

        points += (1000 * comboMultiplier * multiplier).roundToInt()
        return destroyedPositions
    }

    fun ActivateFireryGem(center: Position, multiplier: Float = 1f,destroyedPositions: MutableList<Position> = mutableListOf<Position>()): List<Position> {
        points += (multiplier * 500 * comboMultiplier).roundToInt()
        val explosionColor = array[center.x][center.y]
        for (dx in -1..1) {
            for (dy in -1..1) {
                val x = center.x + dx
                val y = center.y + dy
                if (x in array.indices && y in array[0].indices) {
                    val pos = Position(x, y)
                    points += (25 * comboMultiplier * multiplier).roundToInt()
                    if (!destroyedPositions.contains(pos)) {
                        activateSpecialGem(
                            pos,
                            multiplier,
                            explosionColor!!.getGroup(),
                            destroyedPositions
                        )
                    }
                }
            }
        }
        return destroyedPositions
    }

    fun ActivateColorCube(color: ColorGroup, multiplier: Float = 1f,destroyedPositions: MutableList<Position> = mutableListOf<Position>()): List<Position> {
        for (x in array.indices) {
            for (y in array[x].indices) {
                if (array[x][y]?.getGroup() == color || color == ColorGroup.ColorCube) {
                    val pos = Position(x, y)
                    points += (1000 * comboMultiplier * multiplier).roundToInt()//52650,20150
                    if (!destroyedPositions.contains(pos))
                        activateSpecialGem(pos, multiplier,color,destroyedPositions)
                }
            }
        }
        return destroyedPositions
    }

    fun ActivateBlock(blocks: List<Block>) {
        val destroyedPositions = mutableListOf<Position>()
        val special = mutableMapOf<Position,JewelColor>()
        for (block in blocks) {
            BlockPoints(block)
            when {
                block.positions.size == 4 -> {
                    val specialPosition = listOf(Lastpos1, Lastpos2).find { block.positions.contains(it) } ?: block.center()!!
                    val specialColor = block.color.getFireryVersion()
                    special[specialPosition] = specialColor
                    for (pos in block.positions) {
                        destroyedPositions.addAll(activateSpecialGem(pos))
                    }
                }

                block.positions.size == 5 -> {
                    val specialPosition = listOf(Lastpos1, Lastpos2).find { block.positions.contains(it) } ?: block.center()!!
                    val specialColor = JewelColor.ColorCube
                    special[specialPosition] = specialColor
                    for (pos in block.positions) {
                        destroyedPositions.addAll(activateSpecialGem(pos))
                    }
                }

                block.positions.size > 5 -> {
                    val specialPosition = listOf(Lastpos1, Lastpos2).find { block.positions.contains(it) } ?: block.center()!!
                    val specialColor = block.color.getFlashVersion()
                    special[specialPosition] = specialColor
                    for (pos in block.positions) {
                        destroyedPositions.addAll(activateSpecialGem(pos))
                    }
                }
                else -> {
                    var flash = true
                    for (pos in block.positions) {
                        if (!getCell(pos.x,pos.y)!!.isFirery())
                            flash = false
                        destroyedPositions.addAll(activateSpecialGem(pos))
                    }
                    if (flash) {
                        val specialPosition = listOf(Lastpos1, Lastpos2).find { block.positions.contains(it) } ?: block.center()!!
                        val specialColor = block.color.getFlashVersion()
                        special[specialPosition] = specialColor
                    }

                }
            }
            comboMultiplier += 0.1f
        }
        for (pos in destroyedPositions) {
            setCell(pos.x,pos.y,null)
        }
        for ((pos,color) in special) {
            setCell(pos.x,pos.y,color)
        }
    }
}