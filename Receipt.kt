package com.demopay.app

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

private const val GREEN = 0xFF1B8A3C.toInt()
private const val LIGHT_GREEN = 0xFFDDF3E3.toInt()
private const val INK = 0xFF1B1C1F.toInt()
private const val GREY = 0xFF5B6560.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()

private fun paint(color: Int, size: Float, bold: Boolean, align: Paint.Align): Paint {
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = color
    p.textSize = size
    p.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    p.textAlign = align
    return p
}

private fun fit(text: String, p: Paint, maxW: Float): String {
    if (p.measureText(text) <= maxW) return text
    var t = text
    while (t.length > 1 && p.measureText(t + "...") > maxW) t = t.dropLast(1)
    return t + "..."
}

/** Draws the receipt image. DEMO marks are part of the picture itself (big tiled watermark + banners). */
fun buildReceiptBitmap(txn: Txn): Bitmap {
    val w = 1080
    val h = 1700
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(WHITE)
    val cx = w / 2f

    // big green tick
    val ring = Paint(Paint.ANTI_ALIAS_FLAG)
    ring.color = LIGHT_GREEN
    c.drawCircle(cx, 250f, 190f, ring)
    val disc = Paint(Paint.ANTI_ALIAS_FLAG)
    disc.color = GREEN
    c.drawCircle(cx, 250f, 145f, disc)
    val tick = Paint(Paint.ANTI_ALIAS_FLAG)
    tick.color = WHITE
    tick.style = Paint.Style.STROKE
    tick.strokeWidth = 32f
    tick.strokeCap = Paint.Cap.ROUND
    tick.strokeJoin = Paint.Join.ROUND
    val path = Path()
    path.moveTo(cx - 65f, 255f)
    path.lineTo(cx - 20f, 300f)
    path.lineTo(cx + 70f, 200f)
    c.drawPath(path, tick)

    c.drawText("Payment Complete", cx, 520f, paint(GREEN, 74f, true, Paint.Align.CENTER))

    // DEMO pill
    val pillFill = Paint(Paint.ANTI_ALIAS_FLAG)
    pillFill.color = LIGHT_GREEN
    c.drawRoundRect(RectF(120f, 560f, 960f, 640f), 40f, 40f, pillFill)
    c.drawText(DEMO_TEXT, cx, 614f, paint(GREEN, 40f, true, Paint.Align.CENTER))

    c.drawText(formatMoney(txn.amountPaise), cx, 790f, paint(INK, 130f, true, Paint.Align.CENTER))
    val namePaint = paint(INK, 48f, true, Paint.Align.CENTER)
    c.drawText(fit("Paid to " + txn.payeeName, namePaint, 900f), cx, 870f, namePaint)
    val upiPaint = paint(GREY, 38f, false, Paint.Align.CENTER)
    c.drawText(fit(txn.upiId, upiPaint, 900f), cx, 925f, upiPaint)

    val line = Paint()
    line.color = 0xFFD9E2DB.toInt()
    line.strokeWidth = 3f
    c.drawLine(90f, 975f, w - 90f, 975f, line)

    val rows = ArrayList<Pair<String, String>>()
    rows.add("Status" to "PAYMENT COMPLETE (DEMO)")
    rows.add("Demo reference ID (not a real UTR)" to txn.ref)
    rows.add("Date and time" to formatTime(txn.time))
    rows.add("Paid from" to txn.bank + "  \u2022\u2022\u2022\u2022 " + txn.last4)
    if (txn.note.isNotBlank()) rows.add("Note" to txn.note)
    val labelPaint = paint(GREY, 32f, false, Paint.Align.LEFT)
    val valuePaint = paint(INK, 40f, true, Paint.Align.LEFT)
    var y = 1045f
    for ((label, value) in rows) {
        c.drawText(label, 90f, y, labelPaint)
        c.drawText(fit(value, valuePaint, 900f), 90f, y + 50f, valuePaint)
        y += 105f
    }

    // footer banner
    val footer = Paint()
    footer.color = GREEN
    c.drawRect(0f, 1570f, w.toFloat(), h.toFloat(), footer)
    c.drawText("NO REAL MONEY WAS TRANSFERRED", cx, 1650f, paint(WHITE, 38f, true, Paint.Align.CENTER))

    // tiled diagonal watermark over everything
    val wm = paint(GREEN, 62f, true, Paint.Align.LEFT)
    wm.alpha = 52
    c.save()
    c.rotate(-30f, cx, h / 2f)
    var row = 0
    var yy = -500f
    while (yy < h + 500f) {
        var xx = -600f + (if (row % 2 == 0) 0f else 330f)
        while (xx < w + 600f) {
            c.drawText(DEMO_TEXT, xx, yy, wm)
            xx += 860f
        }
        yy += 170f
        row++
    }
    c.restore()
    return bmp
}

fun shareReceipt(ctx: Context, bmp: Bitmap, txn: Txn) {
    try {
        val dir = File(ctx.cacheDir, "receipts")
        dir.mkdirs()
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "demo_receipt_" + txn.ref + ".png")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_TEXT, "DEMO receipt - not a real payment")
        intent.clipData = ClipData.newRawUri("receipt", uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        ctx.startActivity(Intent.createChooser(intent, "Share demo receipt"))
    } catch (e: Exception) {
        Toast.makeText(ctx, "Could not share the receipt", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(txn: Txn, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val bitmap = remember(txn.id) { buildReceiptBitmap(txn) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Receipt preview (Demo)") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Demo receipt",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            )
            Button(
                onClick = { shareReceipt(ctx, bitmap, txn) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.Share, null)
                Text("  Share receipt image")
            }
            Text(
                "Every receipt image carries DEMO marks on purpose. It is practice material, not proof of payment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
