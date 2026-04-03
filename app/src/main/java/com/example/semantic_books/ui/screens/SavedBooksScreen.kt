package com.example.semantic_books.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.semantic_books.ui.components.BookCard
import com.example.semantic_books.viewmodel.BookViewModel

@Composable
fun SavedBooksScreen(viewModel: BookViewModel, onBookClick: (String) -> Unit) {
    val savedBooks by viewModel.savedBooks.collectAsState()
    val selectedGenre = viewModel.selectedGenre.collectAsState().value
    val selectedYear = viewModel.selectedYearCategory.collectAsState().value

    val allGenres = savedBooks.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted()
    
    // Спрощена логіка категорій років
    val yearCategories = listOf("До 1900", "1900-1950", "Після 1950")
    
    val filteredBooks = savedBooks.filter { book ->
        val matchGenre = selectedGenre == null || book.genre == selectedGenre
        val matchYear = when(selectedYear) {
            "До 1900" -> book.year < 1900
            "1900-1950" -> book.year in 1900..1950
            "Після 1950" -> book.year > 1950
            else -> true
        }
        matchGenre && matchYear
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок з зіркою
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = "Star", tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Мої Уподобані",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Фільтри
        if (savedBooks.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Жанри
                items(allGenres) { genre ->
                    FilterChip(
                        selected = selectedGenre == genre,
                        onClick = { viewModel.selectedGenre.value = if (selectedGenre == genre) null else genre },
                        label = { Text(genre) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Роки
                items(yearCategories) { yearCat ->
                    FilterChip(
                        selected = selectedYear == yearCat,
                        onClick = { viewModel.selectedYearCategory.value = if (selectedYear == yearCat) null else yearCat },
                        label = { Text(yearCat) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.3f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (filteredBooks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Нічого не знайдено 🔎",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredBooks, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        isSaved = true,
                        onSaveClick = { viewModel.toggleSave(book) },
                        onClick = { onBookClick(book.id) },
                        modifier = Modifier.animateItem() // ✨ Крута Compose 1.7 анімація!
                    )
                }
            }
        }
    }
}
