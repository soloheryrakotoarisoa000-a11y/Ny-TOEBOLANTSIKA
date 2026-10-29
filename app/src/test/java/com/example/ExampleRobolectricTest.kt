package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CategoryEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.CurrencyFormatter
import com.example.util.FullBackupData
import com.example.util.JsonBackupHelper
import com.example.util.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ny Toebolantsika", appName)
    }

    @Test
    fun `currency formatting works as expected for Ariary`() {
        val formatted = CurrencyFormatter.format(125000L, "Ar", false)
        assertEquals("125 000 Ar", formatted)

        val parsed = CurrencyFormatter.parseAmount("125 000 Ar")
        assertEquals(125000L, parsed)
    }

    @Test
    fun `pin security hashing and verification`() {
        val pin = "1234"
        val hash = SecurityUtils.hashPin(pin)
        assertTrue(SecurityUtils.verifyPin("1234", hash))
        assertFalse(SecurityUtils.verifyPin("0000", hash))
    }

    @Test
    fun `json backup serialize and deserialize roundtrip`() {
        val tx = TransactionEntity(
            id = 1,
            type = TransactionType.EXPENSE,
            amount = 50000L,
            categoryId = "food",
            categoryName = "Alimentation",
            dateMillis = 1700000000000L,
            description = "Courses",
            paymentMethod = PaymentMethod.CASH
        )
        val cat = CategoryEntity(
            id = "food",
            name = "Alimentation",
            type = TransactionType.EXPENSE,
            iconName = "restaurant",
            colorHex = "#E0533C",
            isDefault = true
        )
        val backupData = FullBackupData(
            exportTimestamp = 1700000000000L,
            appVersion = "1.0",
            transactions = listOf(tx),
            categories = listOf(cat),
            budgets = emptyList(),
            settings = emptyList()
        )

        val json = JsonBackupHelper.serialize(backupData)
        val restored = JsonBackupHelper.deserialize(json)

        assertNotNull(restored)
        assertEquals(1, restored?.transactions?.size)
        assertEquals(50000L, restored?.transactions?.first()?.amount)
        assertEquals(TransactionType.EXPENSE, restored?.transactions?.first()?.type)
        assertEquals("food", restored?.transactions?.first()?.categoryId)
    }
}
