/*package com.example.resellerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for RecyclerView
class OrdersAdapter(
    private val ordersList: List<Order>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = ordersList[position]
        holder.bind(order)
        holder.deleteButton.setOnClickListener {
            val key = order.key // Dapatkan key dari Order
            if (key.isNotEmpty()) {
                onDeleteClick(key)  // Kirimkan key untuk penghapusan
            }
        }
    }

    override fun getItemCount(): Int = ordersList.size

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resellerName: TextView = itemView.findViewById(R.id.resellerName)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(order: Order) {
            resellerName.text = order.resellerName
        }
    }
}*/

/*package com.example.resellerapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for RecyclerView
class OrdersAdapter(
    private val ordersList: List<Order>,
    private val onDeleteClick: (String) -> Unit,
    private val onSaveClick: (String) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOrdersList(newOrdersList: List<Order>) {
        (ordersList as MutableList).clear()
        ordersList.addAll(newOrdersList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = ordersList[position]
        holder.bind(order)

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            val key = order.key // Get the key from Order
            if (key.isNotEmpty()) {
                onDeleteClick(key) // Pass the key for deletion
            }
        }

        // Handle save button click
        holder.saveButton.setOnClickListener {
            val key = order.key // Get the key from Order
            if (key.isNotEmpty()) {
                onSaveClick(key) // Pass the key for saving
            }
        }
    }

    override fun getItemCount(): Int = ordersList.size

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resellerName: TextView = itemView.findViewById(R.id.resellerName)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val saveButton: ImageView = itemView.findViewById(R.id.saveButton)

        fun bind(order: Order) {
            resellerName.text = order.resellerName
        }
    }
}*/

package com.example.resellerapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.resellerapp.Order
import com.example.resellerapp.MainActivity

// Adapter class for RecyclerView
class OrdersAdapter(
    private var ordersList: List<Order>,
    private val onDeleteClick: (String) -> Unit,
    private val onSaveClick: (String) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOrdersList(newOrdersList: List<Order>) {
        ordersList = newOrdersList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = ordersList[position]
        holder.bind(order)

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            val key = order.key // Get the key from Order
            if (key.isNotEmpty()) {
                onDeleteClick(key) // Pass the key for deletion
            }
        }

        // Handle save button click
        holder.saveButton.setOnClickListener {
            val key = order.key // Get the key from Order
            if (key.isNotEmpty()) {
                onSaveClick(key) // Pass the key for saving
            }
        }
    }

    override fun getItemCount(): Int = ordersList.size

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resellerName: TextView = itemView.findViewById(R.id.resellerName)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val addressText: TextView = itemView.findViewById(R.id.addressText)
        private val phoneText: TextView = itemView.findViewById(R.id.phoneText)
        private val itemText: TextView = itemView.findViewById(R.id.itemText)
        private val dpText: TextView = itemView.findViewById(R.id.dpText)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val saveButton: ImageView = itemView.findViewById(R.id.saveButton)

        fun bind(order: Order) {
            resellerName.text = order.resellerName
            nameText.text = "Nama : ${order.name}"
            addressText.text = "Alamat : ${order.address}"
            phoneText.text = "No.Telepon : ${order.phone}"
            itemText.text = "Barang : ${order.item}"
            dpText.text = "DP : ${order.dp}"
        }
    }
}
