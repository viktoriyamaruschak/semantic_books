package com.example.semantic_books.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.semantic_books.api.Book
import com.example.semantic_books.api.RetrofitClient
import com.example.semantic_books.api.SearchRequest
import com.example.semantic_books.data.SavedBooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SavedBooksRepository(application)

    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults

    private val _savedBooks = MutableStateFlow<List<Book>>(repository.getSavedBooks())
    val savedBooks: StateFlow<List<Book>> = _savedBooks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Фільтри збережених книг
    val selectedGenre = MutableStateFlow<String?>(null)
    val selectedYearCategory = MutableStateFlow<String?>(null)

    // Схожі книги
    private val _similarBooks = MutableStateFlow<List<Book>>(emptyList())
    val similarBooks: StateFlow<List<Book>> = _similarBooks

    init {
        loadInitialBooks()
    }

    private fun loadInitialBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.getAllBooks()
                _searchResults.value = response["books"] ?: emptyList()
            } catch (e: Exception) {
                _error.value = "Помилка завантаження: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔍 Розумний Пошук
    fun search(query: String) {
        if (query.isBlank()) {
            loadInitialBooks()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.searchBooks(SearchRequest(query))
                _searchResults.value = response.results.map { it.book }
            } catch (e: Exception) {
                _error.value = "Помилка мережі: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ❤️ Зберегти/Видалити книгу
    fun toggleSave(book: Book) {
        repository.toggleSaved(book)
        _savedBooks.value = repository.getSavedBooks() // Оновлюємо стан для UI
    }

    fun isSaved(bookId: String): Boolean = _savedBooks.value.any { it.id == bookId }
    
    // 📖 Отримати книгу для сторінки деталей
    fun getBookById(bookId: String): Book? {
        return _searchResults.value.find { it.id == bookId } 
            ?: _savedBooks.value.find { it.id == bookId }
    }

    // 🔮 Завантажити схожі книги через штучний інтелект (новий ендпоінт)
    fun loadSimilarBooks(bookId: String) {
        viewModelScope.launch {
            _similarBooks.value = emptyList() // Очищуємо попередні результати
            try {
                val response = RetrofitClient.api.getSimilarBooks(bookId)
                _similarBooks.value = response.results.map { it.book }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 🇺🇦 Переклад опису
    private val _translatedDescriptions = MutableStateFlow<Map<String, String>>(emptyMap())
    val translatedDescriptions: StateFlow<Map<String, String>> = _translatedDescriptions

    private val _isTranslating = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isTranslating: StateFlow<Map<String, Boolean>> = _isTranslating

    fun translateDescription(bookId: String) {
        if (_translatedDescriptions.value.containsKey(bookId) || _isTranslating.value[bookId] == true) return

        viewModelScope.launch {
            _isTranslating.value = _isTranslating.value + (bookId to true)
            try {
                val response = RetrofitClient.api.getUkrainianDescription(bookId)
                _translatedDescriptions.value = _translatedDescriptions.value + (bookId to response.description_uk)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isTranslating.value = _isTranslating.value + (bookId to false)
            }
        }
    }
}

