package com.demopay.app

import java.util.UUID

/** Result of asking a payment provider to process a payment. */
sealed interface PayResult {
    class Success(val txn: Txn) : PayResult
    class Failure(val reason: String) : PayResult
}

/**
 * Payment provider abstraction. Only DemoProvider exists in this app: it never contacts a
 * bank, a UPI network or the internet. The interface shows where a real provider SDK
 * would plug in later (which needs a licensed partner, not part of this demo).
 */
interface PaymentProvider {
    val name: String
    fun process(account: DemoAccount, payee: Payee, amountPaise: Long, note: String): PayResult
}

class DemoProvider : PaymentProvider {
    override val name: String = "Demo Provider (offline)"

    override fun process(account: DemoAccount, payee: Payee, amountPaise: Long, note: String): PayResult {
        if (amountPaise <= 0L) return PayResult.Failure("Enter an amount greater than zero.")
        if (amountPaise > MAX_PER_TXN_PAISE) {
            return PayResult.Failure("Demo limit is " + formatMoney(MAX_PER_TXN_PAISE) + " per payment.")
        }
        if (amountPaise > account.balancePaise) {
            return PayResult.Failure("Not enough demo balance in " + account.bank + ".")
        }
        if (payee.name.isBlank()) return PayResult.Failure("Payee details are missing.")
        val txn = Txn(
            id = UUID.randomUUID().toString(),
            ref = genRef(),
            payeeName = payee.name,
            upiId = payee.upiId,
            amountPaise = amountPaise,
            note = note,
            time = System.currentTimeMillis(),
            bank = account.bank,
            last4 = account.last4
        )
        return PayResult.Success(txn)
    }

    companion object {
        const val MAX_PER_TXN_PAISE = 10_000_000L // demo Rs 1,00,000
    }
}
