package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Long, // Stored as integer currency units (e.g. 125000 Ar)
    val categoryId: String,
    val categoryName: String,
    val dateMillis: Long,
    val description: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH
)
