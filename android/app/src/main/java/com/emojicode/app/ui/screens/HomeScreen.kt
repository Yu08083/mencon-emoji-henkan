package com.emojicode.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emojicode.app.notif.NotificationAccess
import com.emojicode.app.overlay.FloatingOverlayService
import com.emojicode.app.ui.HomeViewModel
import com.emojicode.app.ui.components.PermissionBanner
import com.emojicode.app.ui.components.SectionCard
import com.emojicode.app.ui.CipherEditorActivity
import com.emojicode.app.ui.QrScannerActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: HomeViewModel) {
    val mode by vm.mode.collectAsState()
    val input by vm.input.collectAsState()
    val output by vm.output.collectAsState()
    val pendingImport by vm.showImportConfirm.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "メンコン絵文字",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "EMOJI ⇄ KANA CIPHER",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, QrScannerActivity::class.java))
                    }) { Icon(Icons.Outlined.QrCodeScanner, contentDescription = "QRスキャン") }
                    IconButton(onClick = {
                        context.startActivity(Intent(context, CipherEditorActivity::class.java))
                    }) { Icon(Icons.Outlined.Tune, contentDescription = "暗号表編集") }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // パーミッションバナー
            PermissionBanner(vm = vm)

            // 変換カード
            SectionCard {
                ConverterSection(
                    mode = mode,
                    input = input,
                    output = output,
                    onModeChange = vm::setMode,
                    onInputChange = vm::updateInput,
                )
            }

            // 履歴・お気に入りは編集画面で扱うので、ここはシンプルに
            Text(
                "💡 暗号表の編集・お気に入り・QR共有は右上のアイコンから",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // 共有暗号表の受け入れダイアログ
    pendingImport?.let { incoming ->
        AlertDialog(
            onDismissRequest = { vm.rejectImport() },
            title = { Text("共有された暗号表") },
            text = { Text("受け取った暗号表を取り込みますか？\n現在の暗号表は上書きされます。") },
            confirmButton = {
                TextButton(onClick = { vm.acceptImport(incoming) }) { Text("取り込む") }
            },
            dismissButton = {
                TextButton(onClick = { vm.rejectImport() }) { Text("キャンセル") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterSection(
    mode: HomeViewModel.Mode,
    input: String,
    output: String,
    onModeChange: (HomeViewModel.Mode) -> Unit,
    onInputChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // タブ
        SegmentedTabs(mode = mode, onChange = onModeChange)

        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (mode == HomeViewModel.Mode.DECODE) "絵文字を入力" else "ひらがな・カタカナを入力") },
            placeholder = {
                Text(if (mode == HomeViewModel.Mode.DECODE)
                    "🎒🐨🐱🍅\"🧸🌙🥁🔥🦑🌙🥁🍑🛷🍅" else "らくてん")
            },
            minLines = 3,
            keyboardOptions = KeyboardOptions(autoCorrect = false)
        )

        Text("↓", fontSize = 16.sp, color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        OutputDisplay(text = output)
    }
}

@Composable
private fun SegmentedTabs(mode: HomeViewModel.Mode, onChange: (HomeViewModel.Mode) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabButton(
            text = "絵文字 → 文字",
            active = mode == HomeViewModel.Mode.DECODE,
            modifier = Modifier.weight(1f),
            onClick = { onChange(HomeViewModel.Mode.DECODE) }
        )
        TabButton(
            text = "文字 → 絵文字",
            active = mode == HomeViewModel.Mode.ENCODE,
            modifier = Modifier.weight(1f),
            onClick = { onChange(HomeViewModel.Mode.ENCODE) }
        )
    }
}

@Composable
private fun TabButton(text: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = if (active) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        tonalElevation = if (active) 1.dp else 0.dp,
    ) {
        Text(
            text,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OutputDisplay(text: String) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
            .heightIn(min = 70.dp)
    ) {
        Column {
            if (text.isBlank()) {
                Text(
                    "ここに変換結果が表示されます",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                )
            } else {
                Text(text, fontSize = 18.sp, lineHeight = 28.sp)
            }
        }
        if (text.isNotBlank()) {
            FilledTonalButton(
                onClick = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                },
                modifier = Modifier.align(Alignment.TopEnd),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("コピー", fontSize = 11.sp)
            }
        }
    }
}
