# 📱 メンコン絵文字変換ツール — Android

LINEなどの通知から絵文字暗号を自動検知し、画面上にフローティング窓で復号結果を表示します。

## 機能

- 🔓 **LINE通知の自動検知** — メッセージが届いた瞬間に暗号を見抜いて復号
- 🪟 **フローティング窓** — 他のアプリの上に重ねて表示。ドラッグで自由に移動
- ✏️ **暗号表のカスタマイズ** — 五十音すべて自由に変更、お気に入りとして保存
- 📷 **QRコード読み取り** — Web版で作った暗号表を読み込んで共有
- 🌐 **URLインポート** — `https://*.github.io/mencon-emoji-henkan/#cipher=...` を開くだけ
- ☾ **ダーク／ライトテーマ** — システム連動

## 必要な権限

| 権限 | 用途 |
|------|------|
| 通知へのアクセス | LINE等の通知から暗号メッセージを検出 |
| 他のアプリの上に表示 | フローティング窓の表示 |
| カメラ（任意） | QRコードのスキャン |
| 通知の表示 | 復号結果の表示（フローティング窓を使わない時） |

権限はすべて初回起動時に案内されます。**通知の中身はアプリ内処理のみで、外部送信は一切ありません。**

## ビルド方法

```bash
cd android
./gradlew assembleRelease
# 生成物: app/build/outputs/apk/release/app-release.apk
```

Debug APKなら：

```bash
./gradlew assembleDebug
```

### 必要なもの
- Android Studio Hedgehog (2023.1) 以降
- JDK 17
- Android SDK Platform 34
- Min SDK 26 (Android 8.0)

## アーキテクチャ

```
android/app/src/main/java/com/emojicode/app/
├── EmojiCodeApp.kt                    Application（通知チャンネル初期化）
├── cipher/
│   ├── DefaultCipher.kt               デフォルト暗号表・かなマップ
│   ├── KanaUtils.kt                   カナ変換・書記素分割
│   ├── Encoder.kt                     文字→絵文字
│   ├── Decoder.kt                     絵文字→文字
│   └── CipherUrlCodec.kt              共有URL・QRのエンコード/デコード
├── storage/
│   └── CipherRepository.kt            DataStoreで暗号表・お気に入りを保存
├── notif/
│   ├── LineNotificationListener.kt    通知リスナーサービス
│   └── NotificationAccess.kt          権限ヘルパー
├── overlay/
│   ├── FloatingOverlayService.kt      画面オーバーレイサービス
│   └── FloatingBubbleView.kt          フローティング窓のView
└── ui/
    ├── MainActivity.kt                ホーム画面
    ├── CipherEditorActivity.kt        暗号表エディタ
    ├── QrScannerActivity.kt           QRスキャナ
    ├── HomeViewModel.kt
    ├── theme/Theme.kt
    ├── components/{PermissionBanner,SectionCard}.kt
    └── screens/{HomeScreen,CipherEditorScreen,QrScannerScreen}.kt
```

## なぜ通知リスナーなのか

Androidでは `NotificationListenerService` という仕組みで、他アプリの通知を読み取れます。
これがLINEの中身に触れる**唯一の合法ルート**です（アクセシビリティで強引にやる手もありますが、不安定で推奨されません）。

iOS版はこれが不可能なので、共有メニュー（Share Extension）経由の半自動になります。

## 既知の制限

- **ロック画面通知**は機種によって取れないことがあります（メーカー独自の省電力設定が原因）
- LINE設定で「メッセージ内容を表示しない」になっていると、当然内容を読み取れません
- 通知が来ても画面オフ中はオーバーレイを出せないので、通知として表示します

---

作者: [𝕏 @yu_](https://twitter.com/yu_)
