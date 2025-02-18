package com.example.moneta.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.moneta.model.Article
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class NewsResponse(
    val articles: List<Article>
)

interface NewsAPI {
    @GET("everything")
    suspend fun getArticles(
        @Query("q") query: String = "finance", // Default to "finance"
        @Query("apiKey") apiKey: String // API key as a parameter
    ): NewsResponse
}

@Composable
fun FinancialNewsScreen(navController: NavController) {
    var newsArticles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val apiKey = "6b4dd7acede9450ea76b9484e2a28c2e" // Replace with your API key

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://newsapi.org/v2/") // Base URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val newsAPI = retrofit.create(NewsAPI::class.java)
                val response = newsAPI.getArticles(apiKey = apiKey) // Call to get finance articles

                newsArticles = response.articles
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error fetching news: ${e.localizedMessage}"
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(newsArticles) { article ->
                        NewsCard(article) // Pass the article to the NewsCard
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(article: Article) {
    var expanded by remember { mutableStateOf(false) } // Track if the card is expanded

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { expanded = !expanded }, // Toggle expansion on click
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            article.urlToImage?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2, // Show full title when expanded
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            article.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = if (expanded) Int.MAX_VALUE else 2, // Show full description when expanded
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "See More",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.description ?: "No full content available",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

