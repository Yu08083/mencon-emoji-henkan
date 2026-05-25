# 📱 メンコン絵文字変換ツール — iOS

LINEメッセージを長押し → 共有 → 「メンコン絵文字」 で復号できる iOSアプリです。

## なぜ Share Extension なのか

iOS は他アプリの通知を読む API を一切公開していないため、Android のような完全自動の検知はできません。
代わりに **共有メニュー（Share Extension）** を使うことで、ワンタップで暗号文を本アプリに渡せます。

```
LINEメッセージを長押し
        ↓
共有 → メンコン絵文字で復号
        ↓
復号結果が画面に表示される（コピーも可能）
```

## 機能

- 🔄 メッセージを共有経由で復号 / 暗号化
- ✏️ 暗号表のカスタマイズ
- ⭐ お気に入り設定の保存
- 🔗 URL共有 — Web版・Android版と同じフォーマット
- 📲 Universal Links 対応 — `https://yu08083.github.io/mencon-emoji-henkan/#cipher=...` を開くと自動で取り込み
- 🌓 ダーク／ライトテーマ自動切替

## プロジェクト構成

```
ios/
├── EmojiCode/                          メインアプリ
│   ├── EmojiCodeApp.swift              アプリエントリ
│   ├── Info.plist
│   ├── EmojiCode.entitlements          App Group + Associated Domains
│   ├── Cipher/                         暗号エンジン（Share Extensionと共有）
│   │   ├── DefaultCipher.swift
│   │   ├── KanaUtils.swift
│   │   ├── Encoder.swift
│   │   ├── Decoder.swift
│   │   └── CipherUrlCodec.swift
│   ├── Storage/
│   │   └── CipherRepository.swift      App Group 経由で共有保存
│   └── Views/
│       ├── Theme.swift
│       ├── HomeView.swift
│       ├── CipherEditorView.swift
│       └── ShareSheet.swift
└── EmojiCodeShare/                     Share Extension
    ├── ShareViewController.swift       共有経由で起動 → 復号
    ├── MainInterface.storyboard
    ├── Info.plist
    └── EmojiCodeShare.entitlements
```

## ビルド方法

### 1. Xcode プロジェクトを作成

このリポジトリは Xcode プロジェクトファイル（.xcodeproj）を含みません。Xcode で「Create a new Xcode project」から:

1. **iOS > App** で `EmojiCode` という名前のプロジェクトを作成（SwiftUI、Swift）
2. **File > New > Target > Share Extension** で `EmojiCodeShare` ターゲットを追加
3. このリポジトリの `ios/EmojiCode/**` を本体ターゲットに、`ios/EmojiCodeShare/**` を Share Extension ターゲットに追加
4. `Cipher/*.swift` と `Storage/*.swift` は両方のターゲットに含める（Target Membership で両方をチェック）

### 2. App Group を設定

1. **Signing & Capabilities** で両ターゲットに `App Groups` を追加
2. グループID: `group.com.emojicode.app`

### 3. Associated Domains を設定（オプション）

Universal Links で `yu08083.github.io` を開くなら、本体ターゲットの **Signing & Capabilities** で:
- Associated Domains: `applinks:yu08083.github.io`

### 4. ビルド

```bash
xcodebuild -project EmojiCode.xcodeproj -scheme EmojiCode -sdk iphoneos
```

## 制限事項

**iOSではLINE通知の自動検知はOSの仕様上できません。**
これは脱獄でもしない限り回避不可能で、本アプリ固有の制限ではなく iOS全体の仕様です。

代わりに以下の方法で素早くアクセスできます:
- 共有メニュー（推奨）
- カスタムキーボード（将来対応予定）
- ショートカット連携（将来対応予定）

---

作者: [𝕏 @yu_](https://twitter.com/yu_)
