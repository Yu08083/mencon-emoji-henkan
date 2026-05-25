import Foundation

public enum Encoder {
    public static func encode(_ input: String, cipher: [String: String]) -> String {
        guard !input.isEmpty else { return "" }
        let hira = KanaUtils.katakanaToHiragana(input)
        var result = ""

        for ch in hira {
            // 小書きかなを通常化
            let base: Character = KanaMaps.small[ch] ?? ch
            let key = String(base)

            if let emoji = cipher[key] {
                result += emoji
            } else if let cleanBase = KanaMaps.dakuten[base],
                      let emoji = cipher[String(cleanBase)] {
                result += emoji + "\""
            } else if let cleanBase = KanaMaps.handakuten[base],
                      let emoji = cipher[String(cleanBase)] {
                result += emoji + "'"
            } else if ch == "-" {
                result += "ー"
            } else {
                result.append(ch)
            }
        }
        return result
    }
}
