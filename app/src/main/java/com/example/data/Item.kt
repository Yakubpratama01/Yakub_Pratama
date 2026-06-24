package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String,
    val category: String,
    val stockQuantity: Int,
    val minStockQuantity: Int, // threshold for low-stock
    val price: Double,
    val supplier: String,
    val expirationDate: String?, // ISO Format: YYYY-MM-DD
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
