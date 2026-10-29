package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String, // e.g., "global_2026-10" or "cat_food_2026-10"
    val monthKey: String, // "yyyy-MM", e.g. "2026-10"
    val categoryId: String? = null, // null for global budget
    val amount: Long // Budgeted amount in currency units
)
