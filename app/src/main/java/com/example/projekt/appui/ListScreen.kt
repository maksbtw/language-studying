package com.example.projekt.appui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projekt.data.Word
import com.example.projekt.data.levels
import com.example.projekt.ui.theme.ProjektTheme
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.projekt.R
import com.example.projekt.database.WordsRepository

// List Screen -------------------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    repository: WordsRepository,
    modifier: Modifier = Modifier,
    onAddWord: (Word) -> Unit,
    onDeleteWord: (Word) -> Unit,
    onEditWord: (Word) -> Unit,
) {
    val words by repository.wordsFlow.collectAsState(initial = emptyList())
    Log.d("WordsListScreen", "Words: $words")

    var wordToManage: Word? by remember { mutableStateOf<Word?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNewDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 5.dp, 10.dp, 50.dp)
                    .offset(x = (15).dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj nowe słowo")
            }
        }
    ) { innerPadding ->
        Surface (modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 110.dp)) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                item {
                    Text(
                        text = "List of words",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(
                    items = words
                ) { word: Word ->
                    WordItem(
                        word = word,
                        onEditClick = { showEditDialog = true; wordToManage = word },
                        onDelete = { showDeleteDialog = true; wordToManage = word },
                        onLevelChange = { showLevelDialog = true; wordToManage = word }
                    )
                }
            }

            // dialogs ----------------------
            if (showDeleteDialog && wordToManage != null) {
                DeleteDialog(
                    word = wordToManage!!,
                    onConfirm = {
                        onDeleteWord(wordToManage!!)
                        wordToManage = null
                        showDeleteDialog = false
                    },
                    onCancel = {
                        wordToManage = null
                        showDeleteDialog = false
                    }
                )
            }
            if (showLevelDialog) {
                LevelChangingDialog(
                    word = wordToManage!!,
                    onChoosing = { selectedLevel ->
                        val updatedWord = wordToManage!!.copy(knowledgeLevel = selectedLevel)
                        onEditWord(updatedWord)
                        showLevelDialog = false
                        wordToManage = null
                    },
                    onCancel = {
                        showLevelDialog = false
                        wordToManage = null
                    },

                    )
            }
            if (showNewDialog) {
                CreateOfEditWord(
                    onConfirm = { originalWord, translation ->
                        onAddWord(
                            Word(
                                original = originalWord,
                                translation = translation,
                                definition = wordToManage?.definition,
                                example = wordToManage?.example
                            )
                        )
                        showNewDialog = false
                    },
                    onCancel = { showNewDialog = false },
                    isNewWord = true
                )
            }
            if (showEditDialog) {
                CreateOfEditWord(
                    onConfirm = { originalWord, translation ->
                        val originalTextChanged = (wordToManage?.original != originalWord)
                        val updatedWord = wordToManage!!.copy(
                            original = originalWord,
                            translation = translation,
                            // Zachowaj pozostałe właściwości
                            definition = if (originalTextChanged) null else wordToManage?.definition,
                            example = if (originalTextChanged) null else wordToManage?.example,
                            knowledgeLevel = wordToManage?.knowledgeLevel ?: levels.UNDEFINED,
                            wordId = wordToManage!!.wordId
                        )
                        onEditWord(updatedWord)
                        showEditDialog = false
                    },
                    onCancel = { showEditDialog = false },
                    isNewWord = false,
                    originalLastState = wordToManage!!.original,
                    translationLastState = wordToManage!!.translation
                )
            }
        }
    }
}

// Word Item ---------------------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordItem(
    word: Word,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onLevelChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = onDelete
            ),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(word.knowledgeLevel.color, shape = CircleShape)
                        .combinedClickable(
                            onClick = onLevelChange,
                        )
                        .semantics {
                            contentDescription = word.knowledgeLevel.name
                        }
                )
                Text(
                    word.original,
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                )
                val rotationAngle = if (expanded)
                    0 else 180
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle.toFloat())
                        .weight(0.2f)
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.TwoTone.MoreVert,
                        contentDescription = "Edit word",
                        modifier = Modifier.weight(0.2f)
                    )
                }
            }
            Column() {
                if (expanded) {
                    Text(
                        if(word.translation.toString() != "") word.translation.toString() else "No translation",
                        modifier = modifier.padding(10.dp),
                        color = Color.Gray
                    )
                    Text("Definition: ", modifier = modifier.padding(10.dp, 10.dp, 10.dp, 0.dp))
                    Text(
                        if(word.definition != null) word.definition.toString() else "No definition found",
                        modifier = modifier.padding(10.dp, 5.dp, 10.dp, 5.dp),
                        color = Color.Gray
                    )
                    Text("Examples: ", modifier = modifier.padding(10.dp, 10.dp, 10.dp, 0.dp))
                    Text(
                        if(word.example != null) word.example.toString() else "No example found",
                        modifier = modifier.padding(10.dp, 5.dp, 10.dp, 5.dp),
                        color = Color.Gray
                    )
                }
            }
        }
    }

}

// Delete Dialog -----------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialog(
    word: Word,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(color = Color.White, RoundedCornerShape(16.dp)),
        onDismissRequest = onCancel,
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Text(text = "Are you sure you want to delete ${word.original}?")
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                TextButton(
                    modifier = Modifier.padding(end = 20.dp),
                    onClick = onCancel,
                ) {
                    Text("Cancel")
                }

                TextButton(
                    onClick = onConfirm,
                ) {
                    Text("Confirm", color = Color.Red)
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun DeleteDialogPreview() {
//    ProjektTheme {
//        DeleteDialog(
//            word = Word(
//                original = "Apple",
//                translation = "Jablko",
//                definition = "Fruit",
//                examples = listOf("Eat an apple a day, keeps the doctor away"),
//                knowledgeLevel = levels.AVERAGE
//            ),
//            onConfirm = { },
//            onCancel = { }
//        )
//    }
//}

// Level Changing Dialog ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelChangingDialog(
    word: Word,
    onChoosing: (levels) -> Unit,
    onCancel: () -> Unit
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(color = Color.White, RoundedCornerShape(16.dp)),
        onDismissRequest = onCancel,
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Text(text = "Choose level of knowing word: ${word.original}", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (level in levels.entries) {
                    TextButton(
                        modifier = Modifier.padding(end = 20.dp),
                        onClick = { onChoosing(level) },
                    ) {
                        Text(level.name, color = level.color)
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun LevelChangingDialogPreview() {
//    ProjektTheme {
//        LevelChangingDialog(
//            word = Word(
//                original = "Apple",
//                translation = "Jablko",
//                definition = "Fruit",
//                examples = listOf("Eat an apple a day, keeps the doctor away"),
//                knowledgeLevel = levels.AVERAGE
//            ),
//            onChoosing = {},
//            onCancel = {}
//        )
//    }
//}

// Create new Word ---------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfEditWord(
    onConfirm: (String, String) -> Unit,
    onCancel: () -> Unit,
    isNewWord: Boolean,
    originalLastState: String = "",
    translationLastState: String = "",
) {
    var originalWordState by remember { if(isNewWord) mutableStateOf("") else mutableStateOf(originalLastState) }
    var translationState by remember { if(isNewWord) mutableStateOf("") else mutableStateOf(translationLastState) }

    BasicAlertDialog(
        modifier = Modifier
            .background(color = Color.White, RoundedCornerShape(16.dp)),
        onDismissRequest = {},
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isNewWord) "Add new word" else "Edit word",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextFieldWithClearAndError(
                value = originalWordState,
                onValueChange = { newValue ->
                    originalWordState = newValue
                },
                label = "Original word",
                errorMessage = "The original word is incorrect",
                isError = false
            )
            OutlinedTextFieldWithClearAndError(
                value = translationState,
                onValueChange = { newValue ->
                    translationState = newValue
                },
                label = if(isNewWord) "Translation (optional)" else "Translation",
                errorMessage = "The translation is incorrect",
                isError = false
            )
            Row (
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                TextButton(
                    onClick = onCancel,
                ) {
                    Text("Cancel", color = Color.Gray)
                }
                TextButton(
                    onClick = { onConfirm(originalWordState, translationState) },
                ) {
                    Text("Confirm", color = Color.Blue)
                }
            }
        }

    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Preview
//@Composable
//fun CreateNewWordPreview() {
//    ProjektTheme {
//        CreateNewWord(
//            onConfirm = {},
//            onCancel = {},
//        )
//    }
//}

// Outlined Text Field ------------------------------------------------------------

@Composable
fun OutlinedTextFieldWithClearAndError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onValueChange("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(
                            R.string.clear_content_description,
                            label
                        )
                    )
                }
            }
        },
        isError = isError,
        supportingText = {
            Row {
                if (isError)
                    Text(errorMessage)
            }
        },
        modifier = modifier
    )
}