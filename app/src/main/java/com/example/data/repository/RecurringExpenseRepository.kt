package com.example.data.repository

import com.example.data.local.RecurringExpenseDao
import com.example.data.local.TransactionDao
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.RecurringFrequency
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class RecurringExpenseRepository(
    private val recurringDao: RecurringExpenseDao,
    private val transactionDao: TransactionDao
) {
    val allRecurringExpenses: Flow<List<RecurringExpenseEntity>> = recurringDao.getAllRecurringExpenses()

    suspend fun insert(recurringExpense: RecurringExpenseEntity): Long {
        return recurringDao.insert(recurringExpense)
    }

    suspend fun update(recurringExpense: RecurringExpenseEntity) {
        recurringDao.update(recurringExpense)
    }

    suspend fun delete(recurringExpense: RecurringExpenseEntity) {
        recurringDao.delete(recurringExpense)
    }

    suspend fun toggleActive(expense: RecurringExpenseEntity) {
        recurringDao.update(expense.copy(isActive = !expense.isActive))
    }

    /**
     * Checks all active recurring expenses and automatically logs overdue transactions
     * into the transactions ledger.
     * Returns the count of transactions auto-recorded.
     */
    suspend fun processDueRecurringExpenses(): Int {
        val now = System.currentTimeMillis()
        val activeExpenses = recurringDao.getActiveRecurringExpenses()
        var recordedCount = 0

        for (expense in activeExpenses) {
            val baseTime = if (expense.lastProcessedDateMillis > 0) expense.lastProcessedDateMillis else expense.startDateMillis
            var nextDue = calculateNextDueDate(baseTime, expense.frequency)

            // If due now or in the past, process and record
            if (nextDue <= now) {
                var lastDue = nextDue
                while (lastDue <= now) {
                    val tx = TransactionEntity(
                        type = TransactionType.EXPENSE,
                        amount = expense.amount,
                        category = expense.category,
                        note = if (expense.note.isNotBlank()) "Recurring: ${expense.title} (${expense.note})" else "Recurring: ${expense.title}",
                        dateMillis = lastDue
                    )
                    transactionDao.insert(tx)
                    recordedCount++

                    val advance = calculateNextDueDate(lastDue, expense.frequency)
                    if (advance <= lastDue) break
                    lastDue = advance
                    // Cap at 12 to avoid runaway if long ago
                    if (recordedCount > 12) break
                }
                recurringDao.update(expense.copy(lastProcessedDateMillis = lastDue))
            }
        }
        return recordedCount
    }

    /**
     * Manually triggers a single immediate recording for a recurring item.
     */
    suspend fun recordNow(expense: RecurringExpenseEntity): Long {
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            type = TransactionType.EXPENSE,
            amount = expense.amount,
            category = expense.category,
            note = if (expense.note.isNotBlank()) "Recurring: ${expense.title} (${expense.note})" else "Recurring: ${expense.title}",
            dateMillis = now
        )
        val id = transactionDao.insert(tx)
        recurringDao.update(expense.copy(lastProcessedDateMillis = now))
        return id
    }

    companion object {
        fun calculateNextDueDate(fromMillis: Long, frequency: RecurringFrequency): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = fromMillis
            }
            when (frequency) {
                RecurringFrequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                RecurringFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                RecurringFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                RecurringFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
            }
            return calendar.timeInMillis
        }
    }
}
