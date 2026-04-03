package com.example.semantic_books.data

import android.content.Context
import android.content.SharedPreferences
import com.example.semantic_books.api.Book
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SavedBooksRepository(context: Context) {
    // Створюємо закрите локальне сховище
    private val prefs: SharedPreferences = context.getSharedPreferences("saved_books", Context.MODE_PRIVATE)
    private val gson = Gson()

    // 📖 Отримати всі збережені книги
    fun getSavedBooks(): List<Book> {
        val json = prefs.getString("books", "[]")
        val type = object : TypeToken<List<Book>>() {}.type
        return gson.fromJson(json, type)
    }

    // ❤️ Додати/Видалити книгу з улюблених
    fun toggleSaved(book: Book): Boolean {
        val current = getSavedBooks().toMutableList()
        val exists = current.find { it.id == book.id }
        
        val isSaved = if (exists != null) {
            current.remove(exists)
            false
        } else {
            current.add(book)
            true
        }
        
        // Зберігаємо оновлений список
        prefs.edit().putString("books", gson.toJson(current)).apply()
        return isSaved
    }
    
    // ❓ Перевірка чи книга вже в улюблених
    fun isSaved(bookId: String): Boolean {
        return getSavedBooks().any { it.id == bookId }
    }
}
