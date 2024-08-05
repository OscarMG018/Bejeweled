
enum class JewelType {
    Red(0)
    Yellow(1)
    Green(2)
    Blue(3)

    companion object {
        fun fromInt(value: Int): Types = Types.values().firts {
            it.value = value
        }
    }
}

class Position(y; Int, x: Int)

class Pairing(var jewelPositions: List<Position> = listOf())

class Board {
    var gameBoard: List<List<JewelType>> = listOf(
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
        listOf(null,null,null,null,null,null,null,null,null,null)
    )

    fun swap(pos1: Position, pos2: Position) {
        JewelType aux = gameBoard[pos1.y][pos1.x]
        gameBoard[pos1.y][pos1.x] = gameBoard[pos2.y][pos1.x]
        gameBoard[pos2.y][pos2.x] = aux
    }

    fun resetBoard() {
        for (y in 0..10)
        for (x in 0..10) {
            val jewelType = (0..4).random()
            gameBoard[y][x] = JewelType.fromInt(jewelType)
        }
    }

    fun getPairings() {
        var boardCopy = gameBoard
        fun dfs(y: Int, x: Int,JewelType type) {
            var res: List<JewelType> = listOf()
            if (boardCopy[y][x] == type) {
                boardCopy[y][x] == null
                res.add(Position(y,x))
                res.addall(dfs(y+1,x))
                res.addall(dfs(y-1,x))
                res.addall(dfs(y,x+1))
                res.addall(dfs(y,x-1))
            }
            return res
        }

        var result: List<Pairing>
        for(y in 0..10)
        for(x in 0..10) {
            if (boardCopy[y][x] == null)
                continue
            else {
                var pairingPositions = dfs(y,x,boardCopy[y][x])
                if (pairingPositions.length >= 3) {
                    result.add(Pairing(pairingPositions))
                }
            }
        }
    }

    fun DeletePairing(pairing: Pairing) {
        for (pos in pairing.jewelPositions) {
            gameBoard[pos.y,pos.x] = null;
        }
    }

    fun Fall() {
        for (i in 0..10) {
            pos = Position(1,i)
            while(pos.y < 10) {
                if (gameBoard[pos.y][pos.x] == null) {
                    pos.y++
                }
                else if (pos.y > 0) {
                    if (gameBoard[pos.y-1][pos.x] == null)
                        swap(pos,Position(pos.y-1,pos.x))
                        pos.y--
                    else {
                        pos.y++
                    }
                }
            }
        }
    }

}