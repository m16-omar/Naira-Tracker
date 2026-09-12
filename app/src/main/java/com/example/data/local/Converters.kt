package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.RecurringFrequency
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (e: Exception) {
            TransactionType.EXPENSE
        }
    }

    @TypeConverter
    fun fromRecurringFrequency(frequency: RecurringFrequency): String = frequency.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency {
        return try {
            RecurringFrequency.valueOf(value)
        } catch (e: Exception) {
            RecurringFrequency.MONTHLY
        }
    }
}
