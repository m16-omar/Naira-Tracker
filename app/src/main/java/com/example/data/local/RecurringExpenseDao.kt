package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses ORDER BY createdAt DESC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1")
    suspend fun getActiveRecurringExpenses(): List<RecurringExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurringExpense: RecurringExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RecurringExpenseEntity>)

    @Update
    suspend fun update(recurringExpense: RecurringExpenseEntity)

    @Delete
    suspend fun delete(recurringExpense: RecurringExpenseEntity)

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM recurring_expenses")
    suspend fun getCount(): Int
}
