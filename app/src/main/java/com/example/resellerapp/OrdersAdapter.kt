package com.example.resellerapp

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
            order.key.let { key ->
                if (key.isNotEmpty()) {
                    onDeleteClick(key)  // Pastikan key digunakan untuk penghapusan
                }
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
}


