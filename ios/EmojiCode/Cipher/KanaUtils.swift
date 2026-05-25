import Foundation

public enum KanaUtils {
    /// カタカナをひらがなに変換
    public static func katakanaToHiragana(_ s: String) -> String {
        var out = ""
        out.reserveCapacity(s.count)
        for ch in s.unicodeScalars {
            // ァ(U+30A1) - ヶ(U+30F6) → 0x60を引く
            if ch.value >= 0x30A1, ch.value <= 0x30F6 {
                let mapped = Unicode.Scalar(ch.value - 0x60)!
                out.unicodeScalars.append(mapped)
            } else {
                out.unicodeScalars.append(ch)
            }
        }
        return out
    }
}
