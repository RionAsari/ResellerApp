/*package com.example.resellerapp

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
}*/

/*package com.example.resellerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resellerapp.databinding.ActivityMainBinding
import com.example.resellerapp.ui.theme.GenerateQRActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var ordersAdapter: OrdersAdapter  // Adapter untuk RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        // Inisialisasi RecyclerView
        ordersAdapter = OrdersAdapter(
            ordersList = emptyList(),
            onDeleteClick = { key -> deleteOrder(key) },
            onSaveClick = { key -> saveOrder(key) }
        )
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ordersAdapter
        }

        // Mendapatkan data pesanan dari Firebase
        val ordersRef = database.child("orders")
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ordersList = mutableListOf<Order>()

                snapshot.children.forEach { orderSnapshot ->
                    val order = orderSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        // Masukkan key dan pastikan dp valid
                        val orderWithKey = order.copy(
                            key = orderSnapshot.key ?: "",
                            dp = order.dp.toString().toIntOrNull() ?: 0
                        )
                        ordersList.add(orderWithKey)
                    }
                }

                // Perbarui RecyclerView dengan data baru
                ordersAdapter.updateOrdersList(ordersList)
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

    // Fungsi untuk menghapus pesanan
    private fun deleteOrder(key: String) {
        database.child("orders").child(key).removeValue()
    }

    // Fungsi untuk menyimpan (bisa diisi logika sesuai kebutuhan)
    private fun saveOrder(key: String) {
        // Logika penyimpanan (contoh: update status di Firebase)
    }
}*/

package com.example.resellerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resellerapp.databinding.ActivityMainBinding
import com.example.resellerapp.ui.theme.GenerateQRActivity
import com.google.firebase.database.*
import com.example.resellerapp.OrdersAdapter
import com.example.resellerapp.Order

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var ordersAdapter: OrdersAdapter // Adapter untuk RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Setup RecyclerView dengan adapter
        ordersAdapter = OrdersAdapter(
            ordersList = emptyList(),
            onDeleteClick = { key -> deleteOrder(key) },
            onSaveClick = { key -> saveOrder(key) }
        )
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ordersAdapter
        }

        // Observasi data dari Firebase
        observeOrders()

        // Tombol Generate QR Code
        binding.generateQrButton.setOnClickListener {
            val intent = Intent(this, GenerateQRActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi untuk mengamati data di Firebase
    private fun observeOrders() {
        val ordersRef = database.child("orders")
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ordersList = mutableListOf<Order>()

                // Mengambil data dari Firebase
                snapshot.children.forEach { orderSnapshot ->
                    val order = orderSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        ordersList.add(order.copy(key = orderSnapshot.key ?: ""))
                    }
                }

                // Perbarui adapter dengan data baru
                ordersAdapter.updateOrdersList(ordersList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Logika error handling
            }
        })
    }

    // Fungsi untuk menghapus data dari Firebase
    private fun deleteOrder(key: String) {
        database.child("orders").child(key).removeValue()
    }

    // Fungsi untuk menyimpan data (contoh: update status)
    private fun saveOrder(key: String) {
        // Logika untuk menyimpan atau update status (contoh: mark as completed)
        database.child("orders").child(key).child("status").setValue("Saved")
    }
}
