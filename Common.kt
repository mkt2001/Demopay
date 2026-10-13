package com.demopay.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.SecureRandom
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

const val DEMO_TEXT = "DEMO \u2014 NOT A REAL PAYMENT"

fun formatMoney(paise: Long): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return nf.format(paise / 100.0)
}

fun formatTime(ms: Long): String =
    SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(ms))

/** Demo reference ID. Deliberately NOT numeric and NOT a real UTR. */
fun genRef(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    val r = SecureRandom()
    val sb = StringBuilder("DEMO-")
    repeat(10) { sb.append(chars[r.nextInt(chars.length)]) }
    return sb.toString()
}

private val palette = listOf(
    Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFF43A047), Color(0xFFEF6C00),
    Color(0xFF8E24AA), Color(0xFF00897B), Color(0xFF6D4C41), Color(0xFF3949AB)
)

fun colorFor(name: String): Color = palette[abs(name.hashCode()) % palette.size]

const val MAX_NAME = 60

@Composable
fun DemoBanner(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(
            "  $DEMO_TEXT",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun Avatar(name: String, color: Color, size: Dp = 44.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
        Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.42f).sp)
    }
}

/** Light repeated DEMO words drawn behind a screen so it can never pass as a real payment screen. */
@Composable
fun Watermark() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(8) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                repeat(2) {
                    Text(
                        "DEMO",
                        modifier = Modifier.rotate(-25f),
                        color = DemoGreen.copy(alpha = 0.13f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TxnRow(t: Txn, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(t.payeeName, colorFor(t.payeeName))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(t.payeeName, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                formatTime(t.time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("- " + formatMoney(t.amountPaise), fontWeight = FontWeight.Bold)
            Text(
                "DEMO \u00B7 COMPLETE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}
