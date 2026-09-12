package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CategoryItem
import com.example.data.model.CategoryPresets
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.RecurringFrequency
import com.example.data.model.ThemeMode
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.BudgetRepository
import com.example.data.repository.RecurringExpenseRepository
import com.example.data.repository.TransactionRepository
import com.example.data.repository.UserSettingsRepository
import com.example.ui.components.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class TransactionFilter(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL("All")
}

data class DailySpending(
    val dateMillis: Long,
    val dayLabel: String,
    val fullDate: String,
    val amount: Double
)

data class CategorySpending(
    val categoryName: String,
    val categoryItem: CategoryItem,
    val amount: Double,
    val percentage: Float
)

data class UiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val currentBalance: Double = 0.0,
    val spentThisMonth: Double = 0.0,
    val incomeThisMonth: Double = 0.0,
    val monthlyBudget: Double = BudgetRepository.DEFAULT_BUDGET,
    val topSpendingCategory: CategorySpending? = null,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val dailySpendingLast30Days: List<DailySpending> = emptyList(),
    val isOverBudget: Boolean = false,
    val budgetPercentage: Float = 0f,
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isAddEditSheetOpen: Boolean = false,
    val editingTransaction: TransactionEntity? = null,
    val preselectedCategory: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = TransactionRepository(database.transactionDao())
    private val budgetRepository = BudgetRepository(application)
    private val recurringRepository = RecurringExpenseRepository(database.recurringExpenseDao(), database.transactionDao())
    private val userSettingsRepository = UserSettingsRepository(application)

    // User Settings
    val themeMode: StateFlow<ThemeMode> = userSettingsRepository.themeMode
    val isBiometricEnabled: StateFlow<Boolean> = userSettingsRepository.isBiometricEnabled

    // App Security Lock state: unlocked if biometric disabled, locked initially if biometric enabled
    private val _isAppUnlocked = MutableStateFlow(!userSettingsRepository.isBiometricEnabled.value)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    // Recurring Expenses
    val allRecurringExpenses: StateFlow<List<RecurringExpenseEntity>> = recurringRepository.allRecurringExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _autoRecordedNotification = MutableStateFlow<String?>(null)
    val autoRecordedNotification: StateFlow<String?> = _autoRecordedNotification.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isAddEditSheetOpen = MutableStateFlow(false)
    val isAddEditSheetOpen = _isAddEditSheetOpen.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction = _editingTransaction.asStateFlow()

    private val _preselectedCategory = MutableStateFlow<String?>(null)
    val preselectedCategory = _preselectedCategory.asStateFlow()

    private val _recentlyDeleted = MutableStateFlow<TransactionEntity?>(null)
    val recentlyDeleted = _recentlyDeleted.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedIfEmpty()
            // Check and auto-record any due recurring expenses (e.g. monthly subscriptions or rent)
            val autoCount = recurringRepository.processDueRecurringExpenses()
            if (autoCount > 0) {
                _autoRecordedNotification.value = "$autoCount recurring expense(s) auto-recorded."
            }
        }
    }

    private val filterState = combine(_selectedFilter, _searchQuery) { filter, search ->
        Pair(filter, search)
    }

    private val sheetUiState = combine(
        _isAddEditSheetOpen,
        _editingTransaction,
        _preselectedCategory
    ) { isOpen, editingTx, preselectedCat ->
        Triple(isOpen, editingTx, preselectedCat)
    }

    val uiState: StateFlow<UiState> = combine(
        repository.allTransactions,
        budgetRepository.monthlyBudget,
        filterState,
        sheetUiState
    ) { allTx, budget, filterPair, sheetTriple ->
        val (filter, search) = filterPair
        val (isSheetOpen, editingTx, preselectedCat) = sheetTriple
        computeUiState(allTx, budget, filter, search, isSheetOpen, editingTx, preselectedCat)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    private fun computeUiState(
        allTx: List<TransactionEntity>,
        budget: Double,
        filter: TransactionFilter,
        search: String,
        isSheetOpen: Boolean,
        editingTx: TransactionEntity?,
        preselectedCat: String?
    ): UiState {
        var totalIncome = 0.0
        var totalExpense = 0.0
        var monthExpense = 0.0
        var monthIncome = 0.0

        val categorySpendingMap = mutableMapOf<String, Double>()

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val txCalendar = Calendar.getInstance()

        allTx.forEach { tx ->
            if (tx.type == TransactionType.INCOME) {
                totalIncome += tx.amount
            } else {
                totalExpense += tx.amount
            }

            txCalendar.timeInMillis = tx.dateMillis
            val isThisMonth = txCalendar.get(Calendar.MONTH) == currentMonth &&
                    txCalendar.get(Calendar.YEAR) == currentYear

            if (isThisMonth) {
                if (tx.type == TransactionType.INCOME) {
                    monthIncome += tx.amount
                } else {
                    monthExpense += tx.amount
                    val existing = categorySpendingMap.getOrDefault(tx.category, 0.0)
                    categorySpendingMap[tx.category] = existing + tx.amount
                }
            }
        }

        val balance = totalIncome - totalExpense
        val isOverBudget = monthExpense > budget
        val budgetPercentage = if (budget > 0) (monthExpense / budget).toFloat().coerceIn(0f, 2f) else 0f

        val breakdownList = categorySpendingMap.map { (catName, amount) ->
            val percentage = if (monthExpense > 0) (amount / monthExpense).toFloat() else 0f
            CategorySpending(
                categoryName = catName,
                categoryItem = CategoryPresets.getCategoryItem(catName, TransactionType.EXPENSE),
                amount = amount,
                percentage = percentage
            )
        }.sortedByDescending { it.amount }

        val topCategory = breakdownList.firstOrNull()

        val dailySpending = computeDailySpendingLast30Days(allTx)

        val filtered = allTx.filter { tx ->
            val matchesFilter = when (filter) {
                TransactionFilter.TODAY -> DateUtils.isToday(tx.dateMillis)
                TransactionFilter.THIS_WEEK -> DateUtils.isThisWeek(tx.dateMillis)
                TransactionFilter.THIS_MONTH -> {
                    txCalendar.timeInMillis = tx.dateMillis
                    txCalendar.get(Calendar.MONTH) == currentMonth && txCalendar.get(Calendar.YEAR) == currentYear
                }
                TransactionFilter.ALL -> true
            }

            val matchesSearch = if (search.isBlank()) {
                true
            } else {
                val query = search.trim().lowercase()
                tx.category.lowercase().contains(query) ||
                        tx.note.lowercase().contains(query) ||
                        tx.amount.toString().contains(query)
            }

            matchesFilter && matchesSearch
        }

        return UiState(
            transactions = allTx,
            filteredTransactions = filtered,
            currentBalance = balance,
            spentThisMonth = monthExpense,
            incomeThisMonth = monthIncome,
            monthlyBudget = budget,
            topSpendingCategory = topCategory,
            categoryBreakdown = breakdownList,
            dailySpendingLast30Days = dailySpending,
            isOverBudget = isOverBudget,
            budgetPercentage = budgetPercentage,
            selectedFilter = filter,
            searchQuery = search,
            isAddEditSheetOpen = isSheetOpen,
            editingTransaction = editingTx,
            preselectedCategory = preselectedCat
        )
    }

    private fun computeDailySpendingLast30Days(transactions: List<TransactionEntity>): List<DailySpending> {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val dailyTotals = mutableMapOf<String, Double>()
        val dateMillisMap = mutableMapOf<String, Long>()
        val fullDateMap = mutableMapOf<String, String>()

        val txCalendar = Calendar.getInstance()

        transactions.forEach { tx ->
            if (tx.type == TransactionType.EXPENSE) {
                txCalendar.timeInMillis = tx.dateMillis
                val key = "${txCalendar.get(Calendar.YEAR)}-${txCalendar.get(Calendar.DAY_OF_YEAR)}"
                dailyTotals[key] = dailyTotals.getOrDefault(key, 0.0) + tx.amount
            }
        }

        val result = mutableListOf<DailySpending>()
        for (i in 29 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)

            val key = "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
            val dayLabel = dayFormat.format(c.time)
            val fullDate = fullDateFormat.format(c.time)
            val amount = dailyTotals.getOrDefault(key, 0.0)

            result.add(
                DailySpending(
                    dateMillis = c.timeInMillis,
                    dayLabel = dayLabel,
                    fullDate = fullDate,
                    amount = amount
                )
            )
        }
        return result
    }

    // UI actions for Transactions
    fun openAddSheet(category: String? = null) {
        _editingTransaction.value = null
        _preselectedCategory.value = category
        _isAddEditSheetOpen.value = true
    }

    fun openEditSheet(transaction: TransactionEntity) {
        _editingTransaction.value = transaction
        _preselectedCategory.value = transaction.category
        _isAddEditSheetOpen.value = true
    }

    fun closeAddEditSheet() {
        _isAddEditSheetOpen.value = false
        _editingTransaction.value = null
        _preselectedCategory.value = null
    }

    fun setFilter(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveTransaction(
        id: Long,
        type: TransactionType,
        amount: Double,
        category: String,
        note: String,
        dateMillis: Long
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                id = id,
                type = type,
                amount = amount,
                category = category,
                note = note,
                dateMillis = dateMillis
            )
            if (id == 0L) {
                repository.insert(transaction)
            } else {
                repository.update(transaction)
            }
            closeAddEditSheet()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            _recentlyDeleted.value = transaction
            repository.delete(transaction)
        }
    }

    fun undoDelete() {
        val deleted = _recentlyDeleted.value ?: return
        viewModelScope.launch {
            repository.insert(deleted.copy(id = 0))
            _recentlyDeleted.value = null
        }
    }

    fun clearRecentlyDeleted() {
        _recentlyDeleted.value = null
    }

    fun updateMonthlyBudget(newBudget: Double) {
        budgetRepository.setMonthlyBudget(newBudget)
    }

    // Recurring Expenses Management
    fun saveRecurringExpense(
        id: Long,
        title: String,
        amount: Double,
        category: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            val item = RecurringExpenseEntity(
                id = id,
                title = title,
                amount = amount,
                category = category,
                frequency = frequency,
                startDateMillis = startDateMillis,
                note = note,
                isActive = true
            )
            if (id == 0L) {
                recurringRepository.insert(item)
            } else {
                recurringRepository.update(item)
            }
        }
    }

    fun deleteRecurringExpense(expense: RecurringExpenseEntity) {
        viewModelScope.launch {
            recurringRepository.delete(expense)
        }
    }

    fun toggleRecurringActive(expense: RecurringExpenseEntity) {
        viewModelScope.launch {
            recurringRepository.toggleActive(expense)
        }
    }

    fun recordRecurringNow(expense: RecurringExpenseEntity) {
        viewModelScope.launch {
            recurringRepository.recordNow(expense)
            _autoRecordedNotification.value = "Recorded installment for ${expense.title}."
        }
    }

    fun clearAutoRecordedNotification() {
        _autoRecordedNotification.value = null
    }

    // User Settings & Theme Mode
    fun setThemeMode(mode: ThemeMode) {
        userSettingsRepository.setThemeMode(mode)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        userSettingsRepository.setBiometricEnabled(enabled)
        if (!enabled) {
            _isAppUnlocked.value = true
        }
    }

    fun unlockApp() {
        _isAppUnlocked.value = true
    }

    fun lockApp() {
        if (isBiometricEnabled.value) {
            _isAppUnlocked.value = false
        }
    }
}
