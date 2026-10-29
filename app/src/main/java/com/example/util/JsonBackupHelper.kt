package com.example.util

import com.example.data.model.AppSettingsEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject

data class FullBackupData(
    val exportTimestamp: Long,
    val appVersion: String,
    val transactions: List<TransactionEntity>,
    val categories: List<CategoryEntity>,
    val budgets: List<BudgetEntity>,
    val settings: List<AppSettingsEntity>
)

object JsonBackupHelper {

    fun serialize(backup: FullBackupData): String {
        val root = JSONObject()
        root.put("app", "NY_TOEBOLANTSIKA")
        root.put("schemaVersion", 1)
        root.put("exportTimestamp", backup.exportTimestamp)
        root.put("appVersion", backup.appVersion)

        // Transactions
        val txArray = JSONArray()
        backup.transactions.forEach { tx ->
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("type", tx.type.name)
            obj.put("amount", tx.amount)
            obj.put("categoryId", tx.categoryId)
            obj.put("categoryName", tx.categoryName)
            obj.put("dateMillis", tx.dateMillis)
            obj.put("description", tx.description)
            obj.put("paymentMethod", tx.paymentMethod.name)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Categories
        val catArray = JSONArray()
        backup.categories.forEach { cat ->
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("type", cat.type.name)
            obj.put("iconName", cat.iconName)
            obj.put("colorHex", cat.colorHex)
            obj.put("isDefault", cat.isDefault)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        // Budgets
        val bgArray = JSONArray()
        backup.budgets.forEach { bg ->
            val obj = JSONObject()
            obj.put("id", bg.id)
            obj.put("monthKey", bg.monthKey)
            obj.put("categoryId", bg.categoryId ?: JSONObject.NULL)
            obj.put("amount", bg.amount)
            bgArray.put(obj)
        }
        root.put("budgets", bgArray)

        // Settings (exclude pin hash for safety or keep non-sensitive keys)
        val stArray = JSONArray()
        backup.settings.forEach { st ->
            // Do not export sensitive security PIN hash to plain JSON
            if (st.key != "pin_hash") {
                val obj = JSONObject()
                obj.put("key", st.key)
                obj.put("value", st.value)
                stArray.put(obj)
            }
        }
        root.put("settings", stArray)

        return root.toString(2)
    }

    fun deserialize(jsonString: String): FullBackupData? {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("transactions") || !root.has("categories")) {
                return null
            }

            val timestamp = root.optLong("exportTimestamp", System.currentTimeMillis())
            val version = root.optString("appVersion", "1.0")

            val txArray = root.getJSONArray("transactions")
            val transactions = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                transactions.add(
                    TransactionEntity(
                        id = 0, // Auto-increment on restore to avoid conflict
                        type = TransactionType.valueOf(obj.getString("type")),
                        amount = obj.getLong("amount"),
                        categoryId = obj.getString("categoryId"),
                        categoryName = obj.getString("categoryName"),
                        dateMillis = obj.getLong("dateMillis"),
                        description = obj.optString("description", ""),
                        paymentMethod = try {
                            PaymentMethod.valueOf(obj.optString("paymentMethod", "CASH"))
                        } catch (_: Exception) {
                            PaymentMethod.CASH
                        }
                    )
                )
            }

            val catArray = root.getJSONArray("categories")
            val categories = mutableListOf<CategoryEntity>()
            for (i in 0 until catArray.length()) {
                val obj = catArray.getJSONObject(i)
                categories.add(
                    CategoryEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        type = TransactionType.valueOf(obj.getString("type")),
                        iconName = obj.optString("iconName", "category"),
                        colorHex = obj.optString("colorHex", "#64748B"),
                        isDefault = obj.optBoolean("isDefault", false)
                    )
                )
            }

            val budgets = mutableListOf<BudgetEntity>()
            if (root.has("budgets")) {
                val bgArray = root.getJSONArray("budgets")
                for (i in 0 until bgArray.length()) {
                    val obj = bgArray.getJSONObject(i)
                    val catId = if (obj.isNull("categoryId")) null else obj.getString("categoryId")
                    budgets.add(
                        BudgetEntity(
                            id = obj.getString("id"),
                            monthKey = obj.getString("monthKey"),
                            categoryId = catId,
                            amount = obj.getLong("amount")
                        )
                    )
                }
            }

            val settings = mutableListOf<AppSettingsEntity>()
            if (root.has("settings")) {
                val stArray = root.getJSONArray("settings")
                for (i in 0 until stArray.length()) {
                    val obj = stArray.getJSONObject(i)
                    settings.add(
                        AppSettingsEntity(
                            key = obj.getString("key"),
                            value = obj.getString("value")
                        )
                    )
                }
            }

            FullBackupData(
                exportTimestamp = timestamp,
                appVersion = version,
                transactions = transactions,
                categories = categories,
                budgets = budgets,
                settings = settings
            )
        } catch (_: Exception) {
            null
        }
    }
}
