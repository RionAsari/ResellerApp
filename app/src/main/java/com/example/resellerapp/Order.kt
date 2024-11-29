package com.example.resellerapp

data class Order(
    val key: String = "",
    val resellerName: String = "",
    val name: String = "",
    val address: String = "",
    val phone: Long = 0L,
    val item: String = "",
    val dp: Long = 0L,
    val timestamp: Long = System.currentTimeMillis() // Default to the current time
)

