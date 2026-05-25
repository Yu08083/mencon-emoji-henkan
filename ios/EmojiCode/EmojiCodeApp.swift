import SwiftUI

@main
struct EmojiCodeApp: App {
    @StateObject private var repo = CipherRepository.shared

    var body: some Scene {
        WindowGroup {
            HomeView()
                .environmentObject(repo)
                .onOpenURL { url in
                    // URLハッシュ経由のインポート
                    if let imported = CipherUrlCodec.parse(from: url.absoluteString) {
                        repo.setCipher(imported)
                    }
                }
        }
    }
}
