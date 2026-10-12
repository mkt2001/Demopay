package com.demopay.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

private val amountRegex = Regex("^\\d{0,7}(\\.\\d{0,2})?$")

private fun parseAmount(text: String): Long? =
    text.toBigDecimalOrNull()?.multiply(BigDecimal(100))?.toLong()

private fun plainAmount(paise: Long): String =
    BigDecimal(paise).movePointLeft(2).stripTrailingZeros().toPlainString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayScreen(payee: Payee, state: AppState, onBack: () -> Unit, onDone: (Txn) -> Unit) {
    var amountText by remember { mutableStateOf(payee.amountPaise?.let { plainAmount(it) } ?: "") }
    var note by remember { mutableStateOf(payee.note) }
    var accountId by remember { mutableStateOf(state.accounts.first().id) }
    var confirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val account = state.accounts.firstOrNull { it.id == accountId } ?: state.accounts.first()
    val amountPaise: Long? = parseAmount(amountText)
    val insufficient = amountPaise != null && amountPaise > account.balancePaise
    val tooHigh = amountPaise != null && amountPaise > DemoProvider.MAX_PER_TXN_PAISE
    val canPay = amountPaise != null && amountPaise > 0 && !insufficient && !tooHigh

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Send money (Demo)") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DemoBanner()

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(payee.name, colorFor(payee.name), 52.dp)
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(payee.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            payee.upiId,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (!payee.isUpiQr) {
                Text(
                    "This QR does not contain UPI payment details. QR content: " + payee.rawText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { v -> if (amountRegex.matches(v)) { amountText = v; error = null } },
                label = { Text("Amount (Rs)") },
                singleLine = true,
                isError = insufficient || tooHigh,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.headlineMedium,
                supportingText = {
                    if (insufficient) Text("Not enough demo balance in this account")
                    else if (tooHigh) Text("Demo limit: " + formatMoney(DemoProvider.MAX_PER_TXN_PAISE) + " per payment")
                    else Text("Available: " + formatMoney(account.balancePaise))
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 60) note = it },
                label = { Text("Note (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Pay from (demo bank account)", fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    state.accounts.forEach { a ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { accountId = a.id; error = null }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = a.id == accountId, onClick = null)
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(a.bank, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "\u2022\u2022\u2022\u2022 " + a.last4,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(formatMoney(a.balancePaise), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            val err = error
            if (err != null) {
                Text(err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = { confirm = true },
                enabled = canPay,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Pay " + formatMoney(amountPaise ?: 0L) + " (Demo)", fontSize = 16.sp)
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Confirm demo payment") },
            text = {
                Text(
                    "Pay " + formatMoney(amountPaise ?: 0L) + " to " + payee.name + " from " +
                        account.bank + "?\n\nNo real money will move. This only changes the demo balance."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val a = amountPaise
                    confirm = false
                    if (a != null) {
                        val r = state.pay(account.id, payee, a, note.trim())
                        if (r is PayResult.Success) onDone(r.txn)
                        if (r is PayResult.Failure) error = r.reason
                    }
                }) { Text("Pay (Demo)") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SuccessScreen(txn: Txn, onReceipt: () -> Unit, onDone: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tick"
    )

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // big green tick
            Box(
                Modifier.size(220.dp).scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(DemoGreen.copy(alpha = 0.16f))
                )
                Box(
                    Modifier
                        .size(168.dp)
                        .clip(CircleShape)
                        .background(DemoGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(112.dp))
                }
            }
            Text(
                "Payment Complete",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DemoGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                formatMoney(txn.amountPaise),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text("Paid to " + txn.payeeName, modifier = Modifier.padding(top = 4.dp))
            Text(txn.upiId, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(14.dp))
            DemoBanner()

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    DetailRow("Status", "PAYMENT COMPLETE (DEMO)")
                    DetailRow("Demo reference ID (not a real UTR)", txn.ref)
                    DetailRow("Date and time", formatTime(txn.time))
                    DetailRow("Paid from", txn.bank + "  \u2022\u2022\u2022\u2022 " + txn.last4)
                    if (txn.note.isNotBlank()) DetailRow("Note", txn.note)
                }
            }
            Text(
                "This is a simulation. No bank or UPI network was used and no money moved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
            OutlinedButton(
                onClick = onReceipt,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(52.dp)
            ) { Text("View receipt") }
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp)
            ) { Text("Done") }
        }
        Watermark()
    }
}
