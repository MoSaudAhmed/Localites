package com.example.localites.models

data class MarketModel(
    val name: String = "",
    val title: String = "",
    val description: String = "",
    val country: String = "",
    val city: String = "",
    val createdBy: String = "",
    val createdDate: String = "",
    val pictures: List<String> = ArrayList()
)

