package com.example.resellerapp.ui.theme

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resellerapp.R
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.OutputStream
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color


class GenerateQRActivity : AppCompatActivity() {

    private lateinit var qrImageView: ImageView
    private lateinit var resellerNameInput: EditText
    private lateinit var generateQrButton: Button
    private lateinit var saveQrButton: Button
    private var generatedBitmap: Bitmap? = null
    private var currentResellerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_qr)

        qrImageView = findViewById(R.id.qrImageView)
        resellerNameInput = findViewById(R.id.resellerNameInput)
        generateQrButton = findViewById(R.id.generateQrButton)
        saveQrButton = findViewById(R.id.saveQrButton)

        // Generate QR Code
        generateQrButton.setOnClickListener {
            val resellerName = resellerNameInput.text.toString()
            if (resellerName.isNotEmpty()) {
                currentResellerName = resellerName
                generateQrCode(resellerName)
            } else {
                Toast.makeText(this, "Masukkan nama reseller!", Toast.LENGTH_SHORT).show()
            }
        }

        // Save QR Code
        saveQrButton.setOnClickListener {
            if (generatedBitmap != null && currentResellerName.isNotEmpty()) {
                saveQrToGallery(currentResellerName, generatedBitmap!!)
            } else {
                Toast.makeText(this, "QR Code belum dihasilkan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menghasilkan QR Code
    private fun generateQrCode(resellerName: String) {
        val url = "https://rionasari.github.io/reseller-form/?resellerName=$resellerName"

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(url, BarcodeFormat.QR_CODE, 400, 400)
            qrImageView.setImageBitmap(bitmap)
            generatedBitmap = bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal membuat QR Code!", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk menyimpan QR Code ke galeri
    private fun saveQrToGallery(resellerName: String, bitmap: Bitmap) {
        // Tambahkan nama reseller pada gambar QR Code
        val bitmapWithText = addTextToBitmap(bitmap, resellerName)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$resellerName-QR.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ResellerQR")
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                if (bitmapWithText.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    Toast.makeText(this, "QR Code berhasil disimpan!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal menyimpan QR Code!", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Gagal mengakses output stream!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Gagal mengakses galeri!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTextToBitmap(bitmap: Bitmap, text: String): Bitmap {
        // Salin bitmap asli ke bitmap baru yang dapat dimodifikasi
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            isAntiAlias = true
        }

        // Hitung posisi teks agar berada di bawah QR Code
        val textX = 10f
        val textY = mutableBitmap.height - 20f

        canvas.drawText(text, textX, textY, paint)

        return mutableBitmap
    }
}
