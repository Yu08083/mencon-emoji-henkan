package com.emojicode.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emojicode.app.cipher.CipherUrlCodec
import com.emojicode.app.storage.CipherRepository
import com.emojicode.app.ui.screens.HomeScreen
import com.emojicode.app.ui.theme.EmojiCodeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EmojiCodeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(applicationContext)
                    )
                    HomeScreen(vm)
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /** URL/カスタムスキームで起動された場合、暗号表をインポート */
    private fun handleIntent(intent: Intent?) {
        intent ?: return
        val data = intent.data ?: return
        val imported = CipherUrlCodec.parseFromUri(data) ?: return
        val repo = CipherRepository(applicationContext)
        // バックグラウンドで保存しつつ、ユーザーにダイアログ表示するのは HomeScreen 側のpending状態に渡す
        HomeViewModel.pendingImport = imported
    }
}
