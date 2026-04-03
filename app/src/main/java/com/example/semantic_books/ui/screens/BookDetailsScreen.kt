package com.example.semantic_books.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.semantic_books.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookId: String?,
    viewModel: BookViewModel,
    onBackClick: () -> Unit,
    onSimilarBookClick: (String) -> Unit
) {
    if (bookId == null) {
        onBackClick()
        return
    }

    val book = viewModel.getBookById(bookId)
    if (book == null) {
        onBackClick()
        return
    }

    val savedBooks by viewModel.savedBooks.collectAsState()
    val isSaved = savedBooks.any { it.id == book.id }
    
    val similarBooks by viewModel.similarBooks.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(bookId) {
        viewModel.loadSimilarBooks(book.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, book.title)
                            putExtra(Intent.EXTRA_TEXT, "Заціни книгу «${book.title}» від ${book.author}!\n\n${book.description.take(150)}...\n\nЗнайдено через AI Book Search! 📚✨")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Поділитися книгою..."))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Поділитися", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { viewModel.toggleSave(book) }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Уподобати",
                            tint = if (isSaved) Color.Red else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🖼 Велика обкладинка
            AsyncImage(
                model = book.cover_url,
                contentDescription = "Cover for ${book.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(200.dp)
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.DarkGray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 📝 Інформація
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Автор: ${book.author}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Жанр: ${book.genre}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Рік: ${book.year}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Опис
            val translatedDescriptions by viewModel.translatedDescriptions.collectAsState()
            val isTranslatingMap by viewModel.isTranslating.collectAsState()
            
            val ukDescription = translatedDescriptions[book.id]
            val isLoadingTranslation = isTranslatingMap[book.id] ?: false

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Опис",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (ukDescription == null) {
                    TextButton(
                        onClick = { viewModel.translateDescription(book.id) },
                        enabled = !isLoadingTranslation
                    ) {
                        if (isLoadingTranslation) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isLoadingTranslation) "Перекладаю..." else "Перекласти 🇺🇦")
                    }
                } else {
                    Text(
                        text = "Перекладено ✅",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = ukDescription ?: book.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 🔮 Схожі за вайбом (Звернення до нового АПІ)
            if (similarBooks.isNotEmpty()) {
                Text(
                    text = "Схожі за вайбом 🔮",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(similarBooks) { similarBook ->
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { onSimilarBookClick(similarBook.id) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = similarBook.cover_url,
                                contentDescription = "Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = similarBook.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
