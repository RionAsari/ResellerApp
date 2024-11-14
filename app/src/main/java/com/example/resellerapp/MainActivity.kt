package com.example.resellerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.resellerapp.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        // Mendapatkan data pesanan dari Firebase
        val ordersRef = database.child("orders")

        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ordersList = mutableListOf<Order>()

                snapshot.children.forEach { orderSnapshot ->
                    val order = orderSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        // Pastikan dp bisa dikonversi menjadi Int jika dibutuhkan
                        val orderWithKey = order.copy(
                            key = orderSnapshot.key ?: "",
                            dp = order.dp.toString().toIntOrNull() ?: 0 // Mengonversi dp jika perlu
                        )
                        ordersList.add(orderWithKey)
                    }
                }

                // Update tampilan dengan ordersList
                binding.ordersTextView.text = ordersList.joinToString("\n") { order ->
                    "Nama Reseller: ${order.resellerName}\n" +
                            "Nama: ${order.name}\n" +
                            "Alamat: ${order.address}\n" +
                            "Telepon: ${order.phone}\n" +
                            "Barang: ${order.item}\n" +
                            "DP: ${order.dp}\n\n"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })




        // Tombol untuk ke QRCodeActivity
        binding.generateQrButton.setOnClickListener {
            val intent = Intent(this, GenerateQRActivity::class.java)
            startActivity(intent)
        }
    }
}
