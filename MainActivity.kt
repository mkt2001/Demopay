package com.demopay.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

sealed interface Screen {
    object Home : Screen
    object Scan : Screen
    object History : Screen
    object Contacts : Screen
    class Pay(val payee: Payee) : Screen
    class Success(val txn: Txn) : Screen
    class Receipt(val txn: Txn) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val state = AppState(applicationContext)
        setContent {
            DemoPayTheme(state.theme) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(state)
                }
            }
        }
    }
}

@Composable
fun AppRoot(state: AppState) {
    val ctx = LocalContext.current
    var stack by remember { mutableStateOf<List<Screen>>(listOf(Screen.Home)) }

    fun push(s: Screen) { stack = stack + s }
    fun pop() { if (stack.size > 1) stack = stack.dropLast(1) }

    // notifications permission (Android 13+), asked once at start
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val camLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) push(Screen.Scan)
        else Toast.makeText(ctx, "Camera permission is needed to scan QR codes", Toast.LENGTH_LONG).show()
    }

    fun openScanner() {
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) push(Screen.Scan) else camLauncher.launch(Manifest.permission.CAMERA)
    }

    BackHandler(enabled = stack.size > 1) {
        if (stack.last() is Screen.Success) stack = listOf(Screen.Home) else pop()
    }

    when (val s = stack.last()) {
        is Screen.Home -> HomeScreen(
            state = state,
            onScan = { openScanner() },
            onContacts = { push(Screen.Contacts) },
            onHistory = { push(Screen.History) },
            onPayContact = { c -> push(Screen.Pay(Payee(name = c.name, upiId = c.upiId))) }
        )
        is Screen.Scan -> ScannerScreen(
            onResult = { text ->
                if (stack.last() is Screen.Scan) {
                    stack = stack.dropLast(1) + Screen.Pay(parseQr(text))
                }
            },
            onBack = { pop() }
        )
        is Screen.Contacts -> ContactsScreen(
            onBack = { pop() },
            onPay = { payee -> push(Screen.Pay(payee)) }
        )
        is Screen.Pay -> PayScreen(
            payee = s.payee,
            state = state,
            onBack = { pop() },
            onDone = { t ->
                Notifier.paymentComplete(ctx, t)
                stack = listOf(Screen.Home, Screen.Success(t))
            }
        )
        is Screen.Success -> SuccessScreen(
            txn = s.txn,
            onReceipt = { push(Screen.Receipt(s.txn)) },
            onDone = { stack = listOf(Screen.Home) }
        )
        is Screen.Receipt -> ReceiptScreen(txn = s.txn, onBack = { pop() })
        is Screen.History -> HistoryScreen(
            state = state,
            onBack = { pop() },
            onReceipt = { t -> push(Screen.Receipt(t)) }
        )
    }
}
