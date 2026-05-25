package com.emojicode.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emojicode.app.ui.screens.CipherEditorScreen
import com.emojicode.app.ui.theme.EmojiCodeTheme

class CipherEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmojiCodeTheme {
                Surface(Modifier.fillMaxSize()) {
                    val vm: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(applicationContext)
                    )
                    CipherEditorScreen(vm, onBack = { finish() })
                }
            }
        }
    }
}
