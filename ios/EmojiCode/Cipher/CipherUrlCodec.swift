import Foundation

/// Web版と完全互換のURL/QR共有フォーマット
public enum CipherUrlCodec {
    private static let version = 1

    /// URI文字列から暗号表を抽出
    public static func parse(from urlString: String) -> [String: String]? {
        let pattern = "[#?&]cipher=([^&\\s]+)"
        guard let regex = try? NSRegularExpression(pattern: pattern),
              let match = regex.firstMatch(in: urlString,
                                            range: NSRange(urlString.startIndex..., in: urlString)),
              let range = Range(match.range(at: 1), in: urlString) else {
            return nil
        }
        let encoded = String(urlString[range])
        return parsePayload(encoded)
    }

    private static func parsePayload(_ encoded: String) -> [String: String]? {
        // base64url → base64
        var b64 = encoded.replacingOccurrences(of: "-", with: "+")
            .replacingOccurrences(of: "_", with: "/")
        let mod = b64.count % 4
        if mod != 0 { b64 += String(repeating: "=", count: 4 - mod) }

        guard let data = Data(base64Encoded: b64),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let v = json["v"] as? Int, v == version else {
            return nil
        }
        var result = DefaultCipher.map
        if let diff = json["c"] as? [String: String] {
            for (k, v) in diff { result[k] = v }
        }
        if let extra = json["x"] as? [String: String] {
            for (k, v) in extra { result[k] = v }
        }
        return result
    }

    /// 暗号表をシェア用URLに変換
    public static func buildShareUrl(cipher: [String: String],
                                      baseUrl: String = "https://yu08083.github.io/mencon-emoji-henkan/") -> String {
        var diff = [String: String]()
        var extra = [String: String]()
        for (k, v) in cipher {
            if let def = DefaultCipher.map[k] {
                if def != v { diff[k] = v }
            } else {
                extra[k] = v
            }
        }
        let payload: [String: Any] = ["v": version, "c": diff, "x": extra]
        guard let data = try? JSONSerialization.data(withJSONObject: payload, options: [.sortedKeys]),
              let encoded = data.base64URLEncodedString() else {
            return baseUrl
        }
        return "\(baseUrl)#cipher=\(encoded)"
    }
}

private extension Data {
    func base64URLEncodedString() -> String? {
        return self.base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }
}
