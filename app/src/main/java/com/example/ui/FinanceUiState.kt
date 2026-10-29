package com.example.ui

import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType

enum class PeriodFilter(val labelFr: String) {
    TODAY("Aujourd'hui"),
    THIS_WEEK("Cette semaine"),
    THIS_MONTH("Ce mois-ci"),
    PREVIOUS_MONTH("Mois précédent"),
    ALL("Toutes les dates")
}

enum class SortOrder(val labelFr: String) {
    DATE_DESC("Plus récent"),
    DATE_ASC("Plus ancien"),
    AMOUNT_DESC("Montant élevé"),
    AMOUNT_ASC("Montant faible")
}

data class CategorySpend(
    val category: CategoryEntity,
    val amount: Long,
    val percentage: Float
)

data class DaySpend(
    val label: String,
    val amount: Long,
    val dateMillis: Long
)

data class FinanceUiState(
    val currentBalance: Long = 0L,
    val initialBalance: Long = 0L,
    val currencySymbol: String = "Ar",
    val currencyCode: String = "MGA",
    val isCurrencyPrefix: Boolean = false,
    val allTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val currentMonthKey: String = "",
    val currentMonthIncome: Long = 0L,
    val currentMonthExpense: Long = 0L,
    val currentMonthSavings: Long = 0L,
    val globalBudget: Long = 0L,
    val budgetSpent: Long = 0L,
    val budgetRemaining: Long = 0L,
    val budgetPercent: Float = 0f,
    val categoryExpenses: List<CategorySpend> = emptyList(),
    val categoryIncomeSources: List<CategorySpend> = emptyList(),
    val budgetsForMonth: List<BudgetEntity> = emptyList(),
    val last7DaysData: List<DaySpend> = emptyList(),
    val periodFilter: PeriodFilter = PeriodFilter.THIS_MONTH,
    val typeFilter: TransactionType? = null,
    val categoryFilterId: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val isPinEnabled: Boolean = false,
    val isPinLocked: Boolean = false,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val lastBackupDate: String? = null,
    val isLoading: Boolean = false,
    val userFeedbackMessage: String? = null
)
