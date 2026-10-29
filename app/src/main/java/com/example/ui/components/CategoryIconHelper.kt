package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {

    fun getIcon(iconName: String): ImageVector {
        return when (iconName) {
            "restaurant" -> Icons.Default.Restaurant
            "directions_bus" -> Icons.Default.DirectionsBus
            "home" -> Icons.Default.Home
            "bolt" -> Icons.Default.Bolt
            "wifi" -> Icons.Default.Wifi
            "medical_services" -> Icons.Default.MedicalServices
            "school" -> Icons.Default.School
            "checkroom" -> Icons.Default.Checkroom
            "groups" -> Icons.Default.Groups
            "sports_esports" -> Icons.Default.SportsEsports
            "volunteer_activism" -> Icons.Default.VolunteerActivism
            "savings" -> Icons.Default.Savings
            "payments" -> Icons.Default.Payments
            "storefront" -> Icons.Default.Storefront
            "work" -> Icons.Default.Work
            "card_giftcard" -> Icons.Default.CardGiftcard
            "monetization_on" -> Icons.Default.MonetizationOn
            "account_balance" -> Icons.Default.AccountBalance
            "shopping_bag" -> Icons.Default.ShoppingBag
            "fitness_center" -> Icons.Default.FitnessCenter
            else -> Icons.Default.Category
        }
    }

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF0B6646)): Color {
        return try {
            val clean = hex.removePrefix("#")
            val colorInt = if (clean.length == 6) {
                ("FF$clean").toLong(16)
            } else {
                clean.toLong(16)
            }
            Color(colorInt)
        } catch (_: Exception) {
            defaultColor
        }
    }

    val AVAILABLE_ICONS = listOf(
        "restaurant" to "Alimentation",
        "directions_bus" to "Transport",
        "home" to "Logement",
        "bolt" to "Énergie",
        "wifi" to "Internet/Tel",
        "medical_services" to "Santé",
        "school" to "Éducation",
        "checkroom" to "Habillement",
        "groups" to "Famille",
        "sports_esports" to "Loisirs",
        "volunteer_activism" to "Dons/Église",
        "savings" to "Épargne",
        "payments" to "Salaire",
        "storefront" to "Commerce",
        "work" to "Travail",
        "card_giftcard" to "Cadeaux",
        "monetization_on" to "Revenus",
        "category" to "Général"
    )

    val AVAILABLE_COLORS = listOf(
        "#0B6646", "#1B8755", "#1E6F8F", "#3B82F6",
        "#8B5CF6", "#A855F7", "#EC4899", "#F43F5E",
        "#E0533C", "#F59E0B", "#D97706", "#64748B"
    )
}
