package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.util.CsvExportHelper
import com.example.util.DateUtils
import com.example.util.FullBackupData
import com.example.util.JsonBackupHelper
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState(currentMonthKey = DateUtils.getCurrentMonthKey()))
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private val currentMonthKey = DateUtils.getCurrentMonthKey()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.allTransactions,
                repository.allCategories,
                repository.getBudgetsForMonth(currentMonthKey),
                repository.allSettings
            ) { transactions, categories, budgets, settings ->
                recalculateState(transactions, categories, budgets, settings)
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun recalculateState(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        budgets: List<BudgetEntity>,
        settings: List<com.example.data.model.AppSettingsEntity>
    ): FinanceUiState {
        val currentState = _uiState.value

        val settingsMap = settings.associate { it.key to it.value }
        val currencySymbol = settingsMap["currency_symbol"] ?: "Ar"
        val currencyCode = settingsMap["currency_code"] ?: "MGA"
        val isPrefix = settingsMap["currency_prefix"] == "true"
        val initialBalance = settingsMap["initial_balance"]?.toLongOrNull() ?: 0L
        val pinEnabled = settingsMap["pin_enabled"] == "true"
        val pinHash = settingsMap["pin_hash"] ?: ""
        val themeMode = settingsMap["theme_mode"] ?: "SYSTEM"
        val lastBackupDate = settingsMap["last_backup_date"]

        // Total calculations
        var totalIncomeAllTime = 0L
        var totalExpenseAllTime = 0L

        val monthStart = DateUtils.getStartOfMonth()
        val monthEnd = DateUtils.getEndOfMonth()

        var currentMonthIncome = 0L
        var currentMonthExpense = 0L

        val categoryExpenseMap = mutableMapOf<String, Long>()
        val categoryIncomeMap = mutableMapOf<String, Long>()

        transactions.forEach { tx ->
            if (tx.type == TransactionType.INCOME) {
                totalIncomeAllTime += tx.amount
                if (tx.dateMillis in monthStart..monthEnd) {
                    currentMonthIncome += tx.amount
                    categoryIncomeMap[tx.categoryId] = (categoryIncomeMap[tx.categoryId] ?: 0L) + tx.amount
                }
            } else {
                totalExpenseAllTime += tx.amount
                if (tx.dateMillis in monthStart..monthEnd) {
                    currentMonthExpense += tx.amount
                    categoryExpenseMap[tx.categoryId] = (categoryExpenseMap[tx.categoryId] ?: 0L) + tx.amount
                }
            }
        }

        val currentBalance = initialBalance + totalIncomeAllTime - totalExpenseAllTime
        val currentMonthSavings = currentMonthIncome - currentMonthExpense

        // Budget calculations
        val globalBudgetId = "global_$currentMonthKey"
        val globalBudgetEntity = budgets.find { it.id == globalBudgetId }
        val globalBudget = globalBudgetEntity?.amount ?: 0L
        val budgetSpent = currentMonthExpense
        val budgetRemaining = if (globalBudget > 0) globalBudget - budgetSpent else 0L
        val budgetPercent = if (globalBudget > 0) {
            (budgetSpent.toFloat() / globalBudget.toFloat()) * 100f
        } else 0f

        // Category breakdown lists
        val catMap = categories.associateBy { it.id }
        val categoryExpensesList = categoryExpenseMap.mapNotNull { (catId, amt) ->
            val cat = catMap[catId] ?: CategoryEntity(catId, catId, TransactionType.EXPENSE, "category", "#64748B")
            val pct = if (currentMonthExpense > 0) (amt.toFloat() / currentMonthExpense.toFloat()) * 100f else 0f
            CategorySpend(cat, amt, pct)
        }.sortedByDescending { it.amount }

        val categoryIncomeList = categoryIncomeMap.mapNotNull { (catId, amt) ->
            val cat = catMap[catId] ?: CategoryEntity(catId, catId, TransactionType.INCOME, "category", "#059669")
            val pct = if (currentMonthIncome > 0) (amt.toFloat() / currentMonthIncome.toFloat()) * 100f else 0f
            CategorySpend(cat, amt, pct)
        }.sortedByDescending { it.amount }

        // Last 7 days chart data
        val sevenDaysRanges = DateUtils.getLast7DaysDays()
        val last7DaysData = sevenDaysRanges.map { (dayStart, dayEnd, label) ->
            val daySpent = transactions.filter {
                it.type == TransactionType.EXPENSE && it.dateMillis in dayStart..dayEnd
            }.sumOf { it.amount }
            DaySpend(label, daySpent, dayStart)
        }

        // Recent 5 transactions
        val recentTransactions = transactions.take(5)

        // Filtered transactions for History tab
        val filtered = filterTransactions(
            transactions,
            currentState.periodFilter,
            currentState.typeFilter,
            currentState.categoryFilterId,
            currentState.searchQuery,
            currentState.sortOrder
        )

        val isLocked = if (pinEnabled && currentState.isPinLocked) true else (pinEnabled && !currentState.isPinLocked && pinHash.isNotEmpty() && currentState.isPinEnabled != pinEnabled)

        return currentState.copy(
            currentBalance = currentBalance,
            initialBalance = initialBalance,
            currencySymbol = currencySymbol,
            currencyCode = currencyCode,
            isCurrencyPrefix = isPrefix,
            allTransactions = transactions,
            filteredTransactions = filtered,
            recentTransactions = recentTransactions,
            categories = categories,
            currentMonthKey = currentMonthKey,
            currentMonthIncome = currentMonthIncome,
            currentMonthExpense = currentMonthExpense,
            currentMonthSavings = currentMonthSavings,
            globalBudget = globalBudget,
            budgetSpent = budgetSpent,
            budgetRemaining = budgetRemaining,
            budgetPercent = budgetPercent,
            categoryExpenses = categoryExpensesList,
            categoryIncomeSources = categoryIncomeList,
            budgetsForMonth = budgets,
            last7DaysData = last7DaysData,
            isPinEnabled = pinEnabled,
            isPinLocked = isLocked,
            themeMode = themeMode,
            lastBackupDate = lastBackupDate
        )
    }

    private fun filterTransactions(
        transactions: List<TransactionEntity>,
        period: PeriodFilter,
        type: TransactionType?,
        categoryId: String?,
        searchQuery: String,
        sortOrder: SortOrder
    ): List<TransactionEntity> {
        val now = Calendar.getInstance()
        val (rangeStart, rangeEnd) = when (period) {
            PeriodFilter.TODAY -> Pair(DateUtils.getStartOfToday(), DateUtils.getEndOfToday())
            PeriodFilter.THIS_WEEK -> Pair(DateUtils.getStartOfWeek(), Long.MAX_VALUE)
            PeriodFilter.THIS_MONTH -> Pair(DateUtils.getStartOfMonth(now), DateUtils.getEndOfMonth(now))
            PeriodFilter.PREVIOUS_MONTH -> DateUtils.getPreviousMonthRange()
            PeriodFilter.ALL -> Pair(0L, Long.MAX_VALUE)
        }

        var result = transactions.filter { tx ->
            tx.dateMillis in rangeStart..rangeEnd &&
                    (type == null || tx.type == type) &&
                    (categoryId == null || tx.categoryId == categoryId) &&
                    (searchQuery.isBlank() ||
                            tx.description.contains(searchQuery, ignoreCase = true) ||
                            tx.categoryName.contains(searchQuery, ignoreCase = true))
        }

        result = when (sortOrder) {
            SortOrder.DATE_DESC -> result.sortedByDescending { it.dateMillis }
            SortOrder.DATE_ASC -> result.sortedBy { it.dateMillis }
            SortOrder.AMOUNT_DESC -> result.sortedByDescending { it.amount }
            SortOrder.AMOUNT_ASC -> result.sortedBy { it.amount }
        }

        return result
    }

    // Filter controls
    fun setPeriodFilter(filter: PeriodFilter) {
        _uiState.update { state ->
            val filtered = filterTransactions(
                state.allTransactions, filter, state.typeFilter, state.categoryFilterId, state.searchQuery, state.sortOrder
            )
            state.copy(periodFilter = filter, filteredTransactions = filtered)
        }
    }

    fun setTypeFilter(type: TransactionType?) {
        _uiState.update { state ->
            val filtered = filterTransactions(
                state.allTransactions, state.periodFilter, type, state.categoryFilterId, state.searchQuery, state.sortOrder
            )
            state.copy(typeFilter = type, filteredTransactions = filtered)
        }
    }

    fun setCategoryFilter(categoryId: String?) {
        _uiState.update { state ->
            val filtered = filterTransactions(
                state.allTransactions, state.periodFilter, state.typeFilter, categoryId, state.searchQuery, state.sortOrder
            )
            state.copy(categoryFilterId = categoryId, filteredTransactions = filtered)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = filterTransactions(
                state.allTransactions, state.periodFilter, state.typeFilter, state.categoryFilterId, query, state.sortOrder
            )
            state.copy(searchQuery = query, filteredTransactions = filtered)
        }
    }

    fun setSortOrder(sort: SortOrder) {
        _uiState.update { state ->
            val filtered = filterTransactions(
                state.allTransactions, state.periodFilter, state.typeFilter, state.categoryFilterId, state.searchQuery, sort
            )
            state.copy(sortOrder = sort, filteredTransactions = filtered)
        }
    }

    // Transaction CRUD
    fun addTransaction(
        type: TransactionType,
        amount: Long,
        category: CategoryEntity,
        dateMillis: Long,
        description: String,
        paymentMethod: PaymentMethod
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    type = type,
                    amount = amount,
                    categoryId = category.id,
                    categoryName = category.name,
                    dateMillis = dateMillis,
                    description = description.trim(),
                    paymentMethod = paymentMethod
                )
            )
        }
    }

    fun updateTransaction(
        id: Long,
        type: TransactionType,
        amount: Long,
        category: CategoryEntity,
        dateMillis: Long,
        description: String,
        paymentMethod: PaymentMethod
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                TransactionEntity(
                    id = id,
                    type = type,
                    amount = amount,
                    categoryId = category.id,
                    categoryName = category.name,
                    dateMillis = dateMillis,
                    description = description.trim(),
                    paymentMethod = paymentMethod
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    // Category CRUD
    fun addCategory(name: String, type: TransactionType, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val id = "cat_" + System.currentTimeMillis()
            repository.insertCategory(
                CategoryEntity(
                    id = id,
                    name = name.trim(),
                    type = type,
                    iconName = iconName,
                    colorHex = colorHex,
                    isDefault = false
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    // Budgets
    fun setGlobalBudget(amount: Long) {
        viewModelScope.launch {
            val id = "global_$currentMonthKey"
            repository.setBudget(
                BudgetEntity(id = id, monthKey = currentMonthKey, categoryId = null, amount = amount)
            )
        }
    }

    fun setCategoryBudget(categoryId: String, amount: Long) {
        viewModelScope.launch {
            val id = "cat_${categoryId}_$currentMonthKey"
            repository.setBudget(
                BudgetEntity(id = id, monthKey = currentMonthKey, categoryId = categoryId, amount = amount)
            )
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    // Settings
    fun setInitialBalance(amount: Long) {
        viewModelScope.launch {
            repository.setSetting("initial_balance", amount.toString())
        }
    }

    fun setCurrency(code: String, symbol: String, isPrefix: Boolean = false) {
        viewModelScope.launch {
            repository.setSetting("currency_code", code)
            repository.setSetting("currency_symbol", symbol)
            repository.setSetting("currency_prefix", isPrefix.toString())
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setSetting("theme_mode", mode)
        }
    }

    // PIN Security
    fun setPin(pin: String) {
        viewModelScope.launch {
            val hash = SecurityUtils.hashPin(pin)
            repository.setSetting("pin_hash", hash)
            repository.setSetting("pin_enabled", "true")
            _uiState.update { it.copy(isPinEnabled = true, isPinLocked = false) }
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            repository.setSetting("pin_enabled", "false")
            repository.setSetting("pin_hash", "")
            _uiState.update { it.copy(isPinEnabled = false, isPinLocked = false) }
        }
    }

    fun unlockApp(pin: String): Boolean {
        var success = false
        val state = _uiState.value
        viewModelScope.launch {
            val storedHash = repository.getSetting("pin_hash") ?: ""
            if (SecurityUtils.verifyPin(pin, storedHash)) {
                _uiState.update { it.copy(isPinLocked = false) }
                success = true
            }
        }
        return success
    }

    fun lockApp() {
        if (_uiState.value.isPinEnabled) {
            _uiState.update { it.copy(isPinLocked = true) }
        }
    }

    // Demo Data & Reset
    fun loadDemoData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.loadDemoData()
            _uiState.update { it.copy(isLoading = false, userFeedbackMessage = "Données de démonstration chargées avec succès !") }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.clearAllData()
            _uiState.update { it.copy(isLoading = false, userFeedbackMessage = "Toutes les données ont été réinitialisées.") }
        }
    }

    // JSON Backup & CSV Export
    fun exportBackupJson(): String {
        val state = _uiState.value
        val fullData = FullBackupData(
            exportTimestamp = System.currentTimeMillis(),
            appVersion = "1.0",
            transactions = state.allTransactions,
            categories = state.categories,
            budgets = state.budgetsForMonth,
            settings = emptyList() // Safe settings only
        )
        val json = JsonBackupHelper.serialize(fullData)
        val dateLabel = DateUtils.formatDateTime(System.currentTimeMillis())
        viewModelScope.launch {
            repository.setSetting("last_backup_date", dateLabel)
        }
        return json
    }

    fun restoreBackupJson(jsonString: String): Boolean {
        val backup = JsonBackupHelper.deserialize(jsonString) ?: return false
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.restoreBackup(backup)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userFeedbackMessage = "Sauvegarde restaurée avec succès (${backup.transactions.size} opérations) !"
                )
            }
        }
        return true
    }

    fun exportCsv(): String {
        val state = _uiState.value
        return CsvExportHelper.generateCsv(state.allTransactions, state.currencySymbol)
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(userFeedbackMessage = null) }
    }
}
