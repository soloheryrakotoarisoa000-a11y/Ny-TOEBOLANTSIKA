package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.DateUtils
import com.example.util.FullBackupData
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FinanceRepository(private val database: AppDatabase) {

    private val txDao = database.transactionDao()
    private val catDao = database.categoryDao()
    private val bgDao = database.budgetDao()
    private val stDao = database.appSettingsDao()

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = txDao.getAll()

    fun getTransactionsByRange(start: Long, end: Long): Flow<List<TransactionEntity>> =
        txDao.getByDateRange(start, end)

    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>> =
        txDao.getRecent(limit)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        txDao.insert(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        txDao.update(transaction)

    suspend fun deleteTransaction(id: Long) =
        txDao.deleteById(id)

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = catDao.getAll()

    suspend fun insertCategory(category: CategoryEntity) =
        catDao.insert(category)

    suspend fun updateCategory(category: CategoryEntity) =
        catDao.update(category)

    suspend fun deleteCategory(id: String) =
        catDao.deleteById(id)

    // Budgets
    fun getBudgetsForMonth(monthKey: String): Flow<List<BudgetEntity>> =
        bgDao.getBudgetsForMonth(monthKey)

    val allBudgets: Flow<List<BudgetEntity>> = bgDao.getAllBudgets()

    suspend fun setBudget(budget: BudgetEntity) =
        bgDao.insert(budget)

    suspend fun deleteBudget(id: String) =
        bgDao.deleteById(id)

    // Settings
    val allSettings: Flow<List<AppSettingsEntity>> = stDao.getAll()

    suspend fun getSetting(key: String): String? =
        stDao.getValue(key)

    fun observeSetting(key: String): Flow<String?> =
        stDao.observeValue(key)

    suspend fun setSetting(key: String, value: String) =
        stDao.set(AppSettingsEntity(key, value))

    // Backup & Restore
    suspend fun restoreBackup(data: FullBackupData) {
        txDao.deleteAll()
        bgDao.deleteAll()
        catDao.deleteAll()

        catDao.insertAll(data.categories)
        txDao.insertAll(data.transactions)
        bgDao.insertAll(data.budgets)
        data.settings.forEach { stDao.set(it) }
    }

    suspend fun clearAllData() {
        txDao.deleteAll()
        bgDao.deleteAll()
        catDao.deleteAll()
        catDao.insertAll(AppDatabase.DEFAULT_CATEGORIES)
        setSetting("initial_balance", "0")
    }

    suspend fun loadDemoData() {
        clearAllData()

        val cal = Calendar.getInstance()
        val currentMonthKey = DateUtils.getCurrentMonthKey()

        // Set global monthly budget to 1 500 000 Ar
        setBudget(BudgetEntity("global_$currentMonthKey", currentMonthKey, null, 1500000L))
        setBudget(BudgetEntity("cat_food_$currentMonthKey", currentMonthKey, "food", 450000L))
        setBudget(BudgetEntity("cat_transport_$currentMonthKey", currentMonthKey, "transport", 120000L))
        setBudget(BudgetEntity("cat_utilities_$currentMonthKey", currentMonthKey, "utilities", 180000L))

        // Initial balance
        setSetting("initial_balance", "250000")

        // Demo transactions spanning recent days
        val demoTransactions = listOf(
            TransactionEntity(
                type = TransactionType.INCOME,
                amount = 1800000L,
                categoryId = "salary",
                categoryName = "Salaire",
                dateMillis = cal.timeInMillis - (12 * 86400000L),
                description = "Virement salaire mensuel",
                paymentMethod = PaymentMethod.BANK
            ),
            TransactionEntity(
                type = TransactionType.INCOME,
                amount = 250000L,
                categoryId = "freelance",
                categoryName = "Activité indépendante",
                dateMillis = cal.timeInMillis - (7 * 86400000L),
                description = "Mission conseil freelance",
                paymentMethod = PaymentMethod.MOBILE_MONEY
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 165000L,
                categoryId = "food",
                categoryName = "Alimentation",
                dateMillis = cal.timeInMillis - (5 * 86400000L),
                description = "Courses au supermarché et marché local",
                paymentMethod = PaymentMethod.CASH
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 95000L,
                categoryId = "utilities",
                categoryName = "Électricité et eau",
                dateMillis = cal.timeInMillis - (4 * 86400000L),
                description = "Facture JIRAMA électricité",
                paymentMethod = PaymentMethod.MOBILE_MONEY
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 45000L,
                categoryId = "transport",
                categoryName = "Transport",
                dateMillis = cal.timeInMillis - (3 * 86400000L),
                description = "Carburant et taxi",
                paymentMethod = PaymentMethod.CASH
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 60000L,
                categoryId = "telecom",
                categoryName = "Téléphone et Internet",
                dateMillis = cal.timeInMillis - (2 * 86400000L),
                description = "Recharge forfait Internet 4G",
                paymentMethod = PaymentMethod.MOBILE_MONEY
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 75000L,
                categoryId = "food",
                categoryName = "Alimentation",
                dateMillis = cal.timeInMillis - (1 * 86400000L),
                description = "Dîner restaurant en famille",
                paymentMethod = PaymentMethod.CASH
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 30000L,
                categoryId = "church_gifts",
                categoryName = "Église et dons",
                dateMillis = cal.timeInMillis - (8 * 3600000L),
                description = "Offrande et soutien communautaire",
                paymentMethod = PaymentMethod.CASH
            )
        )

        demoTransactions.forEach { txDao.insert(it) }
    }
}
