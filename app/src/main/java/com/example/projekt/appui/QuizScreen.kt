package com.example.projekt.appui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projekt.data.Word
import com.example.projekt.data.levels
import com.example.projekt.database.WordsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    repository: WordsRepository,
    onBack: () -> Unit
) {
    val words by repository.wordsFlow.collectAsState(initial = emptyList())
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var showTranslation by remember { mutableStateOf(false) }
    var knowCount by remember { mutableStateOf(0) }
    var dontKnowCount by remember { mutableStateOf(0) }
    var questionCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(words) {
        if (words.isNotEmpty()) {
            currentWord = words.random()
        }
    }

    fun handleAnswer(isCorrect: Boolean) {
        if (isCorrect)
            knowCount++
        else
            dontKnowCount++
        questionCount++

        coroutineScope.launch {
            currentWord?.let { word ->
                val newLevel = when {
                    isCorrect -> word.knowledgeLevel.ordinal.coerceAtMost(levels.entries.size - 2) + 1
                    else -> word.knowledgeLevel.ordinal.coerceAtLeast(2) - 1
                }
                val updatedWord = word.copy(
                    knowledgeLevel = levels.entries[newLevel]
                )
                repository.updateWord(updatedWord)
            }

            currentWord = words.random()
            showTranslation = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fast Quiz") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Question: $questionCount")
                Text("Result (know / don`t know): $knowCount/$dontKnowCount",
                    color = if (questionCount > 0) {
                        if (knowCount > dontKnowCount) Color.Green else Color.Red
                    } else Color.Unspecified)
            }

            Spacer(modifier = Modifier.height(32.dp))

            currentWord?.let { word ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("What does mean:", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(word.original, style = MaterialTheme.typography.displayMedium)

                    Spacer(modifier = Modifier.height(32.dp))

                    if (showTranslation) {
                        Text(word.translation,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary)
                    } else {
                        Button(onClick = { showTranslation = true }) {
                            Text("Show translation")
                        }
                    }
                }
            } ?: run {
                Text("There is not enough words for quiz",
                    style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { handleAnswer(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Dont know")
                }

                Button(
                    onClick = { handleAnswer(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Know")
                }
            }
        }
    }
}