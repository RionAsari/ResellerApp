package com.example.resellerapp

data class Order(
    val key: String = "",
    val resellerName: String = "",
    val name: String = "",
    val address: String = "",
    val phone: Int = 0,
    val item: String = "",
    val dp: Int = 0  // Pastikan dp adalah Int
)
