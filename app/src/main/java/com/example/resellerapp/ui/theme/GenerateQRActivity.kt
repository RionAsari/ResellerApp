package com.example.resellerapp.ui.theme

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.resellerapp.R
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class GenerateQRActivity : AppCompatActivity() {

    private lateinit var qrImageView: ImageView
    private lateinit var resellerNameInput: EditText
    private lateinit var generateQrButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_qr)

        qrImageView = findViewById(R.id.qrImageView)
        resellerNameInput = findViewById(R.id.resellerNameInput)
        generateQrButton = findViewById(R.id.generateQrButton)

        // Menangani klik tombol untuk menghasilkan QR Code
        generateQrButton.setOnClickListener {
            val resellerName = resellerNameInput.text.toString()
            if (resellerName.isNotEmpty()) {
                generateQrCode(resellerName)
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
