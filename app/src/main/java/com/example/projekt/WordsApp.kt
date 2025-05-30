package com.example.projekt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekt.appui.ListScreen
import com.example.projekt.appui.QuizScreen
import com.example.projekt.appui.StatsScreen
import com.example.projekt.database.WordsRepository
import kotlinx.coroutines.launch

enum class WordsAppDestinations() {
    List,
    Stats,
    Quiz
}

@Composable
fun WordsApp (
    repository: WordsRepository,
    navController: NavHostController = rememberNavController()
) {
    val coroutineScope = rememberCoroutineScope()
    val allWords = repository.getAllWords().collectAsState(initial = emptyList()).value

    Scaffold(
        topBar = {},
        floatingActionButton = {
            Row (
                modifier = Modifier.fillMaxWidth().padding(start = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(
                    modifier = Modifier.width(100.dp),
                    onClick = {
                    navController.navigate(WordsAppDestinations.Stats.name)
                }) {
                    Icon(painter = painterResource(R.drawable.stats), contentDescription = "Statistics")
                }
                FloatingActionButton(
                    modifier = Modifier.width(100.dp),
                    onClick = {
                    navController.navigate(WordsAppDestinations.List.name)
                }) {
                    Icon(Icons.Filled.Home, contentDescription = "Home page")
                }
                FloatingActionButton(
                    modifier = Modifier.width(100.dp),
                    onClick = { navController.navigate(WordsAppDestinations.Quiz.name) }
                ) {
                    Icon(painter = painterResource(R.drawable.quiz), "Quiz")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WordsAppDestinations.List.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = WordsAppDestinations.List.name) {
                ListScreen(
                    repository = repository,
                    onAddWord = { word ->
                        coroutineScope.launch {
                            repository.addWord(word)
                            val (fetchedDefinition, fetchedExample) = repository.getWordDefinition(word.original)

                            val updatedWord = word.copy(
                                definition = fetchedDefinition,
                                example = fetchedExample
                            )
                            repository.updateWord(updatedWord)
                        }
                    },
                    onDeleteWord = { word ->
                        coroutineScope.launch { repository.deleteWord(word) }
                    },
                    onEditWord = { word ->
                        coroutineScope.launch {
                            var wordToUpdate = word

                            if ((word.definition.isNullOrEmpty() || word.example.isNullOrEmpty()) && word.original.isNotEmpty()){
                                val (fetchedDefinition, fetchedExample) = repository.getWordDefinition(word.original)
                                wordToUpdate = word.copy(
                                    definition = fetchedDefinition,
                                    example = fetchedExample
                                )
                            }
                            repository.updateWord(wordToUpdate)
                        }
                    },
                )
            }
            composable(route = WordsAppDestinations.Stats.name) {
                StatsScreen(
                    words = allWords,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(route = WordsAppDestinations.Quiz.name) {
                QuizScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun WordsAppPreview() {
//    WordsApp(
//        repository = (LocalContext.current as WordsApplication).wordsRepository
//    )
//}