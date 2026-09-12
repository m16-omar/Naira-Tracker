package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val startDateMillis: Long,
    val lastProcessedDateMillis: Long = 0L,
    val isActive: Boolean = true,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
