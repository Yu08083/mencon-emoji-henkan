package com.emojicode.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emojicode.app.cipher.CipherUrlCodec
import com.emojicode.app.cipher.DefaultCipher
import com.emojicode.app.ui.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CipherEditorScreen(vm: HomeViewModel, onBack: () -> Unit) {
    val cipher by vm.cipher.collectAsState()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var showShareDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("暗号表を編集") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Outlined.Share, contentDescription = "共有")
                    }
                }
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
            // 説明
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    "💡 セルをタップして絵文字を変更。濁点・半濁点付きの文字（が、ぱ等）は、対応する清音の絵文字に \" や ' が付きます。",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // 五十音グリッド
            CipherGrid(
                cipher = cipher,
                onChange = { kana, emoji ->
                    scope.launch {
                        val updated = cipher.toMutableMap()
                        updated[kana] = emoji
                        vm.repo.setCipher(updated)
                    }
                }
            )

            // リセットボタン
            OutlinedButton(
                onClick = { scope.launch { vm.repo.resetCipher() } },
                modifier = Modifier.align(Alignment.End)
            ) { Text("デフォルトに戻す") }

            // お気に入りセクション
            FavoritesPanel(vm = vm)
        }
    }

    if (showShareDialog) {
        ShareDialog(cipher = cipher, onDismiss = { showShareDialog = false })
    }
}

@Composable
private fun CipherGrid(
    cipher: Map<String, String>,
    onChange: (String, String) -> Unit,
) {
    var editingKana by remember { mutableStateOf<String?>(null) }

    // LazyGridはネスト時に高さ問題が出るので、固定行のColumn+Rowで構築
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DefaultCipher.KANA_ORDER.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { kana ->
                    Box(Modifier.weight(1f)) {
                        CipherCell(
                            kana = kana,
                            emoji = cipher[kana] ?: "",
                            onClick = { if (kana.isNotEmpty()) editingKana = kana },
                        )
                    }
                }
            }
        }
    }

    editingKana?.let { kana ->
        EditEmojiDialog(
            kana = kana,
            currentEmoji = cipher[kana] ?: "",
            onConfirm = { newEmoji ->
                onChange(kana, newEmoji)
                editingKana = null
            },
            onDismiss = { editingKana = null }
        )
    }
}

@Composable
private fun CipherCell(kana: String, emoji: String, onClick: () -> Unit) {
    val empty = kana.isEmpty()
    Surface(
        onClick = onClick,
        enabled = !empty,
        shape = RoundedCornerShape(6.dp),
        color = if (empty) androidx.compose.ui.graphics.Color.Transparent
                else MaterialTheme.colorScheme.surfaceVariant,
        border = if (empty) null
                 else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.height(58.dp)
    ) {
        if (!empty) {
            Column(
                Modifier.fillMaxSize().padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(kana, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(emoji, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun EditEmojiDialog(
    kana: String, currentEmoji: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(currentEmoji) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("「$kana」の絵文字") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("絵文字を入力") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) {
                Text("決定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}

@Composable
private fun FavoritesPanel(vm: HomeViewModel) {
    val favorites by vm.repo.favorites.collectAsState(initial = emptyList())
    val cipher by vm.cipher.collectAsState()
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("★ お気に入り設定",
            fontSize = 13.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("名前を付けて保存") },
                singleLine = true,
            )
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(
                onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            vm.repo.addFavorite(name.trim(), cipher)
                            name = ""
                        }
                    }
                }
            ) { Text("保存") }
        }

        if (favorites.isEmpty()) {
            Text("保存済みの設定はありません",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(10.dp))
        } else {
            favorites.forEachIndexed { i, fav ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(fav.name, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        scope.launch { vm.repo.setCipher(fav.cipher) }
                    }) { Text("読込") }
                    TextButton(onClick = {
                        scope.launch { vm.repo.deleteFavorite(i) }
                    }) { Text("削除", color = MaterialTheme.colorScheme.error) }
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
private fun ShareDialog(cipher: Map<String, String>, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val url = remember(cipher) {
        CipherUrlCodec.buildShareUrl(cipher, "https://yu08083.github.io/mencon-emoji-henkan/")
    }
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("暗号表を共有") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("このURLを相手に送るか、QRで読み取ってもらってください",
                    fontSize = 12.sp)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        url,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 16.sp,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                clipboard.setText(androidx.compose.ui.text.AnnotatedString(url))
                // 共有Intentも開く
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, url)
                }
                ctx.startActivity(android.content.Intent.createChooser(intent, "共有"))
                onDismiss()
            }) { Text("コピーして共有") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("閉じる") }
        }
    )
}
