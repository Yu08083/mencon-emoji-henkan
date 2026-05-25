package com.emojicode.app.ui.screens

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.emojicode.app.cipher.CipherUrlCodec
import com.emojicode.app.ui.HomeViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(vm: HomeViewModel, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var detectedCipher by remember { mutableStateOf<Map<String, String>?>(null) }
    var status by remember { mutableStateOf("QRコードをカメラにかざしてください") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QRコードを読み取る") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Outlined.Close, contentDescription = "閉じる")
                    }
                },
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    val scanner = BarcodeScanning.getClient(
                        BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                    )

                    cameraProviderFuture.addListener({
                        try {
                            val provider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val analyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            analyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { proxy ->
                                val mediaImage = proxy.image
                                if (mediaImage == null || detectedCipher != null) {
                                    proxy.close()
                                    return@setAnalyzer
                                }
                                val image = InputImage.fromMediaImage(
                                    mediaImage, proxy.imageInfo.rotationDegrees
                                )
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (bc in barcodes) {
                                            val raw = bc.rawValue ?: continue
                                            val parsed = CipherUrlCodec.parseFromText(raw)
                                                ?: CipherUrlCodec.parseFromUri(android.net.Uri.parse(raw))
                                            if (parsed != null) {
                                                detectedCipher = parsed
                                                status = "暗号表を検出しました"
                                                break
                                            }
                                        }
                                    }
                                    .addOnFailureListener { Log.w("QrScanner", it) }
                                    .addOnCompleteListener { proxy.close() }
                            }

                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analyzer,
                            )
                        } catch (e: Exception) {
                            status = "カメラを起動できません"
                        }
                    }, ContextCompat.getMainExecutor(context))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // 中央の枠線
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.0f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {}
            }

            // ステータス
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    status,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }

        detectedCipher?.let { incoming ->
            AlertDialog(
                onDismissRequest = { detectedCipher = null; status = "QRコードをかざしてください" },
                title = { Text("暗号表を検出") },
                text = { Text("読み取った暗号表を取り込みますか？") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            vm.repo.setCipher(incoming)
                            onClose()
                        }
                    }) { Text("取り込む") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        detectedCipher = null
                        status = "QRコードをかざしてください"
                    }) { Text("キャンセル") }
                }
            )
        }
    }
}
