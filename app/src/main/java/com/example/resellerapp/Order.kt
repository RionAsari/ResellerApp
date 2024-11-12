package com.example.resellerapp

data class Order(
    var key: String = "",  // Tambahkan key
    val address: String = "",
    val item: String = "",
    val name: String = "",
    val phone: String = "",
    val resellerId: String = "",
    val resellerName: String = "",
    val timestamp: String = ""
)

