package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("naira_tracker_budget_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MONTHLY_BUDGET = "monthly_budget_amount"
        const val DEFAULT_BUDGET = 200000.0 // ₦200,000 default monthly budget
    }

    private val _monthlyBudget = MutableStateFlow(getSavedBudget())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private fun getSavedBudget(): Double {
        val saved = prefs.getString(KEY_MONTHLY_BUDGET, null)
        return saved?.toDoubleOrNull() ?: DEFAULT_BUDGET
    }

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putString(KEY_MONTHLY_BUDGET, amount.toString()).apply()
        _monthlyBudget.value = amount
    }
}
