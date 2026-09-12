package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.RecurringFrequency
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, RecurringExpenseEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "naira_tracker.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedInitialData(database.transactionDao(), database.recurringExpenseDao())
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(dao: TransactionDao, recurringDao: RecurringExpenseDao? = null) {
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val seeds = listOf(
                TransactionEntity(
                    type = TransactionType.INCOME,
                    amount = 350000.0,
                    category = "Salary",
                    note = "Monthly salary payment",
                    dateMillis = now - (dayMillis * 3)
                ),
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amount = 4500.0,
                    category = "Food",
                    note = "Jollof Rice & Chicken at Mega Chicken",
                    dateMillis = now
                ),
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amount = 1200.0,
                    category = "Transport",
                    note = "Uber ride from Yaba to Ikeja",
                    dateMillis = now
                ),
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amount = 5000.0,
                    category = "Airtime/Data",
                    note = "MTN 10GB Data Bundle",
                    dateMillis = now - (dayMillis * 1)
                ),
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amount = 18500.0,
                    category = "Bills",
                    note = "IKEDC Electricity token",
                    dateMillis = now - (dayMillis * 2)
                ),
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amount = 14200.0,
                    category = "Shopping",
                    note = "Groceries from Shoprite",
                    dateMillis = now - (dayMillis * 4)
                ),
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amount = 6800.0,
                    category = "Food",
                    note = "Family dinner & Suya",
                    dateMillis = now - (dayMillis * 6)
                ),
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amount = 2500.0,
                    category = "Transport",
                    note = "Fuel for generator",
                    dateMillis = now - (dayMillis * 8)
                ),
                TransactionEntity(
                    type = TransactionType.INCOME,
                    amount = 55000.0,
                    category = "Business Sales",
                    note = "Client website consultation",
                    dateMillis = now - (dayMillis * 10)
                )
            )
            dao.insertAll(seeds)

            val recurringSeeds = listOf(
                RecurringExpenseEntity(
                    title = "Netflix Premium Subscription",
                    amount = 5000.0,
                    category = "Bills",
                    frequency = RecurringFrequency.MONTHLY,
                    startDateMillis = now - (dayMillis * 25),
                    lastProcessedDateMillis = now - (dayMillis * 25),
                    isActive = true,
                    note = "4K streaming family plan"
                ),
                RecurringExpenseEntity(
                    title = "Apartment Rent & Service",
                    amount = 85000.0,
                    category = "Bills",
                    frequency = RecurringFrequency.MONTHLY,
                    startDateMillis = now - (dayMillis * 20),
                    lastProcessedDateMillis = now - (dayMillis * 20),
                    isActive = true,
                    note = "Monthly estate service charge and rent amortization"
                ),
                RecurringExpenseEntity(
                    title = "Gym Membership",
                    amount = 12000.0,
                    category = "Others",
                    frequency = RecurringFrequency.MONTHLY,
                    startDateMillis = now - (dayMillis * 15),
                    lastProcessedDateMillis = now - (dayMillis * 15),
                    isActive = true,
                    note = "Fitness Club Lagos"
                )
            )
            recurringDao?.insertAll(recurringSeeds)
        }
    }
}
