package com.example.bejeweled

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bejeweled.Game.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

enum class AnimationState {
    IDLE, SWAPPING, BREAKING
}

class GameViewModel : ViewModel() {
    // Create an instance of the Board
    private val board: Board = Board()
    var pos1: Position = Position(-1,-1)
    var pos2: Position = Position(-1,-1)

    private val _boardLiveData = MutableLiveData<Array<Array<JewelColor?>>>()
    private val _pointLiveData = MutableLiveData<Int>()
    private val _movesLiveData = MutableLiveData<Int>()
    private val _highScoreLiveData = MutableLiveData<Int>()
    private val _animatingGems = MutableStateFlow<List<AnimatingGem>>(emptyList())

    val boardLiveData: LiveData<Array<Array<JewelColor?>>>
        get() = _boardLiveData
    val pointsLiveData: LiveData<Int>
        get() = _pointLiveData
    val movesLiveData: LiveData<Int>
        get() = _movesLiveData
    val highScoreLiveData: LiveData<Int>
        get() = _highScoreLiveData
    var isSwapping by mutableStateOf(false)
        private set
    val animatingGems: StateFlow<List<AnimatingGem>> = _animatingGems.asStateFlow()
    var animationState by mutableStateOf(AnimationState.IDLE)
    private set

    var breakingGems by mutableStateOf<List<Position>>(emptyList())
        private set

    init {
        _boardLiveData.value = board.getState()
        _pointLiveData.value = board.points
        _movesLiveData.value = board.moves
        _highScoreLiveData.value = 0
    }

    fun saveHighScore(context: Context) {
        val currentPoints = pointsLiveData.value ?: 0
        val previousRecord = loadRecord(context)?.toIntOrNull() ?: 0

        if (currentPoints > previousRecord) {
            saveRecord(context, currentPoints.toString())
        }
    }

    fun Reset() {
        board.initBoard()
        pos1 = Position(-1,-1)
        pos2 = Position(-1,-1)
        updateLiveData()
    }

    fun HandleClick(pos: Position) {
        if (animationState != AnimationState.IDLE) return
        if (pos1.x == -1) {
            pos1 = pos
            updateLiveData()
        } else {
            if (pos1.isOrthogonallyAdjacent(pos)) {
                pos2 = pos
                viewModelScope.launch {
                    val po = board.points
                    if (board.getCell(pos1.x,pos1.y) == JewelColor.ColorCube) {
                        animationState = AnimationState.BREAKING
                        val color = board.getCell(pos2.x,pos2.y)
                        breakingGems = board.activateSpecialGem(pos1, multiplier = 1f,color!!.getGroup())
                        delay(500)
                        for (pos in breakingGems) {
                            board.setCell(pos.x,pos.y,null)
                        }
                        board.MakeBlocksFall()
                        delay(300) // Give some time for the falling animation
                        board.FillBoard()
                        updateLiveData() // Update LiveData after filling
                        breakingGems = emptyList()
                        delay(300) // Give some time for new gems to appear
                    }
                    else if (board.getCell(pos2.x,pos2.y) == JewelColor.ColorCube) {
                        animationState = AnimationState.BREAKING
                        val color = board.getCell(pos1.x,pos1.y)
                        breakingGems = board.activateSpecialGem(pos2, multiplier = 1f,color!!.getGroup())
                        delay(500)
                        for (pos in breakingGems) {
                            board.setCell(pos.x,pos.y,null)
                        }
                        board.MakeBlocksFall()
                        delay(300) // Give some time for the falling animation
                        board.FillBoard()
                        updateLiveData() // Update LiveData after filling
                        breakingGems = emptyList()
                        delay(300) // Give some time for new gems to appear
                    }
                    else {
                    animationState = AnimationState.SWAPPING
                    delay(500) // Animation duration
                    board.Swap(pos1, pos2)
                    updateLiveData()
                    animationState = AnimationState.IDLE
                    delay(1)
                        }
                    checkForBreaks()
                    animationState = AnimationState.IDLE
                    pos1 = Position(-1, -1)
                    pos2 = Position(-1, -1)
                    updateLiveData()
                    println(board.points-po)

                }
            } else {
                pos1 = Position(-1, -1)
                pos2 = Position(-1, -1)
                updateLiveData()
            }
        }
    }

    private suspend fun checkForBreaks() {
        while (true) {
            val blocks = board.detectBlocks()
            if (blocks.isNotEmpty()) {
                val destroyedPositions = mutableListOf<Position>()
                for (block in blocks)
                    for (pos in block.positions) {
                        destroyedPositions.addAll(board.activateSpecialGem(pos))
                    }
                breakingGems = destroyedPositions
                animationState = AnimationState.BREAKING
                delay(500) // Breaking animation duration
                board.ActivateBlock(blocks)
                board.MakeBlocksFall()
                delay(300) // Give some time for the falling animation
                board.FillBoard()
                updateLiveData() // Update LiveData after filling
                breakingGems = emptyList()
                delay(300) // Give some time for new gems to appear
            } else {
                board.comboMultiplier = 1f
                break
            }
        }
        animationState = AnimationState.IDLE
        board.moves++
        updateLiveData()
    }

    private fun updateLiveData() {
        _boardLiveData.value = board.getState().copyOf()
        _pointLiveData.value = board.points
        _movesLiveData.value = board.moves
        if ((_pointLiveData.value ?: 0) > (_highScoreLiveData.value ?: 0))
            _highScoreLiveData.value = _pointLiveData.value
    }

    fun SetHighScore(value:Int) {
        _highScoreLiveData.value = value
    }

}