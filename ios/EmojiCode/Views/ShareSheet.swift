import SwiftUI
import UIKit

struct ShareSheet: UIViewControllerRepresentable {
    let cipher: [String: String]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        let url = CipherUrlCodec.buildShareUrl(cipher: cipher)
        let items: [Any] = [
            "メンコン絵文字で作った暗号表です:\n\(url)",
            URL(string: url) as Any,
        ].compactMap { $0 }
        return UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}
