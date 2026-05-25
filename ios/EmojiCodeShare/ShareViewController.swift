import UIKit
import Social
import MobileCoreServices
import UniformTypeIdentifiers

/// LINEメッセージを長押し → 共有 → 「メンコン絵文字で復号」 を選んだときに起動。
/// 受け取ったテキストを復号して表示する。
class ShareViewController: UIViewController {

    private let cipherRepo = CipherRepository()
    private var receivedText: String?

    private let cardView = UIView()
    private let titleLabel = UILabel()
    private let originalLabel = UILabel()
    private let arrowLabel = UILabel()
    private let decodedLabel = UILabel()
    private let copyButton = UIButton(type: .system)
    private let closeButton = UIButton(type: .system)

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(red: 0.06, green: 0.08, blue: 0.13, alpha: 0.6)

        buildLayout()
        loadIncomingText { [weak self] text in
            DispatchQueue.main.async { self?.processText(text) }
        }
    }

    private func buildLayout() {
        cardView.backgroundColor = UIColor.systemBackground
        cardView.layer.cornerRadius = 18
        cardView.layer.borderWidth = 1
        cardView.layer.borderColor = UIColor.systemGray4.cgColor
        cardView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(cardView)

        titleLabel.text = "🔓 メンコン絵文字"
        titleLabel.font = .systemFont(ofSize: 11, weight: .bold)
        titleLabel.textColor = .secondaryLabel
        titleLabel.translatesAutoresizingMaskIntoConstraints = false

        originalLabel.font = .systemFont(ofSize: 14)
        originalLabel.textColor = .tertiaryLabel
        originalLabel.numberOfLines = 4
        originalLabel.translatesAutoresizingMaskIntoConstraints = false

        arrowLabel.text = "↓"
        arrowLabel.font = .systemFont(ofSize: 14)
        arrowLabel.textColor = .tertiaryLabel
        arrowLabel.textAlignment = .center
        arrowLabel.translatesAutoresizingMaskIntoConstraints = false

        decodedLabel.font = .systemFont(ofSize: 18, weight: .medium)
        decodedLabel.textColor = .label
        decodedLabel.numberOfLines = 0
        decodedLabel.translatesAutoresizingMaskIntoConstraints = false

        copyButton.setTitle("結果をコピー", for: .normal)
        copyButton.titleLabel?.font = .systemFont(ofSize: 14, weight: .bold)
        copyButton.backgroundColor = UIColor(red: 0.83, green: 0.66, blue: 0.35, alpha: 1)
        copyButton.setTitleColor(.white, for: .normal)
        copyButton.layer.cornerRadius = 10
        copyButton.contentEdgeInsets = UIEdgeInsets(top: 10, left: 16, bottom: 10, right: 16)
        copyButton.translatesAutoresizingMaskIntoConstraints = false
        copyButton.addTarget(self, action: #selector(copyTapped), for: .touchUpInside)

        closeButton.setTitle("閉じる", for: .normal)
        closeButton.titleLabel?.font = .systemFont(ofSize: 14)
        closeButton.translatesAutoresizingMaskIntoConstraints = false
        closeButton.addTarget(self, action: #selector(closeTapped), for: .touchUpInside)

        [titleLabel, originalLabel, arrowLabel, decodedLabel, copyButton, closeButton].forEach {
            cardView.addSubview($0)
        }

        NSLayoutConstraint.activate([
            cardView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            cardView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            cardView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),

            titleLabel.topAnchor.constraint(equalTo: cardView.topAnchor, constant: 18),
            titleLabel.leadingAnchor.constraint(equalTo: cardView.leadingAnchor, constant: 18),

            originalLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 12),
            originalLabel.leadingAnchor.constraint(equalTo: cardView.leadingAnchor, constant: 18),
            originalLabel.trailingAnchor.constraint(equalTo: cardView.trailingAnchor, constant: -18),

            arrowLabel.topAnchor.constraint(equalTo: originalLabel.bottomAnchor, constant: 8),
            arrowLabel.leadingAnchor.constraint(equalTo: cardView.leadingAnchor),
            arrowLabel.trailingAnchor.constraint(equalTo: cardView.trailingAnchor),

            decodedLabel.topAnchor.constraint(equalTo: arrowLabel.bottomAnchor, constant: 8),
            decodedLabel.leadingAnchor.constraint(equalTo: cardView.leadingAnchor, constant: 18),
            decodedLabel.trailingAnchor.constraint(equalTo: cardView.trailingAnchor, constant: -18),

            copyButton.topAnchor.constraint(equalTo: decodedLabel.bottomAnchor, constant: 20),
            copyButton.leadingAnchor.constraint(equalTo: cardView.leadingAnchor, constant: 18),

            closeButton.centerYAnchor.constraint(equalTo: copyButton.centerYAnchor),
            closeButton.trailingAnchor.constraint(equalTo: cardView.trailingAnchor, constant: -18),

            cardView.bottomAnchor.constraint(equalTo: copyButton.bottomAnchor, constant: 18),
        ])
    }

    private func loadIncomingText(completion: @escaping (String?) -> Void) {
        guard let extensionContext = extensionContext,
              let item = extensionContext.inputItems.first as? NSExtensionItem,
              let attachments = item.attachments else {
            completion(nil); return
        }

        for provider in attachments {
            let textType = UTType.plainText.identifier
            let urlType = UTType.url.identifier
            if provider.hasItemConformingToTypeIdentifier(textType) {
                provider.loadItem(forTypeIdentifier: textType, options: nil) { (data, _) in
                    completion(data as? String)
                }
                return
            } else if provider.hasItemConformingToTypeIdentifier(urlType) {
                provider.loadItem(forTypeIdentifier: urlType, options: nil) { (data, _) in
                    completion((data as? URL)?.absoluteString)
                }
                return
            }
        }
        completion(nil)
    }

    private func processText(_ text: String?) {
        guard let text = text, !text.isEmpty else {
            decodedLabel.text = "テキストを取得できませんでした"
            originalLabel.isHidden = true
            arrowLabel.isHidden = true
            return
        }
        receivedText = text
        originalLabel.text = "元: \(text)"
        let cipher = cipherRepo.cipher
        if Decoder.containsCipherEmoji(text, cipher: cipher) {
            let decoded = Decoder.decode(text, cipher: cipher)
            decodedLabel.text = decoded
        } else {
            // 暗号でなければ逆方向（エンコード）を試す
            let encoded = Encoder.encode(text, cipher: cipher)
            if encoded != text {
                decodedLabel.text = encoded
                titleLabel.text = "🔒 暗号化"
            } else {
                decodedLabel.text = "暗号メッセージは見つかりませんでした"
            }
        }
    }

    @objc private func copyTapped() {
        guard let text = decodedLabel.text else { return }
        UIPasteboard.general.string = text
        copyButton.setTitle("✓ コピー完了", for: .normal)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.4) { [weak self] in
            self?.dismiss()
        }
    }

    @objc private func closeTapped() {
        dismiss()
    }

    private func dismiss() {
        extensionContext?.completeRequest(returningItems: nil, completionHandler: nil)
    }
}
