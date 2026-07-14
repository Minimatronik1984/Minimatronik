package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val invoiceNumber: String,
    val amountRsd: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false
)
