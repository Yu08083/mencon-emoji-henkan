package com.emojicode.app.cipher

/** 絵文字を元の文字に戻す。 */
object Decoder {
    fun decode(input: String, cipher: Map<String, String>): String {
        if (input.isEmpty()) return ""

        val reverse = HashMap<String, String>(cipher.size)
        for ((kana, emoji) in cipher) reverse[emoji] = kana

        val dakutenReverse = HashMap<Char, Char>()
        for ((d, base) in KanaMaps.DAKUTEN) dakutenReverse[base] = d
        val handakutenReverse = HashMap<Char, Char>()
        for ((h, base) in KanaMaps.HANDAKUTEN) handakutenReverse[base] = h

        val segments = KanaUtils.toGraphemes(input)
        val result = StringBuilder()
        var i = 0
        while (i < segments.size) {
            val seg = segments[i]
            val next = if (i + 1 < segments.size) segments[i + 1] else null
            val kana = reverse[seg]

            if (kana != null && kana.length == 1) {
                val kCh = kana[0]
                if (next != null && next.length == 1 && next[0] in KanaMaps.DAKUTEN_MARKS
                    && dakutenReverse[kCh] != null) {
                    result.append(dakutenReverse[kCh])
                    i += 2; continue
                }
                if (next != null && next.length == 1 && next[0] in KanaMaps.HANDAKUTEN_MARKS
                    && handakutenReverse[kCh] != null) {
                    result.append(handakutenReverse[kCh])
                    i += 2; continue
                }
                result.append(kana)
            } else if (kana != null) {
                result.append(kana)
            } else {
                result.append(seg)
            }
            i++
        }
        return result.toString()
    }

    /** 暗号表のいずれかの絵文字が含まれているか */
    fun containsCipherEmoji(input: String, cipher: Map<String, String>): Boolean {
        for (e in cipher.values) if (input.contains(e)) return true
        return false
    }
}
