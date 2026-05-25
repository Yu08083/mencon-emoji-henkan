package com.emojicode.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emojicode.app.notif.NotificationAccess
import com.emojicode.app.overlay.FloatingOverlayService
import com.emojicode.app.ui.HomeViewModel

/**
 * 通知アクセス・オーバーレイ許可の状態を表示し、設定画面に飛ばす。
 */
@Composable
fun PermissionBanner(vm: HomeViewModel) {
    val ctx = LocalContext.current
    var notifGranted by remember { mutableStateOf(NotificationAccess.isGranted(ctx)) }
    var overlayGranted by remember { mutableStateOf(FloatingOverlayService.canShow(ctx)) }

    // Activityに戻ってきたタイミングで再評価したいが、簡便のためrecomposeで充分
    LaunchedEffect(Unit) {
        notifGranted = NotificationAccess.isGranted(ctx)
        overlayGranted = FloatingOverlayService.canShow(ctx)
    }

    if (notifGranted && overlayGranted) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Bolt, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("自動検知が有効です", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("LINEの通知から暗号メッセージを自動で復号します",
                         fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(10.dp))
                Text("自動検知を有効にするには", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))

            PermissionRow(
                granted = notifGranted,
                title = "通知アクセス",
                desc = "LINEの通知を読み取るために必要",
                onGrant = { NotificationAccess.openSettings(ctx) }
            )
            Spacer(Modifier.height(8.dp))
            PermissionRow(
                granted = overlayGranted,
                title = "他のアプリの上に表示",
                desc = "フローティング窓を出すために必要",
                onGrant = { FloatingOverlayService.openSettings(ctx) }
            )
        }
    }
}

@Composable
private fun PermissionRow(
    granted: Boolean,
    title: String,
    desc: String,
    onGrant: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (granted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(if (granted) "✓" else "!", fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!granted) {
            TextButton(onClick = onGrant) { Text("許可", fontSize = 12.sp) }
        }
    }
}
