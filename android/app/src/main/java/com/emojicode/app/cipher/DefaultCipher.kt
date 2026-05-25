package com.emojicode.app.cipher

/** Web版とまったく同じデフォルト暗号表 */
object DefaultCipher {
    val MAP: Map<String, String> = mapOf(
        "あ" to "🍨", "い" to "🦑", "う" to "🐴", "え" to "🖼️", "お" to "👹",
        "か" to "🦟", "き" to "🌳", "く" to "🐻", "け" to "⚔️", "こ" to "🐨",
        "さ" to "🈂️", "し" to "4️⃣", "す" to "🍉", "せ" to "🪭", "そ" to "🛷",
        "た" to "🥁", "ち" to "🩸", "つ" to "🌙", "て" to "✋", "と" to "🍅",
        "な" to "🍐", "に" to "✌️", "ぬ" to "🧸", "ね" to "🐱", "の" to "🧠",
        "は" to "🦷", "ひ" to "🔥", "ふ" to "🚢", "へ" to "🐍", "ほ" to "📕",
        "ま" to "😷", "み" to "💧", "む" to "💜", "め" to "👀", "も" to "🍑",
        "や" to "🗻", "ゆ" to "🏹", "よ" to "🪀",
        "ら" to "🎒", "り" to "🍎", "る" to "🇷🇴", "れ" to "🧱", "ろ" to "6️⃣",
        "わ" to "🦎", "を" to "🎵", "ん" to "🆖",
    )

    val KANA_ORDER: List<String> = listOf(
        "あ","い","う","え","お",
        "か","き","く","け","こ",
        "さ","し","す","せ","そ",
        "た","ち","つ","て","と",
        "な","に","ぬ","ね","の",
        "は","ひ","ふ","へ","ほ",
        "ま","み","む","め","も",
        "や","", "ゆ","", "よ",
        "ら","り","る","れ","ろ",
        "わ","", "を","", "ん",
    )
}

object KanaMaps {
    /** 濁点付き → 清音 */
    val DAKUTEN: Map<Char, Char> = mapOf(
        'が' to 'か','ぎ' to 'き','ぐ' to 'く','げ' to 'け','ご' to 'こ',
        'ざ' to 'さ','じ' to 'し','ず' to 'す','ぜ' to 'せ','ぞ' to 'そ',
        'だ' to 'た','ぢ' to 'ち','づ' to 'つ','で' to 'て','ど' to 'と',
        'ば' to 'は','び' to 'ひ','ぶ' to 'ふ','べ' to 'へ','ぼ' to 'ほ',
        'ヴ' to 'う',
    )

    /** 半濁点付き → 清音 */
    val HANDAKUTEN: Map<Char, Char> = mapOf(
        'ぱ' to 'は','ぴ' to 'ひ','ぷ' to 'ふ','ぺ' to 'へ','ぽ' to 'ほ',
    )

    /** 小書きかな → 通常 */
    val SMALL: Map<Char, Char> = mapOf(
        'ぁ' to 'あ','ぃ' to 'い','ぅ' to 'う','ぇ' to 'え','ぉ' to 'お',
        'っ' to 'つ','ゃ' to 'や','ゅ' to 'ゆ','ょ' to 'よ','ゎ' to 'わ',
    )

    val DAKUTEN_MARKS = setOf('"', '\u201C', '\u201D', '゛')
    val HANDAKUTEN_MARKS = setOf('\'', '\u2018', '\u2019', '゜')
}
