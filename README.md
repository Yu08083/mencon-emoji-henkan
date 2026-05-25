<div align="center">

<img src="favicon/apple-touch-icon.png" width="120" alt="メンコン絵文字変換ツール" />

# 💕 メンコン絵文字変換ツール

**LINEで使える絵文字暗号メーカー**

ひらがな・カタカナを絵文字に変換、絵文字メッセージを瞬時に翻訳。
Web・Android・iOS・ブラウザ拡張すべてに対応。

🌐 **[サイトを開く →](https://yu08083.github.io/mencon-emoji-henkan/)**

[![Twitter](https://img.shields.io/badge/作者-%40yu___-FF4D97?style=for-the-badge&logo=x&logoColor=white)](https://twitter.com/yu_)
[![License](https://img.shields.io/badge/license-MIT-9A5BFF?style=for-the-badge)](LICENSE)
[![GitHub Pages](https://img.shields.io/badge/Hosted-GitHub%20Pages-FF7EB0?style=for-the-badge&logo=github)](https://yu08083.github.io/mencon-emoji-henkan/)

</div>

---

## 4プラットフォーム対応

| プラットフォーム | 主な体験 | 自動検知 |
|---|---|---|
| 🌐 **Web** (github.io) | サイトを開いて即変換 / 暗号表編集 / QR共有 | — |
| 📱 **Android** | LINE通知を**自動検知**してフローティング窓で表示 | ✅ 完全自動 |
| 📱 **iOS** | メッセージ長押し → 共有 → メンコン絵文字 で復号 | 半自動（OS制約） |
| 🧩 **ブラウザ拡張** | LINE Web版でメッセージを自動翻訳して埋め込み表示 | ✅ |

### なぜiOSだけ完全自動が無理？

iOS は **他アプリの通知を読み取るAPIを一切公開していません**（脱獄しない限り回避不可能）。
Apple のセキュリティ設計上の仕様で、本ツール固有の制限ではありません。
代わりに Share Extension（共有メニュー）で「ワンタップで復号」できるようにしてあります。

---

## ✨ 機能

- 💕 **ひらがな ⇄ 絵文字** の双方向変換
- 🎨 **暗号表のカスタマイズ** — 五十音すべて自由に変更
- ⭐ **お気に入り保存** — 複数の暗号表をプリセット化
- 🔗 **QR / URL 共有** — 全プラットフォーム互換フォーマット
- 📜 **変換履歴** — 直近20件を自動保存
- 🌗 **ダーク / ライトテーマ** — システム連動
- 🔒 **完全ローカル動作** — 通信なし、データ流出ゼロ
- 📲 **PWAインストール対応** — ホーム画面に追加可能

---

## ディレクトリ構成

```
mencon-emoji-henkan/
├── index.html, css/, js/        Web版（github.io にデプロイされる本体）
├── manifest.webmanifest         PWA マニフェスト
├── robots.txt, sitemap.xml      SEO 用
├── favicon/                     10サイズのfavicon + Apple/Androidアイコン
├── assets/                      OG image, Twitter card
├── .well-known/                 Universal Links / App Links 検証ファイル
├── android/                     Android アプリ (Kotlin + Jetpack Compose)
├── ios/                         iOS アプリ + Share Extension (Swift + SwiftUI)
├── extension/                   Chrome / Edge 拡張 (Manifest V3)
├── docs/                        ドキュメント
└── .github/workflows/           CI/CD
```

各プラットフォームの詳細は `*/README.md` を参照してください。

---

## 🚀 クイックスタート

### Web版

このリポジトリをGitHub上でフォーク → `Settings > Pages > Source` を「GitHub Actions」に設定。
次回pushで自動的に `https://<USERNAME>.github.io/mencon-emoji-henkan/` にデプロイされます。

ローカル確認：

```bash
python3 -m http.server 8000
# http://localhost:8000 を開く
```

### Android

```bash
cd android
./gradlew assembleRelease
# 出力: app/build/outputs/apk/release/app-release.apk
```

詳細: [`android/README.md`](android/README.md)

### iOS

Xcode で `ios/` 配下のSwiftファイルを取り込んでビルド。App Group と Associated Domains の設定が必要です。
詳細: [`ios/README.md`](ios/README.md)

### ブラウザ拡張

1. Chrome の `chrome://extensions/` を開く
2. デベロッパーモードON → 「パッケージ化されていない拡張機能を読み込む」
3. `extension/` ディレクトリを選択

詳細: [`extension/README.md`](extension/README.md)

---

## 🛠️ 設計の特徴

### 暗号エンジンは4プラットフォーム共通仕様

JavaScript、Kotlin、Swift で**完全に同じ挙動**になるよう実装。
同じ入力に対して同じ出力が出ます。

これがあるので、Web版で作った暗号表をAndroidでQRスキャンしてそのまま使うのも、
Android で作ったのを Web で開くのもシームレス。

### 共有URLフォーマット

```
https://yu08083.github.io/mencon-emoji-henkan/#cipher=BASE64URL(JSON({v,c,x}))
```

- `v` バージョン
- `c` デフォルト暗号表との**差分のみ**（容量最小化）
- `x` デフォルトに無いキー（特殊文字の追加分）

カスタム暗号表でも120〜200バイト程度。QRコードのバージョン3〜5に収まります。

### プライバシー優先

- 通信は一切なし。暗号表は端末内のみ
- 通知の内容を外部送信することは**絶対にしません**
- ブラウザ拡張は LINEドメイン以外で動きません

---

## 📊 SEO・メタ情報

- **タイトル**: メンコン絵文字変換ツール｜LINEで使える絵文字暗号メーカー
- **構造化データ**: JSON-LD WebApplication schema
- **Open Graph**: og:image (1200×630)、og:type=website
- **Twitter Card**: summary_large_image（@yu_ を creator として設定）
- **PWA**: ホーム画面追加対応、theme-color設定
- **モバイル最適化**: viewport-fit=cover、apple-mobile-web-app

---

## 開発時の注意

### 暗号表データを更新する場合

4箇所すべてを同期させてください：

1. `js/data/default-cipher.js` (Web)
2. `android/app/src/main/java/com/emojicode/app/cipher/DefaultCipher.kt`
3. `ios/EmojiCode/Cipher/DefaultCipher.swift`
4. `extension/shared/cipher.js`

### Universal Links / App Links を本番運用するには

`.well-known/apple-app-site-association` の `TEAMID` と
`.well-known/assetlinks.json` の `sha256_cert_fingerprints` を、
実際のApple Developer Team ID と Android アプリ署名証明書の SHA-256 で置き換えてください。

---

## 🎨 デザイン

- **メインカラー**: ホットピンク (#FF4D97) × ラベンダーパープル (#9A5BFF)
- **アクセント**: ゴールド (#FFD84A)
- **フォント**: Mochiy Pop One, M PLUS Rounded 1c（丸ゴシック）
- **テクスチャ**: SVGドットでキラキラ、放射状グラデーションでドリーミー

---

## 👤 作者

[![@yu_](https://img.shields.io/badge/𝕏-%40yu___-FF4D97?style=for-the-badge)](https://twitter.com/yu_)

不具合・要望はTwitterまでお気軽に 💕

---

## 📜 ライセンス

MIT License — 詳細は [LICENSE](LICENSE) を参照
