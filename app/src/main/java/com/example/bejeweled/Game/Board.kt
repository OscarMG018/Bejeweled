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
    ColorCube;

    fun getGroup(): ColorGroup {
        return when (this) {
            Red, FireryRed -> ColorGroup.Red
            Yellow, FireryYellow -> ColorGroup.Yellow
            Green, FireryGreen -> ColorGroup.Green
            Blue, FireryBlue -> ColorGroup.Blue
            ColorCube -> ColorGroup.ColorCube
        }
    }

    fun getFireryVersion(): JewelColor {
        return when(this) {
            Red -> FireryRed
            Yellow -> FireryYellow
            Green -> FireryGreen
            Blue -> FireryBlue
            FireryRed -> FireryRed
            FireryYellow -> FireryYellow
            FireryGreen -> FireryGreen
            FireryBlue -> FireryBlue
            ColorCube -> ColorCube
        }
    }

    fun isFirery(): Boolean {
        return when(this) {
            Red -> false
            Yellow -> false
            Green -> false
            Blue -> false
            FireryRed -> true
            FireryYellow -> true
            FireryGreen -> true
            FireryBlue -> true
            ColorCube -> false
        }
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

    fun Explode(center: Position,multiplier:Float=1f) {
        points += (multiplier*500*comboMultiplier).roundToInt()
        val explosionColor = array[center.x][center.y]
        array[center.x][center.y] = null
        for (dx in -1..1) {
            for (dy in -1..1) {
                val x = center.x + dx
                val y = center.y + dy
                if (x in array.indices && y in array[0].indices) {
                    if (array[x][y] != null && array[x][y]!!.isFirery()) {
                        Explode(Position(x, y),multiplier=multiplier*1.5f)
                    }
                    if (array[x][y] != null && array[x][y]!!.getGroup() == ColorGroup.ColorCube) {
                        array[x][y] = null
                        Destroy(explosionColor!!.getGroup(),multiplier=multiplier*1.5f)
                    }
                    array[x][y] = null
                }
            }
        }
    }

    fun Destroy(color:ColorGroup,multiplier:Float= 1f) {
        for (x in array.indices) {
            for (y in array[x].indices) {
                if (array[x][y]?.getGroup() == color || color == ColorGroup.ColorCube) {
                    points += (650*comboMultiplier*multiplier).roundToInt()
                    if (array[x][y] != null && array[x][y]!!.isFirery()) {
                        Explode(Position(x, y),multiplier=multiplier*1.5f)
                    }
                    array[x][y] = null  // Or appropriate logic to clear the jewel
                }
            }
        }
    }

    fun DeleteBlock(block:Block) {
        for (positon in block.positions) {
            array[positon.x][positon.y] = null
        }
    }

    fun ActivateBlock(block:Block) {
        if (block.positions.size == 4) {
            var specialPosition: Position = Position(-1, -1)
            if (block.positions.contains(Lastpos1))
                specialPosition = Lastpos1
            else if (block.positions.contains((Lastpos2)))
                specialPosition = Lastpos2
            else
                specialPosition = block.center()!!
            val specialColor = block.color.getFireryVersion()
            for (i in  0 until block.positions.size) {
                if (array[block.positions[i].x][block.positions[i].y] != null && array[block.positions[i].x][block.positions[i].y]!!.isFirery())
                    Explode(block.positions[i])
                array[block.positions[i].x][block.positions[i].y] = null
            }
            array[specialPosition.x][specialPosition.y] = specialColor
            return
        }
        if (block.positions.size >= 5) {
            var specialPosition: Position = Position(-1, -1)
            if (block.positions.contains(Lastpos1))
                specialPosition = Lastpos1
            else if (block.positions.contains((Lastpos2)))
                specialPosition = Lastpos2
            else
                specialPosition = block.center()!!
            val specialColor = JewelColor.ColorCube
            for (i in  0 until block.positions.size) {
                if (array[block.positions[i].x][block.positions[i].y] != null && array[block.positions[i].x][block.positions[i].y]!!.isFirery())
                    Explode(block.positions[i])
                array[block.positions[i].x][block.positions[i].y] = null
            }
            array[specialPosition.x][specialPosition.y] = specialColor
            return
        }
        for (i in  0 until block.positions.size) {
            if (array[block.positions[i].x][block.positions[i].y] != null && array[block.positions[i].x][block.positions[i].y]!!.isFirery())
                Explode(block.positions[i])
            array[block.positions[i].x][block.positions[i].y] = null
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
            points += (200*comboMultiplier).roundToInt()
        }

        println(points)
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

    fun getAllBreakingGems(blocks: List<Block>): List<Position> {
        val result = mutableListOf<Position>()
        for (block in blocks) {
            result.addAll(block.positions)
            for (pos in block.positions) {
                if (array[pos.x][pos.y]!!.isFirery()) {
                    val explodingGems = getExplodingBreakingGems(pos,)
                    println("Explode $explodingGems")
                    for (explodingGem in explodingGems) {
                        if (!result.contains(explodingGem)) {
                            result.add(explodingGem)
                        }
                    }
                }
            }
        }
        println("funtion $result")
        return result
    }

    fun getExplodingBreakingGems(center: Position, visited: MutableSet<Position> = mutableSetOf<Position>()): List<Position> {
        val result = mutableListOf<Position>()
        val explosionColor = array[center.x][center.y]

        if (center in visited || !array[center.x][center.y]!!.isFirery()) {
            return result
        }

        result.add(center)
        visited.add(center)

        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue // Skip the center position itself
                val x = center.x + dx
                val y = center.y + dy
                if (x in array.indices && y in array[0].indices) {
                    val position = Position(x, y)
                    if (!visited.contains(position)) {
                        if (array[x][y]!!.isFirery())
                            result.addAll(getExplodingBreakingGems(position, visited))
                        if (array[x][y]!!.getGroup() == ColorGroup.ColorCube)
                            result.addAll(getColorBombBreakingGems(explosionColor!!.getGroup(), position, visited))
                        else
                            result.add(position)
                    }
                }
            }
        }

        return result
    }

    fun getColorBombBreakingGems(color: ColorGroup, source: Position, visited: MutableSet<Position> = mutableSetOf<Position>()): List<Position> {
        val result = mutableListOf<Position>()
        result.add(source)
        visited.add(source)
        for (x in array.indices) {
            for (y in array[x].indices) {
                if (array[x][y]?.getGroup() == color || color == ColorGroup.ColorCube) {
                    val position = Position(x,y)
                    if (array[x][y]!!.isFirery() && !visited.contains(position)) {
                        val explodingGems = getExplodingBreakingGems(position, visited)
                        result.addAll(explodingGems)
                    }
                    if (!visited.contains(position)) {
                        result.add(position)
                        visited.add(position)
                    }
                }
            }
        }
        return result
    }
}