package com.demopay.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(state: AppState, onBack: () -> Unit, onReceipt: (Txn) -> Unit) {
    var selected by remember { mutableStateOf<Txn?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Transaction history (Demo)") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            if (state.txns.isEmpty()) {
                Text(
                    "No demo transactions yet.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    items(state.txns) { t -> TxnRow(t) { selected = t } }
                }
            }
        }
    }

    selected?.let { t ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text("Demo transaction") },
            text = {
                Column {
                    DemoBanner()
                    DetailRow("Amount", formatMoney(t.amountPaise))
                    DetailRow("Paid to", t.payeeName)
                    DetailRow("UPI ID", t.upiId)
                    DetailRow("Demo reference ID (not a real UTR)", t.ref)
                    DetailRow("Date and time", formatTime(t.time))
                    if (t.note.isNotBlank()) DetailRow("Note", t.note)
                    DetailRow("Status", t.status + " (DEMO)")
                    DetailRow("Paid from", t.bank + "  \u2022\u2022\u2022\u2022 " + t.last4)
                }
            },
            confirmButton = {
                TextButton(onClick = { selected = null; onReceipt(t) }) { Text("View receipt") }
            },
            dismissButton = { TextButton(onClick = { selected = null }) { Text("Close") } }
        )
    }
}
