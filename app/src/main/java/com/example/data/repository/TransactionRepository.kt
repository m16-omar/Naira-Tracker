package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.TransactionDao
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun update(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun delete(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteById(id: Long) =
        transactionDao.deleteTransactionById(id)

    suspend fun checkAndSeedIfEmpty() {
        if (transactionDao.getCount() == 0) {
            AppDatabase.seedInitialData(transactionDao)
        }
    }
}
