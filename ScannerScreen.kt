package com.demopay.app

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class QrAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

    @Volatile
    private var done = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(proxy: ImageProxy) {
        val media = proxy.image
        if (media == null || done) {
            proxy.close()
            return
        }
        val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { codes ->
                val value = codes.firstOrNull()?.rawValue
                if (value != null && !done) {
                    done = true
                    onResult(value)
                }
            }
            .addOnCompleteListener { proxy.close() }
    }
}

@Composable
fun ScannerScreen(onResult: (String) -> Unit, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val owner = ctx as ComponentActivity
    val previewView = remember { PreviewView(ctx) }

    DisposableEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(ctx)
        val executor = Executors.newSingleThreadExecutor()
        var provider: ProcessCameraProvider? = null
        future.addListener({
            val p = future.get()
            provider = p
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor, QrAnalyzer { text -> previewView.post { onResult(text) } })
            try {
                p.unbindAll()
                p.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (e: Exception) {
                // camera not available
            }
        }, ContextCompat.getMainExecutor(ctx))
        onDispose {
            provider?.unbindAll()
            executor.shutdown()
        }
    }

    Box(Modifier.fillMaxSize().then(Modifier)) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Box(
            Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .border(3.dp, Color.White, RoundedCornerShape(24.dp))
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
        }

        Column(
            Modifier.align(Alignment.BottomCenter).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Point the camera at a QR code", color = Color.White, textAlign = TextAlign.Center)
            Text(
                "Demo mode: this only opens a demo payment screen",
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
