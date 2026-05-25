# アーキテクチャ概要

## 全体像

```
                    ┌─────────────────────┐
                    │ DEFAULT_CIPHER (定数)│
                    │ ひらがな44字 → 絵文字│
                    └──────────┬──────────┘
                               │ 4プラットフォーム共通
       ┌───────────────────────┼───────────────────────┐
       ▼                       ▼                       ▼
  ┌─────────┐           ┌──────────┐            ┌──────────┐
  │  Web   │           │ Android  │            │   iOS    │
  │ (JS)   │           │ (Kotlin) │            │  (Swift) │
  └────┬────┘           └────┬─────┘            └────┬─────┘
       │                     │                       │
       └────────┬────────────┴───────────┬───────────┘
                ▼                        ▼
        URL Share Format:    QR Code (同一フォーマット)
        #cipher=BASE64URL(JSON({v,c,x}))
                ▲
                │
       ┌────────┴────────┐
       │ ブラウザ拡張(JS) │
       └─────────────────┘
```

## 暗号エンジンのアルゴリズム

### エンコード (文字 → 絵文字)

```
入力 "がっこう"
  ↓
カタカナ→ひらがな正規化
  ↓
書記素単位で1文字ずつ処理:
  'が' → 濁点系? → 'か'の絵文字 + "
  'っ' → 小書きかな → 'つ' に正規化 → 'つ'の絵文字
  'こ' → 'こ'の絵文字
  'う' → 'う'の絵文字
  ↓
出力 "🦟\"🌙🐨🐴"
```

### デコード (絵文字 → 文字)

```
入力 "🦟"🌙🐨🐴"
  ↓
書記素分割: ["🦟", "\"", "🌙", "🐨", "🐴"]
  ↓
逆引き走査:
  "🦟" → 'か'  次が '"' → 濁点付きに → 'が'
  "🌙" → 'つ'
  "🐨" → 'こ'
  "🐴" → 'う'
  ↓
出力 "がつこう"
```

## 共有フォーマット

```json
{
  "v": 1,
  "c": { "あ": "🍓", "い": "🦄" },
  "x": { "!": "❗" }
}
```

- `v`: バージョン番号（将来の破壊的変更用）
- `c`: デフォルト暗号表との**差分のみ**（保存・送信を最小化）
- `x`: デフォルトに無いキー（特殊文字・追加文字）

これを `JSON.stringify` → UTF-8 bytes → base64url で URL safe にエンコード。

典型的なカスタム暗号表で120〜200バイト程度。QRコードのバージョン3〜5に収まります。

## ストレージ戦略

| 環境 | 保存先 |
|------|--------|
| Web | `localStorage["mencon-emoji-henkan:cipher"]` (差分JSON) |
| Android | DataStore Preferences (`emoji_code_pro`) |
| iOS | UserDefaults (App Group: `group.com.emojicode.app`) |
| 拡張 | `chrome.storage.sync` (端末間同期) |

## Android: 通知検知の仕組み

```
LINE がメッセージ通知をPOST
       ↓
NotificationListenerService.onNotificationPosted()
       ↓
package: jp.naver.line.android ?
       ↓
extras から title / text / bigText を取得
       ↓
containsCipherEmoji() で暗号判定
       ↓
Decoder.decode() で復号
       ↓
分岐:
  ├ オーバーレイ許可あり → FloatingOverlayService.start()
  └ なし → NotificationManager.notify() で別通知ポスト
```

### フォアグラウンドサービスの理由

オーバーレイ表示中は `FloatingOverlayService` をフォアグラウンドとして動かします。
これは Android 8+ の制約上、バックグラウンドからオーバーレイを長時間維持するのに必要。

`foregroundServiceType="specialUse"` を使うことで、Play Storeレビュー時にも
「LINE暗号復号のオーバーレイ」という用途を明示できます。

## iOS: Share Extension の流れ

```
LINE メッセージを長押し
       ↓
共有 → メンコン絵文字で復号 を選択
       ↓
NSExtensionItem.attachments の plainText を取得
       ↓
CipherRepository から App Group の暗号表を読込
       ↓
Decoder.decode() で復号
       ↓
ShareViewController で結果を表示 + UIPasteboard.general.string に書込
```

iOS は他アプリ通知を読めないため、これが現実的な最高速ルート。

## ブラウザ拡張: DOM監視

```
MutationObserver でDOM変更を監視
       ↓
characterData or childList の変化 → requestAnimationFrame でデバウンス
       ↓
TreeWalker で TEXT_NODE を走査
       ↓
nodeValue に暗号表絵文字を含むか?
       ↓
含む → 親のメッセージブロック要素を特定
       ↓
data-emojicode-processed 属性で重複防止
       ↓
ブロックの直後に翻訳バナー <div class="emojicode-banner"> を挿入
```

LINE Web のクラス名は頻繁に変わるため、**セレクタに依存しない**設計にしました。
「絵文字を含む短いテキスト」というヒューリスティクスだけで動きます。

## なぜ4プラットフォームをそれぞれ違う言語で書いたか

- **Web**: JavaScript ESM (バンドラ不要、import文だけで動く)
- **Android**: Kotlin + Jetpack Compose (NotificationListenerService 必須なのでネイティブ必要)
- **iOS**: Swift + SwiftUI (Share Extension のため。React Nativeでは対応不可)
- **拡張**: JavaScript MV3 (chrome.* API 必須)

Cross-platform フレームワーク（Flutter, RN）を使わなかった理由:
1. Android の通知リスナーAPI、iOSのShare Extension は、どちらもネイティブの薄いシムが必要
2. プロジェクト規模が小さく、各プラットフォーム1500行以下に収まる
3. 拡張機能は完全に別物なのでフレームワーク統一の旨味がない
