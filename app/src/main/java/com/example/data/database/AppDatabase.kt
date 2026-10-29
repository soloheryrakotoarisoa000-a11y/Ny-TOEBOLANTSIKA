package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppSettingsDao
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.TransactionDao
import com.example.data.model.AppSettingsEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ny_toebolantsika.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_CATEGORIES = listOf(
            // Expense Categories
            CategoryEntity("food", "Alimentation", TransactionType.EXPENSE, "restaurant", "#E0533C", true),
            CategoryEntity("transport", "Transport", TransactionType.EXPENSE, "directions_bus", "#3B82F6", true),
            CategoryEntity("housing", "Logement", TransactionType.EXPENSE, "home", "#8B5CF6", true),
            CategoryEntity("utilities", "Électricité et eau", TransactionType.EXPENSE, "bolt", "#F59E0B", true),
            CategoryEntity("telecom", "Téléphone et Internet", TransactionType.EXPENSE, "wifi", "#06B6D4", true),
            CategoryEntity("health", "Santé", TransactionType.EXPENSE, "medical_services", "#EC4899", true),
            CategoryEntity("education", "Éducation", TransactionType.EXPENSE, "school", "#10B981", true),
            CategoryEntity("clothing", "Habillement", TransactionType.EXPENSE, "checkroom", "#F43F5E", true),
            CategoryEntity("family", "Famille", TransactionType.EXPENSE, "groups", "#6366F1", true),
            CategoryEntity("leisure", "Loisirs", TransactionType.EXPENSE, "sports_esports", "#A855F7", true),
            CategoryEntity("church_gifts", "Église et dons", TransactionType.EXPENSE, "volunteer_activism", "#E11D48", true),
            CategoryEntity("savings", "Épargne", TransactionType.EXPENSE, "savings", "#0B6646", true),
            CategoryEntity("other_expense", "Autres", TransactionType.EXPENSE, "category", "#64748B", true),

            // Income Categories
            CategoryEntity("salary", "Salaire", TransactionType.INCOME, "payments", "#16A34A", true),
            CategoryEntity("commerce", "Activité commerciale", TransactionType.INCOME, "storefront", "#0D9488", true),
            CategoryEntity("freelance", "Activité indépendante", TransactionType.INCOME, "work", "#2563EB", true),
            CategoryEntity("donations", "Dons et aides", TransactionType.INCOME, "card_giftcard", "#D97706", true),
            CategoryEntity("other_income", "Autres revenus", TransactionType.INCOME, "monetization_on", "#059669", true)
        )
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    database.categoryDao().insertAll(DEFAULT_CATEGORIES)
                    // Default currency Malagasy Ariary (MGA)
                    database.appSettingsDao().set(AppSettingsEntity("currency_code", "MGA"))
                    database.appSettingsDao().set(AppSettingsEntity("currency_symbol", "Ar"))
                    database.appSettingsDao().set(AppSettingsEntity("initial_balance", "0"))
                    database.appSettingsDao().set(AppSettingsEntity("pin_enabled", "false"))
                }
            }
        }
    }
}
