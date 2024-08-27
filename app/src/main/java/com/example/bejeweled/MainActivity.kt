package com.example.bejeweled

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.InputBinding
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.bejeweled.ui.theme.BejeweledTheme
import com.example.bejeweled.Game.Board
import com.example.bejeweled.Game.JewelColor
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.*

import androidx.lifecycle.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.activity.viewModels

class GameViewModel : ViewModel() {
    // Create an instance of the Board
    private val board: Board = Board()

    private val _boardLiveData  = MutableLiveData<Array<Array<JewelColor?>>>()
    val boardLiveData: LiveData<Array<Array<JewelColor?>>>
        get() = _boardLiveData

    init {
        _boardLiveData.value = board.getState()
    }

    fun setCell(row: Int, col: Int, color: JewelColor?) {
        board.setCell(row,col,color)
        updateBoardLiveData()
    }

    fun CheckMove() {
        board.CheckMove()
        updateBoardLiveData()
    }

    private fun updateBoardLiveData() {
        _boardLiveData.value = board.getState().copyOf()
    }
}
class MainActivity : ComponentActivity() {

    private val boardViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BejeweledTheme {
                DisplayBoard(boardViewModel)
            }
        }
    }
}

@Composable
fun DisplayBoard(viewModel: GameViewModel) {

    val boardState by viewModel.boardLiveData.observeAsState(emptyArray())

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color.LightGray)
            ) {
                for (row in 0 until 9) {
                    Row(
                        modifier = Modifier.wrapContentSize()
                    ) {
                        for (col in 0 until 9) {
                            val color = boardState[row][col]
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(getColorForDisplay(color))
                                    .border(1.dp, Color.Gray)
                                    .clickable {
                                        println("click")
                                        if (boardState[row][col] == null) {
                                            viewModel.CheckMove()
                                            return@clickable
                                        }
                                        viewModel.setCell(row, col, null)
                                        println(boardState[row][col])
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getColorForDisplay(color: JewelColor?): androidx.compose.ui.graphics.Color {
    return when (color) {
        JewelColor.Red -> androidx.compose.ui.graphics.Color.Red
        JewelColor.Yellow -> androidx.compose.ui.graphics.Color.Yellow
        JewelColor.Green -> androidx.compose.ui.graphics.Color.Green
        JewelColor.Blue -> androidx.compose.ui.graphics.Color.Blue
        else -> androidx.compose.ui.graphics.Color.Black
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BejeweledTheme {
        Greeting("Android")
    }
}