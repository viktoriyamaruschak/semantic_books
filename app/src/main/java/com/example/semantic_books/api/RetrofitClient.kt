package com.example.semantic_books.api

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// 📚 Моделі даних
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val genre: String,
    val year: Int,
    val description: String,
    val cover_url: String
)

data class SearchRequest(val query: String, val limit: Int = 10)
data class SearchResponse(val results: List<SearchResult>)
data class SearchResult(val relevance: Double, val book: Book)

data class TranslationResponse(val book_id: String, val description_uk: String)

// 🔌 Інтерфейс підключення до твого FastAPI 서버у
interface BookApi {
    @POST("search")
    suspend fun searchBooks(@Body request: SearchRequest): SearchResponse
    
    @GET("books")
    suspend fun getAllBooks(): Map<String, List<Book>>

    @GET("book/{book_id}/similar")
    suspend fun getSimilarBooks(
        @retrofit2.http.Path("book_id") bookId: String,
        @retrofit2.http.Query("limit") limit: Int = 5
    ): SearchResponse

    @GET("book/{book_id}/description/uk")
    suspend fun getUkrainianDescription(
        @retrofit2.http.Path("book_id") bookId: String
    ): TranslationResponse
}

// 🚀 Клієнт Retrofit
object RetrofitClient {
    // ВАЖЛИВО: Твій публічний IP
    private const val BASE_URL = "http://178.74.250.135:8000/api/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    val api: BookApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApi::class.java)
    }
}
