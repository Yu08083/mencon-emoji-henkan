package com.emojicode.app.cipher

/**
 * 文字列をひらがな前提でエンコードする。
 * Web版encoder.jsと挙動を一致させること。
 */
object Encoder {
    fun encode(input: String, cipher: Map<String, String>): String {
        if (input.isEmpty()) return ""
        val hira = KanaUtils.katakanaToHiragana(input)
        val result = StringBuilder()

        var i = 0
        while (i < hira.length) {
            // サロゲートペア/絵文字を考慮しコードポイント単位で進める
            val cp = hira.codePointAt(i)
            val chCount = Character.charCount(cp)
            val token = String(Character.toChars(cp))

            // 単一文字としてのキー候補
            val base = if (token.length == 1) {
                KanaMaps.SMALL[token[0]]?.toString() ?: token
            } else token

            val emoji = cipher[base]
            when {
                emoji != null -> result.append(emoji)
                base.length == 1 && KanaMaps.DAKUTEN[base[0]] != null
                    && cipher[KanaMaps.DAKUTEN[base[0]].toString()] != null -> {
                    result.append(cipher[KanaMaps.DAKUTEN[base[0]].toString()]).append('"')
                }
                base.length == 1 && KanaMaps.HANDAKUTEN[base[0]] != null
                    && cipher[KanaMaps.HANDAKUTEN[base[0]].toString()] != null -> {
                    result.append(cipher[KanaMaps.HANDAKUTEN[base[0]].toString()]).append('\'')
                }
                token == "-" -> result.append("ー")
                else -> result.append(token)
            }
            i += chCount
        }
        return result.toString()
    }
}
