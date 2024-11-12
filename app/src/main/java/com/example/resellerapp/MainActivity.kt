package com.example.resellerapp

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.example.resellerapp.Order
import com.example.resellerapp.OrdersAdapter // Ensure this exists

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var qrImageView: ImageView
    private lateinit var generateQRCodeButton: Button
    private lateinit var ordersList: MutableList<Order>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance().reference

        // Initialize views
        recyclerView = findViewById(R.id.ordersRecyclerView)
        qrImageView = findViewById(R.id.qrImageView)
        generateQRCodeButton = findViewById(R.id.generateQRCodeButton)

        // Setup RecyclerView
        ordersList = mutableListOf()
        val ordersAdapter = OrdersAdapter(ordersList) { orderId -> deleteOrder(orderId) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ordersAdapter

        // Fetch data from Firebase
        fetchOrders()

        // Handle QR code generation
        generateQRCodeButton.setOnClickListener {
            val resellerId = "OB1q_NIW8W_PUS0LeNM"  // Example reseller ID
            val resellerName = "Rusdi"  // Example reseller name
            generateQRCode(resellerId, resellerName)
        }
    }

    private fun fetchOrders() {
        Log.d("MainActivity", "Fetching orders from Firebase")

        database.child("orders").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ordersList.clear()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        order.key = dataSnapshot.key ?: ""  // Simpan key di dalam model
                        ordersList.add(order)
                    }
                }
                recyclerView.adapter?.notifyDataSetChanged()
                Log.d("MainActivity", "Fetched ${ordersList.size} orders")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Firebase fetch failed: ${error.message}")
            }
        })
    }


    private fun deleteOrder(orderId: String) {
        // Log before deleting order
        Log.d("MainActivity", "Deleting order with ID: $orderId")

        database.child("orders").child(orderId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Order deleted", Toast.LENGTH_SHORT).show()
                fetchOrders()  // Refresh the list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete order", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Failed to delete order with ID: $orderId")
            }
    }

    private fun generateQRCode(resellerId: String, resellerName: String) {
        val url = "https://rionasari.github.io/reseller-form/?id_reseller=$resellerId&reseller_name=$resellerName"
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            qrImageView.setImageBitmap(bmp)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MainActivity", "QR Code generation failed", e)
        }
    }
}
