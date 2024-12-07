package com.example.resellerapp

import android.content.ContentValues
import android.content.ContentResolver
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resellerapp.databinding.ActivitySavedOrderBinding
import com.google.firebase.database.*
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import android.os.Environment
import java.io.OutputStream
import android.content.Intent // Import for starting activities
import com.example.resellerapp.ui.theme.GenerateQRActivity


class SavedOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedOrderBinding
    private lateinit var database: DatabaseReference
    private lateinit var savedOrdersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        // Initialize the adapter with proper deletion logic for saved orders
        savedOrdersAdapter = OrdersAdapter(
            ordersList = emptyList(),
            onDeleteClick = { resellerName, key ->
                // Panggil fungsi untuk menampilkan alert dialog konfirmasi
                showDeleteSavedOrderConfirmationDialog(resellerName, key)
            },
            onSaveClick = { key -> /* No need to save here, just delete */ },
            showSaveButton = false
        )

        binding.rvSavedOrders.apply {
            layoutManager = LinearLayoutManager(this@SavedOrderActivity)
            adapter = savedOrdersAdapter
        }

        observeSavedOrders()

        // Add click listener for the delete-all button
        binding.deleteallButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Add click listener for the export button
        binding.exportButton.setOnClickListener {
            val savedOrdersList = savedOrdersAdapter.getOrdersList()
            exportToExcel(savedOrdersList)  // Call the export method
        }

        // Add click listener for the QR button to navigate to GenerateQRActivity
        binding.generateQrButton.setOnClickListener {
            val intent = Intent(this, GenerateQRActivity::class.java)
            startActivity(intent)
        }

        // Add click listener for the home button to navigate to MainActivity
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeSavedOrders() {
        val savedOrdersRef = database.child("saved")
        savedOrdersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val savedOrdersList = mutableListOf<Order>()
                snapshot.children.forEach { resellerSnapshot ->
                    resellerSnapshot.children.forEach { orderSnapshot ->
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null) {
                            savedOrdersList.add(order.copy(key = orderSnapshot.key ?: ""))
                        }
                    }
                }
                savedOrdersAdapter.updateOrdersList(savedOrdersList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun deleteSavedOrder(resellerName: String, key: String) {
        // Delete from the "saved" node
        database.child("saved").child(resellerName).child(key).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Saved order deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to delete saved order: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun deleteAllSavedOrders() {
        database.child("saved").removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "All saved orders deleted successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to delete all saved orders: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete all saved orders? This action cannot be undone.")
        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteAllSavedOrders() // Call the method to delete all saved orders
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Close dialog without any action
        }
        builder.create().show()
    }

    // Show the dialog for deleting a saved order
    private fun showDeleteSavedOrderConfirmationDialog(resellerName: String, key: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete the saved order from \"$resellerName\"?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteSavedOrder(resellerName, key)  // Hapus saved order setelah konfirmasi
            dialog.dismiss()  // Tutup dialog
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()  // Tutup dialog tanpa tindakan
        }
        builder.create().show()  // Tampilkan dialog
    }

    // Method to export saved orders to Excel
    private fun exportToExcel(savedOrdersList: List<Order>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Saved Orders")

        // Create header row with more descriptive labels
        val headerRow = sheet.createRow(0)
        val headers = arrayOf(
            "Reseller Name",
            "Name",
            "Phone",
            "Item",
            "Down Payment",
            "Address",
            "Timestamp"
        )
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        // Populate data rows
        savedOrdersList.forEachIndexed { rowIndex, order ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(order.resellerName)
            row.createCell(1).setCellValue(order.name)  // Display reseller's name
            row.createCell(2).setCellValue(order.phone.toString())  // Display phone number
            row.createCell(3).setCellValue(order.item) // Using item
            row.createCell(4).setCellValue(order.dp.toString()) // Display down payment
            row.createCell(5).setCellValue(order.address)  // Display address
            row.createCell(6)
                .setCellValue(formatTimestamp(order.timestamp)) // Format timestamp to readable date
        }

        // Save file
        try {
            val contentResolver: ContentResolver = applicationContext.contentResolver

            // For Android 10 and above (Scoped Storage)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "SavedOrders.xlsx")
                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                    )  // Save to Downloads folder
                }

                val uri = contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    contentValues
                )

                if (uri != null) {
                    val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                    outputStream?.use { stream ->
                        workbook.write(stream)
                        Toast.makeText(this, "Excel file saved to Downloads", Toast.LENGTH_LONG)
                            .show()
                    }
                    workbook.close()
                } else {
                    Toast.makeText(this, "Failed to create file in Downloads", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                // For Android 9 and below (Legacy Storage)
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SavedOrders.xlsx"
                )
                val outputStream = FileOutputStream(file)
                workbook.write(outputStream)
                outputStream.close()
                workbook.close()

                Toast.makeText(this, "Excel file saved to Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to create Excel file: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Function to format the timestamp as a readable date and time
    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat =
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        val date = java.util.Date(timestamp)
        return dateFormat.format(date)
    }
}
