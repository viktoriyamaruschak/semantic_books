package com.example.semantic_books

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.semantic_books.ui.screens.MainScreen
import com.example.semantic_books.viewmodel.BookViewModel
import com.example.semantic_books.ui.theme.Semantic_booksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Semantic_booksTheme {
                val viewModel: BookViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}