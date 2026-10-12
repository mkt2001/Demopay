package com.demopay.app

import android.net.Uri
import java.math.BigDecimal

private const val MAX_QR_LEN = 600

private fun clean(s: String?, max: Int): String =
    (s ?: "").filter { !it.isISOControl() }.trim().take(max)

/** Reads details from a scanned QR. UPI QR format: upi://pay?pa=ID&pn=Name&am=10&tn=Note */
fun parseQr(raw: String): Payee {
    val text = clean(raw, MAX_QR_LEN)
    if (text.startsWith("upi://", ignoreCase = true)) {
        val uri = Uri.parse(text)
        val pa = clean(uri.getQueryParameter("pa"), 80)
        val pn = clean(uri.getQueryParameter("pn"), 60)
        val tn = clean(uri.getQueryParameter("tn"), 60)
        val paise = uri.getQueryParameter("am")?.trim()?.toBigDecimalOrNull()
            ?.multiply(BigDecimal(100))?.toLong()
        return Payee(
            name = if (pn.isNotBlank()) pn else if (pa.isNotBlank()) pa else "Unknown payee",
            upiId = if (pa.isNotBlank()) pa else "Not available",
            amountPaise = if (paise != null && paise > 0 && paise <= DemoProvider.MAX_PER_TXN_PAISE) paise else null,
            note = tn,
            isUpiQr = true,
            rawText = text
        )
    }
    return Payee(
        name = if (text.length > 40) text.take(40) + "..." else text.ifBlank { "Unknown QR" },
        upiId = "Not a UPI QR",
        isUpiQr = false,
        rawText = text
    )
}
