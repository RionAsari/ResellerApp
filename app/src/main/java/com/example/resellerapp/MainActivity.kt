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

package com.example.resellerapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resellerapp.databinding.ActivityMainBinding
import com.example.resellerapp.ui.theme.GenerateQRActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var ordersAdapter: OrdersAdapter // Adapter untuk RecyclerView
    private var previousOrdersCount: Int = 0 // Menyimpan jumlah data order sebelumnya

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

        // Handle delete all
        binding.deleteallButton.setOnClickListener {
            deleteallOrders()
        }

        // Buat Notification Channel
        createNotificationChannel()
    }

    // Fungsi untuk mengamati data di Firebase
    /*private fun observeOrders() {
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
    }*/

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

                // Cek apakah jumlah data bertambah
                val currentOrdersCount = ordersList.size
                if (currentOrdersCount > previousOrdersCount) {
                    // Tampilkan notifikasi bahwa ada order baru
                    showNewOrderNotification("New Order", "A new order has been added!")
                }

                // Perbarui jumlah data sebelumnya
                previousOrdersCount = currentOrdersCount
            }

            override fun onCancelled(error: DatabaseError) {
                // Logika error handling
            }
        })
    }

    private fun deleteOrder(key: String) {
        database.child("orders").child(key).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ordersAdapter.updateOrdersList(emptyList()) // Kosongkan RecyclerView
                showToast("Order deleted successfully")
            } else {
                showToast("Failed to delete order")
            }
        }
    }

    private fun saveOrder(key: String) {
        database.child("orders").child(key).child("status").setValue("Saved")
    }

    private fun deleteallOrders() {
        database.child("orders").removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ordersAdapter.updateOrdersList(emptyList()) // Kosongkan RecyclerView
                showToast("All orders deleted successfully")
            } else {
                showToast("Failed to delete all orders")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Fungsi untuk membuat Notification Channel (hanya untuk Android Oreo ke atas)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "order_notifications",
                "Order Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new orders"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Fungsi untuk menampilkan notifikasi
    private fun showNewOrderNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, "order_notifications")
            .setSmallIcon(R.drawable.ic_notification) // Ganti dengan icon Anda
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }
}
