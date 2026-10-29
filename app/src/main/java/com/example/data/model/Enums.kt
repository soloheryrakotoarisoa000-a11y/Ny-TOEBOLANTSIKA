package com.example.data.model

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class PaymentMethod(val labelFr: String) {
    CASH("Espèces"),
    MOBILE_MONEY("Mobile Money"),
    BANK("Banque / Virement"),
    OTHER("Autre")
}
