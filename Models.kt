package com.demopay.app

import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String) {
    LIGHT("Light"), DARK("Dark"), AMOLED("AMOLED Black")
}

data class Payee(
    val name: String,
    val upiId: String,
    val amountPaise: Long? = null,
    val note: String = "",
    val isUpiQr: Boolean = true,
    val rawText: String = ""
)

/** A fictional bank account that only exists inside this demo app. */
data class DemoAccount(
    val id: String,
    val bank: String,
    val last4: String,
    val balancePaise: Long
)

data class Txn(
    val id: String,
    val ref: String,
    val payeeName: String,
    val upiId: String,
    val amountPaise: Long,
    val note: String,
    val time: Long,
    val bank: String = "Demo Bank",
    val last4: String = "0000",
    val status: String = "PAYMENT COMPLETE"
)

data class Contact(val name: String, val upiId: String, val color: Color)

val defaultAccounts = listOf(
    DemoAccount("acc1", "Demo Bank", "1234", 1_000_000L),
    DemoAccount("acc2", "Sample Bank", "5678", 500_000L),
    DemoAccount("acc3", "Practice Bank", "9012", 250_000L)
)

val demoContacts = listOf(
    Contact("Ramesh Kumar", "ramesh.demo@demobank", Color(0xFF1E88E5)),
    Contact("Sunita Devi", "sunita.demo@demobank", Color(0xFF8E24AA)),
    Contact("Amit Verma", "amit.demo@demobank", Color(0xFF43A047)),
    Contact("Priya Singh", "priya.demo@demobank", Color(0xFFEF6C00)),
    Contact("Vikas Gupta", "vikas.demo@demobank", Color(0xFF3949AB)),
    Contact("Neha Sharma", "neha.demo@demobank", Color(0xFF00897B)),
    Contact("Rahul Yadav", "rahul.demo@demobank", Color(0xFF6D4C41)),
    Contact("School Fees (Demo)", "schoolfees.demo@demobank", Color(0xFF00838F))
)
