package com.demopay.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray
import org.json.JSONObject

/** Encrypted local storage (AES-256, key in Android Keystore). Falls back to private prefs if the Keystore fails. */
private fun openPrefs(context: Context): SharedPreferences {
    return try {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context, "demopay_secure", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("demopay_plain", Context.MODE_PRIVATE)
    }
}

/** Holds demo accounts, demo history and theme. Everything stays on this phone. */
class AppState(context: Context) {

    private val prefs = openPrefs(context)
    private val provider: PaymentProvider = DemoProvider()

    var accounts by mutableStateOf(loadAccounts())
        private set

    var txns by mutableStateOf(loadTxns())
        private set

    var theme by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme", "LIGHT") ?: "LIGHT") }
            .getOrDefault(ThemeMode.LIGHT)
    )
        private set

    val totalPaise: Long get() = accounts.sumOf { it.balancePaise }

    private fun loadAccounts(): List<DemoAccount> =
        defaultAccounts.map { a -> a.copy(balancePaise = prefs.getLong("bal_" + a.id, a.balancePaise)) }

    private fun loadTxns(): List<Txn> {
        val raw = prefs.getString("txns", "[]") ?: "[]"
        val arr = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val out = ArrayList<Txn>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                Txn(
                    id = o.optString("id"),
                    ref = o.optString("ref"),
                    payeeName = o.optString("name"),
                    upiId = o.optString("upi"),
                    amountPaise = o.optLong("amt"),
                    note = o.optString("note"),
                    time = o.optLong("time"),
                    bank = o.optString("bank", "Demo Bank"),
                    last4 = o.optString("last4", "0000"),
                    status = o.optString("status", "PAYMENT COMPLETE")
                )
            )
        }
        return out
    }

    private fun saveTxns(list: List<Txn>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(
                JSONObject()
                    .put("id", t.id).put("ref", t.ref).put("name", t.payeeName)
                    .put("upi", t.upiId).put("amt", t.amountPaise).put("note", t.note)
                    .put("time", t.time).put("bank", t.bank).put("last4", t.last4)
                    .put("status", t.status)
            )
        }
        prefs.edit().putString("txns", arr.toString()).apply()
    }

    /** Demo payment: only changes the fake balance of the chosen demo account. */
    fun pay(accountId: String, payee: Payee, amountPaise: Long, note: String): PayResult {
        val acc = accounts.firstOrNull { it.id == accountId }
            ?: return PayResult.Failure("Choose a demo account.")
        val result = provider.process(acc, payee, amountPaise, note)
        if (result is PayResult.Success) {
            val newBalance = acc.balancePaise - amountPaise
            accounts = accounts.map { if (it.id == acc.id) it.copy(balancePaise = newBalance) else it }
            txns = listOf(result.txn) + txns
            prefs.edit().putLong("bal_" + acc.id, newBalance).apply()
            saveTxns(txns)
        }
        return result
    }

    fun updateTheme(mode: ThemeMode) {
        theme = mode
        prefs.edit().putString("theme", mode.name).apply()
    }

    fun resetDemo() {
        accounts = defaultAccounts
        txns = emptyList()
        val e = prefs.edit()
        defaultAccounts.forEach { e.putLong("bal_" + it.id, it.balancePaise) }
        e.putString("txns", "[]").apply()
    }
}
