# 🧩 メンコン絵文字変換ツール — ブラウザ拡張

LINE Web版を開いている間、暗号メッセージを**自動でその場に翻訳**して表示します。

## 機能

- 🔓 **自動翻訳** — メッセージが届くと暗号文の直下に復号バナーを差し込み表示
- ⌨️ **入力欄でも変換** — メッセージ作成中に `Alt + E` を押すと、書いた文字をそのまま暗号化
- 🖱️ **右クリックメニュー** — 任意のページで選択テキストを復号/暗号化
- 🔧 **暗号表のカスタマイズ** — ポップアップから五十音すべて編集可能
- ☁️ **端末間同期** — `chrome.storage.sync` でブラウザにログイン中の全端末で同じ暗号表
- 📥 **URL取り込み** — Web版で作った暗号表のURLを貼り付けるだけで読み込み
- 🌗 **ダーク/ライト** — システム設定に追随

## インストール手順（開発者モード）

1. このリポジトリの `extension/` ディレクトリを取得
2. Chrome で `chrome://extensions/` を開く
3. 右上の「デベロッパーモード」をON
4. 「パッケージ化されていない拡張機能を読み込む」をクリック
5. `extension/` ディレクトリを選択
6. LINE Web版（`https://chat.line.biz/` または `https://line.me/`）を開けば自動的に動作

## ファイル構成

```
extension/
├── manifest.json              MV3マニフェスト
├── background.js              service worker（右クリックメニュー）
├── popup.html / popup.css / popup.js   拡張アイコンのポップアップUI
├── content/
│   ├── content.js             LINE Web版を監視して翻訳バナーを注入
│   └── content.css            注入用スタイル（全プロパティに !important）
├── shared/
│   ├── cipher.js              暗号エンジン（Web/Android/iOSと完全互換）
│   ├── storage.js             chrome.storage.sync ラッパー
│   └── url-codec.js           共有URL/QR エンコード・パース
└── icons/
    ├── icon-16.png
    ├── icon-32.png
    ├── icon-48.png
    └── icon-128.png
```

## プライバシー

- 通信は**一切行いません**。暗号表は端末内（chrome.storage.sync）に保存
- LINEのメッセージ内容を外部送信することはありません
- 必要な権限:
  - `storage` — 暗号表の保存
  - `contextMenus` — 右クリックメニュー
  - `activeTab` + LINEドメインへの host permission — DOM注入と監視

## 仕組み

`MutationObserver` で LINE Web のDOM変更を監視し、暗号表に登録された絵文字を含むテキストを検知すると、
そのメッセージブロックの直後に翻訳バナーを挿入します。バナーは初回のみ注入され、再描画でも重複しません。

LINEのDOM構造に依存しないよう、特定のクラス名やセレクタには頼らず、
**「暗号絵文字を含む短いテキストノード」** をヒューリスティックで拾うアプローチを採用しています。

## Firefox / Edge での動作

Manifest V3 互換のため、そのまま動くはずです。Firefox はまだ MV3 の一部仕様に差異があるので、
将来必要に応じて分岐するかもしれません。

## 既知の制限

- LINEのDOM構造が大幅に変わると、メッセージブロック検出が外れることがあります
- スマホ版LINEには使えません（あくまでWeb版用）→ そちらはAndroid/iOSアプリをお使いください

---

作者: [𝕏 @yu_](https://twitter.com/yu_)
