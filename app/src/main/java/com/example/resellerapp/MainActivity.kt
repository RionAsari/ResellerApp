package com.example.resellerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resellerapp.databinding.ActivityMainBinding
import com.example.resellerapp.ui.theme.GenerateQRActivity
import com.google.firebase.database.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.*
import android.content.ContentValues
import android.content.ContentResolver
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Firebase reference
        database = FirebaseDatabase.getInstance().reference

        // Setup RecyclerView Adapter
        ordersAdapter = OrdersAdapter(
            ordersList = emptyList(),
            onDeleteClick = { resellerName, key -> showDeleteOrderConfirmationDialog(resellerName, key) },
            onSaveClick = { key -> saveOrderAsSaved(key) },
            showSaveButton = true
        )

        // Setup RecyclerView
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ordersAdapter
        }

        observeOrders()

        // Event tombol untuk generate QR
        binding.generateQrButton.setOnClickListener {
            val intent = Intent(this, GenerateQRActivity::class.java)
            startActivity(intent)
        }

        // Event tombol untuk saved orders
        binding.ivLead.setOnClickListener {
            val intent = Intent(this, SavedOrderActivity::class.java)
            startActivity(intent)
        }

        // Event tombol delete semua orders
        binding.deleteallButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Event tombol untuk export data ke Excel
        binding.exportButton.setOnClickListener {
            val ordersList = ordersAdapter.getOrdersList()
            exportToExcel(ordersList)  // Panggil metode ekspor ke Excel
        }
    }

    // Observe perubahan data dari Firebase
    private fun observeOrders() {
        val ordersRef = database.child("orders")
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ordersList = mutableListOf<Order>()
                snapshot.children.forEach { orderSnapshot ->
                    val order = orderSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        ordersList.add(order.copy(key = orderSnapshot.key ?: ""))
                    }
                }
                // Update Adapter dengan data terbaru
                ordersAdapter.updateOrdersList(ordersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to fetch orders: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Hapus satu order
    private fun deleteOrder(resellerName: String, key: String) {
        database.child("orders").child(key).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Order from $resellerName deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Simpan order ke saved orders
    private fun saveOrderAsSaved(key: String) {
        val orderRef = database.child("orders").child(key)
        orderRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val order = dataSnapshot.getValue(Order::class.java)
                order?.let {
                    database.child("saved").child(it.resellerName).child(key).setValue(it)
                        .addOnSuccessListener {
                            orderRef.removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Order saved successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to remove order: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save order: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to retrieve order: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Hapus semua order
    private fun deleteAllOrders() {
        database.child("orders").removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "All orders deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Dialog konfirmasi hapus semua order
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete all orders? This action cannot be undone.")
        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteAllOrders()
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    // Dialog konfirmasi hapus satu order
    private fun showDeleteOrderConfirmationDialog(resellerName: String, key: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete the order from \"$resellerName\"?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteOrder(resellerName, key)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    // Method untuk ekspor data orders ke Excel
    private fun exportToExcel(ordersList: List<Order>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Orders")

        // Create header row
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("Reseller Name", "Name", "Phone", "Item", "Down Payment", "Address", "Timestamp")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        // Populate data rows
        ordersList.forEachIndexed { rowIndex, order ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(order.resellerName)
            row.createCell(1).setCellValue(order.name)  // Display reseller's name
            row.createCell(2).setCellValue(order.phone.toString())  // Display phone number
            row.createCell(3).setCellValue(order.item) // Display item
            row.createCell(4).setCellValue(order.dp.toString()) // Display down payment
            row.createCell(5).setCellValue(order.address)  // Display address
            row.createCell(6).setCellValue(formatTimestamp(order.timestamp)) // Format timestamp to readable date
        }

        // Save file
        try {
            val contentResolver: ContentResolver = applicationContext.contentResolver

            // For Android 10 and above (Scoped Storage)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "Orders.xlsx")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)  // Save to Downloads folder
                }

                val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

                if (uri != null) {
                    val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                    outputStream?.use { stream ->
                        workbook.write(stream)
                        Toast.makeText(this, "Excel file saved to Downloads", Toast.LENGTH_LONG).show()
                    }
                    workbook.close()
                } else {
                    Toast.makeText(this, "Failed to create file in Downloads", Toast.LENGTH_SHORT).show()
                }
            } else {
                // For Android 9 and below (Legacy Storage)
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Orders.xlsx")
                val outputStream = FileOutputStream(file)
                workbook.write(outputStream)
                outputStream.close()
                Toast.makeText(this, "Excel file saved to Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error exporting data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Helper method to format timestamp
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
