package com.example.sudokuq5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudokuq5.ui.theme.SudokuQ5Theme

// Map out a solved sudoku, and switch around entire rows or columns
// I tried to figure out how to completely randomize it, I don't think I'm smart enough to

val originalSolution = listOf(
    listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
    listOf(4, 5, 6, 7, 8, 9, 1, 2, 3),
    listOf(7, 8, 9, 1, 2, 3, 4, 5, 6),
    listOf(2, 7, 1, 3, 6, 4, 8, 9, 5),
    listOf(8, 3, 5, 2, 9, 1, 6, 4, 7),
    listOf(9, 6, 4, 5, 7, 8, 2, 3, 1),
    listOf(3, 1, 2, 6, 4, 5, 9, 7, 8),
    listOf(5, 9, 7, 8, 1, 2, 3, 6, 4),
    listOf(6, 4, 8, 9, 3, 7, 5, 1, 2)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SudokuQ5Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SudokuGrid(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SudokuGrid(modifier: Modifier = Modifier) {
    val shuffledPuzzle = remember { shuffleFirstRowAndAdjust(originalSolution) }
    val userInputs = remember { mutableStateListOf(*Array(81) { mutableStateOf("") }) }
    val isWin = remember { mutableStateOf(false) }
    LaunchedEffect(userInputs) {
        if (isFirstColumnCorrect(userInputs.map { it.value }, shuffledPuzzle)) {
            isWin.value = true
        }
    }

    // Display 9x9 grid
    LazyVerticalGrid(
        columns = GridCells.Fixed(9),
        modifier = modifier.fillMaxSize()
    ) {
        // Taken from cs112 problem set mostly
        items(81) { index ->
            val rowIndex = index / 9
            val colIndex = index % 9
            val cellValue = if (rowIndex == 0) {
                shuffledPuzzle[rowIndex][colIndex].toString()
            } else {
                userInputs[index].value
            }

            Card(
                modifier = Modifier
                    .aspectRatio(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (rowIndex == 0) {
                        Text(
                            text = cellValue,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 30.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        TextField(
                            value = cellValue,
                            onValueChange = { newValue ->
                                if (newValue.length <= 1 && newValue.toIntOrNull() in 1..9) {
                                    userInputs[index].value = newValue
                                }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White //?
                            ),
                            modifier = Modifier.fillMaxSize(),
                            singleLine = true // This was suggested by chatGPT to fix the issue with
                                              // input text, but still does not show well.
                        )
                    }
                }
            }
        }
    }
    // Centering button
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Reset button
        Button(onClick = { resetBoard(userInputs, originalSolution) }, modifier = Modifier.padding(16.dp)) {
            Text("Reset")
        }
    }
    // Display a Snackbar if the user wins
    if (isWin.value) {
        Snackbar(modifier = Modifier.padding(16.dp)) {
            Text("You win!")
        }
    }
}

fun resetBoard(userInputs: MutableList<MutableState<String>>, solution: List<List<Int>>) {
    // Shuffle first row and adjust rest of puzzle based on  shuffle
    val shuffledPuzzle = shuffleFirstRowAndAdjust(solution)
    // Update  user inputs list to reset values
    userInputs.clear()
    userInputs.addAll(List(81) { mutableStateOf("") }) // Reset all inputs to empty
    shuffledPuzzle.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { colIndex, value ->
            userInputs[rowIndex * 9 + colIndex].value = value.toString()
        }
    }
}

// Function to shuffle the first row and adjust the other rows accordingly
fun shuffleFirstRowAndAdjust(solution: List<List<Int>>): List<List<Int>> {
    val shuffledFirstRow = solution[0].shuffled() // Shuffle the first row
    val columnMapping = solution[0].map { shuffledFirstRow.indexOf(it) } // Find new column positions based on the shuffle

    return solution.map { row ->
        columnMapping.map { columnIndex -> row[columnIndex] }
    }
}

// Check if user's input for first column is correct
fun isFirstColumnCorrect(userInput: List<String>, shuffledPuzzle: List<List<Int>>): Boolean {
    val correctFirstColumn = shuffledPuzzle.map { it[0].toString() }
    return userInput.zip(correctFirstColumn).all { it.first == it.second }
}

// o well :(
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SudokuQ5Theme {
        SudokuGrid()
    }
}
