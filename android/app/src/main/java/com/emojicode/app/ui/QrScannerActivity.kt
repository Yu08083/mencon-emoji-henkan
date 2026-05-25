package com.emojicode.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emojicode.app.ui.screens.QrScannerScreen
import com.emojicode.app.ui.theme.EmojiCodeTheme

class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }

        setContent {
            EmojiCodeTheme {
                Surface(Modifier.fillMaxSize()) {
                    val vm: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(applicationContext)
                    )
                    QrScannerScreen(vm = vm, onClose = { finish() })
                }
            }
        }
    }
}
