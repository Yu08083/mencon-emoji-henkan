import Foundation
import Combine

/// 暗号表のリポジトリ。App Group 経由で本体・Share Extensionで共有する。
public final class CipherRepository: ObservableObject {

    public static let shared = CipherRepository()

    private let userDefaults: UserDefaults

    @Published public private(set) var cipher: [String: String] = DefaultCipher.map
    @Published public private(set) var favorites: [Favorite] = []

    private let kCipherDiff = "cipher_diff"
    private let kFavorites = "favorites"

    public init() {
        // App Group がない場合は standardにフォールバック
        let groupId = "group.com.emojicode.app"
        self.userDefaults = UserDefaults(suiteName: groupId) ?? .standard
        load()
    }

    public struct Favorite: Codable, Identifiable {
        public let id: String
        public let name: String
        public let cipher: [String: String]
        public let createdAt: Date

        public init(name: String, cipher: [String: String]) {
            self.id = UUID().uuidString
            self.name = name
            self.cipher = cipher
            self.createdAt = Date()
        }
    }

    private func load() {
        // 暗号表（差分）の読み込み
        if let data = userDefaults.data(forKey: kCipherDiff),
           let diff = try? JSONDecoder().decode([String: String].self, from: data) {
            var merged = DefaultCipher.map
            for (k, v) in diff { merged[k] = v }
            cipher = merged
        }
        // お気に入り
        if let data = userDefaults.data(forKey: kFavorites),
           let favs = try? JSONDecoder().decode([Favorite].self, from: data) {
            favorites = favs
        }
    }

    public func setCipher(_ new: [String: String]) {
        cipher = new
        var diff = [String: String]()
        for (k, v) in new {
            if let def = DefaultCipher.map[k] {
                if def != v { diff[k] = v }
            } else {
                diff[k] = v
            }
        }
        if let data = try? JSONEncoder().encode(diff) {
            userDefaults.set(data, forKey: kCipherDiff)
        }
    }

    public func resetCipher() {
        cipher = DefaultCipher.map
        userDefaults.removeObject(forKey: kCipherDiff)
    }

    public func addFavorite(_ name: String, cipher: [String: String]) {
        if let idx = favorites.firstIndex(where: { $0.name == name }) {
            favorites[idx] = Favorite(name: name, cipher: cipher)
        } else {
            favorites.append(Favorite(name: name, cipher: cipher))
        }
        persistFavorites()
    }

    public func deleteFavorite(at index: Int) {
        guard favorites.indices.contains(index) else { return }
        favorites.remove(at: index)
        persistFavorites()
    }

    private func persistFavorites() {
        if let data = try? JSONEncoder().encode(favorites) {
            userDefaults.set(data, forKey: kFavorites)
        }
    }
}
