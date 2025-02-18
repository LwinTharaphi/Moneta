package com.example.moneta.model

import java.io.Serializable

data class Article(
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String?
) : Serializable // Make Article Serializable

