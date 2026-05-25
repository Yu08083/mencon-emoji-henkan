package com.emojicode.app.cipher

import java.text.BreakIterator
import java.util.Locale

object KanaUtils {
    /** カタカナをひらがなに */
    fun katakanaToHiragana(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
            sb.append(
                if (ch in '\u30A1'..'\u30F6') (ch.code - 0x60).toChar()
                else ch
            )
        }
        return sb.toString()
    }

    /** 絵文字を1つの単位として分割（書記素クラスタ） */
    fun toGraphemes(text: String): List<String> {
        val it = BreakIterator.getCharacterInstance(Locale.JAPANESE)
        it.setText(text)
        val out = mutableListOf<String>()
        var start = it.first()
        var end = it.next()
        while (end != BreakIterator.DONE) {
            out.add(text.substring(start, end))
            start = end
            end = it.next()
        }
        return out
    }
}
