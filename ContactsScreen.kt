package com.demopay.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val upiRegex = Regex("^[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onBack: () -> Unit, onPay: (Payee) -> Unit) {
    var upi by remember { mutableStateOf("") }
    val valid = upiRegex.matches(upi.trim())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Pay contacts (Demo)") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Column(Modifier.padding(bottom = 12.dp)) {
                    Text("Pay to a UPI ID", fontWeight = FontWeight.Bold)
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = upi,
                            onValueChange = { upi = it },
                            label = { Text("name@bank") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { onPay(Payee(name = upi.trim(), upiId = upi.trim())) },
                            enabled = valid,
                            modifier = Modifier.padding(start = 8.dp)
                        ) { Text("Next") }
                    }
                    Text(
                        "Demo contacts",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
            items(demoContacts) { c ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPay(Payee(name = c.name, upiId = c.upiId)) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(c.name, c.color)
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(c.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            c.upiId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
