package com.example.resellerapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

// Adapter class for RecyclerView
class OrdersAdapter(
    private var ordersList: List<Order>,
    private val onDeleteClick: (String, String) -> Unit,
    private val onSaveClick: (String) -> Unit,
    private val showSaveButton: Boolean // This flag determines whether to show the "Save" button
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view, showSaveButton) // Pass showSaveButton to the ViewHolder
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOrdersList(newOrdersList: List<Order>) {
        ordersList = newOrdersList
        notifyDataSetChanged()
    }

    // Method to retrieve the current list of orders
    fun getOrdersList(): List<Order> {
        return ordersList
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = ordersList[position]
        holder.bind(order)

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            val key = order.key
            val resellerName = order.resellerName
            if (key.isNotEmpty() && resellerName.isNotEmpty()) {
                onDeleteClick(resellerName, key)
            }
        }

        // Handle save button click
        holder.saveButton.setOnClickListener {
            val key = order.key
            if (key.isNotEmpty()) {
                onSaveClick(order.key)
            }
        }
    }

    override fun getItemCount(): Int = ordersList.size

    class OrderViewHolder(itemView: View, private val showSaveButton: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val resellerName: TextView = itemView.findViewById(R.id.resellerName)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val addressText: TextView = itemView.findViewById(R.id.addressText)
        private val phoneText: TextView = itemView.findViewById(R.id.phoneText)
        private val itemText: TextView = itemView.findViewById(R.id.itemText)
        private val dpText: TextView = itemView.findViewById(R.id.dpText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val saveButton: ImageView = itemView.findViewById(R.id.saveButton)

        fun bind(order: Order) {
            val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            val dateString = formatter.format(order.timestamp)

            resellerName.text = order.resellerName
            nameText.text = "Nama : ${order.name}"
            addressText.text = "Alamat : ${order.address}"
            phoneText.text = "No.Telepon : ${order.phone}"
            itemText.text = "Barang : ${order.item}"
            dpText.text = "DP : ${order.dp}"
            dateText.text = "Tanggal Pengajuan : $dateString"

            // Show or hide the "Save" button based on the showSaveButton flag
            saveButton.visibility = if (showSaveButton) View.VISIBLE else View.GONE
            deleteButton.visibility = View.VISIBLE
        }
    }
}






