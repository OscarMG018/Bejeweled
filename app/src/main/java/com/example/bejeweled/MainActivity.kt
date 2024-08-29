package com.example.bejeweled

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import com.example.bejeweled.ui.theme.BejeweledTheme
import com.example.bejeweled.Game.JewelColor
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import kotlinx.coroutines.*
import com.example.bejeweled.Game.*


class MainActivity : ComponentActivity() {

    private val boardViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //deleteRecord(this)
        boardViewModel.SetHighScore(loadRecord(this)?.toIntOrNull() ?: 0)
        setContent {
            BejeweledTheme {
                var showDialog by remember { mutableStateOf(false) }

                // Check if the player has run out of moves
                val moves by boardViewModel.movesLiveData.observeAsState(0)
                if (moves >= 50) {
                    showDialog = true
                    boardViewModel.saveHighScore(this)
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally // Center the content horizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 60.dp),
                        ) {
                            PointsText(boardViewModel)
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 0.dp),
                        ) {
                            MovesText(boardViewModel)
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 0.dp),
                        ) {
                            HighScore(boardViewModel)
                        }
                    }
                    DisplayBoard(boardViewModel, innerPadding)

                    // Show the game over dialog when the player runs out of moves
                    if (showDialog) {
                        GameOverDialog(
                            viewModel = boardViewModel,
                            onDismiss = { showDialog = false },
                            onExit = { finish() } // Exit the activity when the "Exit" button is clicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(viewModel: GameViewModel, onDismiss: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Do nothing, the dialog is blocking */ },
        title = {
            Text(text = "Game Over")
        },
        text = {
            Text("You've run out of moves! Do you want to reset the game?")
        },
        confirmButton = {
            Button(onClick = {
                viewModel.Reset() // Reset the board when the button is clicked
                onDismiss() // Dismiss the dialog
            }) {
                Text("Reset Game")
            }
        },
        dismissButton = {
            Button(onClick = { onExit() }) {
                Text("Exit")
            }
        }
    )
}

@Composable
fun MovesText(viewModel: GameViewModel) {
    val moves by viewModel.movesLiveData.observeAsState(0)

    Text(text = "${50-moves} Moves Left", fontSize = 25.sp)
}

@Composable
fun PointsText(viewModel: GameViewModel) {
    val points by viewModel.pointsLiveData.observeAsState(0)

    Text(text = "$points Points",fontSize = 25.sp)
}

@Composable
fun HighScore(viewModel: GameViewModel) {
    val highScore by viewModel.highScoreLiveData.observeAsState(0)

    Text(text = "Your Record is $highScore Points",fontSize = 25.sp)
}

@Composable
fun DisplayBoard(viewModel: GameViewModel, pad: PaddingValues) {
    val boardState by viewModel.boardLiveData.observeAsState(emptyArray())
    val animationState = viewModel.animationState
    val breakingGems = viewModel.breakingGems

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(Color.LightGray)
        ) {
            for (row in 0 until 9) {
                Row(modifier = Modifier.wrapContentSize()) {
                    for (col in 0 until 9) {
                        val color = boardState[row][col]
                        val position = Position(row, col)

                        AnimatedGem(
                            position = position,
                            color = color,
                            animationState = animationState,
                            pos1 = viewModel.pos1,
                            pos2 = viewModel.pos2,
                            breakingGems = breakingGems,
                            onClick = { viewModel.HandleClick(position) }
                        )
                    }
                }
            }
        }
    }
}

fun getColorsForDisplay(color: JewelColor?): List<Color> {
    return when (color) {
        JewelColor.Red -> listOf(Color.Red, Color.Red)
        JewelColor.Yellow -> listOf(Color.Yellow,Color.Yellow)
        JewelColor.Green -> listOf(Color.Green,Color.Green)
        JewelColor.Blue -> listOf(Color.Blue,Color.Blue)
        JewelColor.FireryRed -> listOf(Color.hsl(300f,0.5f,0.45f),Color.Red,Color.hsl(300f,0.5f,0.45f))
        JewelColor.FireryYellow -> listOf(Color.hsl(20f,1f,0.5f),Color.Yellow,Color.hsl(20f,1f,0.5f))
        JewelColor.FireryGreen -> listOf(Color.hsl(95f,1f,0.3f),Color.Green,Color.hsl(95f,1f,0.3f))
        JewelColor.FireryBlue -> listOf(Color.Cyan,Color.Blue,Color.Cyan)
        JewelColor.ColorCube -> listOf(Color.Black,Color.White)
        JewelColor.FlashRed -> listOf(Color.Black,Color.Red)
        JewelColor.FlashGreen -> listOf(Color.Black,Color.Green)
        JewelColor.FlashYellow -> listOf(Color.Black,Color.Yellow)
        JewelColor.FlashBlue -> listOf(Color.Black,Color.Blue)
        else ->listOf(Color.Black,Color.Black)
    }
}

fun saveRecord(context: Context, record: String) {
    val fileName = "game_record.txt"
    val fileOutputStream: FileOutputStream

    try {
        fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        fileOutputStream.write(record.toByteArray())
        fileOutputStream.close()
        println("Record saved successfully")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadRecord(context: Context): String? {
    val fileName = "game_record.txt"
    var record: String? = null

    try {
        val fileInputStream: FileInputStream = context.openFileInput(fileName)
        val inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        record = bufferedReader.readLine()

        bufferedReader.close()
        inputStreamReader.close()
        fileInputStream.close()
        println("Record loaded successfully")
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return record
}

fun deleteRecord(context: Context): Boolean {
    val fileName = "game_record.txt"
    return context.deleteFile(fileName)
}

