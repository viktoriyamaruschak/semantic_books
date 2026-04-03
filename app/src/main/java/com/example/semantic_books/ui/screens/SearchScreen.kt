package com.example.semantic_books.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.semantic_books.ui.components.BookCard
import com.example.semantic_books.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: BookViewModel, onBookClick: (String) -> Unit) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🔍 Скляний рядок пошуку
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            placeholder = { Text("Пошук книг за сюжетом...") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { 
                    viewModel.search(textState.text)
                    keyboardController?.hide()
                }
            ),
            trailingIcon = {
                IconButton(onClick = { 
                    viewModel.search(textState.text)
                    keyboardController?.hide()
                }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon", tint = MaterialTheme.colorScheme.primary)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔄 Завантаження
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } 
        // ❌ Помилка
        else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
        // 📚 Результати
        else {
            val currentSavedBooks by viewModel.savedBooks.collectAsState()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Щоб меню не перекривало
            ) {
                items(searchResults, key = { it.id }) { book ->
                    // Відслідковуємо стан: якщо книга є в списку збережених, серце буде червоним!
                    val isSaved = currentSavedBooks.any { it.id == book.id }
                    BookCard(
                        book = book,
                        isSaved = isSaved,
                        onSaveClick = { viewModel.toggleSave(book) },
                        onClick = { onBookClick(book.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

