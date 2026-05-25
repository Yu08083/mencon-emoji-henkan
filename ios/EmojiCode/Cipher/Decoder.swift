import Foundation

public enum Decoder {
    public static func decode(_ input: String, cipher: [String: String]) -> String {
        guard !input.isEmpty else { return "" }

        var reverse = [String: String]()
        for (kana, emoji) in cipher { reverse[emoji] = kana }

        var dakutenReverse = [Character: Character]()
        for (d, base) in KanaMaps.dakuten { dakutenReverse[base] = d }
        var handakutenReverse = [Character: Character]()
        for (h, base) in KanaMaps.handakuten { handakutenReverse[base] = h }

        // Swiftの Character は書記素クラスタとして扱われるので、
        // 絵文字も含めて1単位で正しく取れる。
        let segments: [Character] = Array(input)
        var result = ""
        var i = 0
        while i < segments.count {
            let seg = String(segments[i])
            let next: Character? = i + 1 < segments.count ? segments[i + 1] : nil

            if let kana = reverse[seg], kana.count == 1 {
                let kCh = kana.first!
                if let n = next, KanaMaps.dakutenMarks.contains(n),
                   let dakuten = dakutenReverse[kCh] {
                    result.append(dakuten)
                    i += 2
                    continue
                }
                if let n = next, KanaMaps.handakutenMarks.contains(n),
                   let handakuten = handakutenReverse[kCh] {
                    result.append(handakuten)
                    i += 2
                    continue
                }
                result.append(kana)
            } else if let kana = reverse[seg] {
                result.append(kana)
            } else {
                result.append(seg)
            }
            i += 1
        }
        return result
    }

    public static func containsCipherEmoji(_ input: String, cipher: [String: String]) -> Bool {
        for e in cipher.values where input.contains(e) {
            return true
        }
        return false
    }
}
