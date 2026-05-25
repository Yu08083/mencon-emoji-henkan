package com.emojicode.app.cipher

import android.util.Base64
import org.json.JSONObject
import android.net.Uri

/**
 * Web版と同じURLフォーマット:
 *   https://*.github.io/mencon-emoji-henkan/#cipher=BASE64URL(JSON({v:1, c:差分, x:特殊}))
 *
 * カスタムスキームでも同様:
 *   emojicode://import?cipher=BASE64URL(...)
 */
object CipherUrlCodec {
    private const val VERSION = 1

    /** URI / 任意のテキストから暗号表(差分込み)を抽出。なければ null */
    fun parseFromUri(uri: Uri?): Map<String, String>? {
        if (uri == null) return null
        val raw = uri.toString()
        val regex = Regex("[#?&]cipher=([^&\\s]+)")
        val m = regex.find(raw) ?: return null
        return parsePayload(m.groupValues[1])
    }

    fun parseFromText(text: String?): Map<String, String>? {
        if (text.isNullOrBlank()) return null
        val regex = Regex("[#?&]cipher=([^&\\s]+)")
        val m = regex.find(text) ?: return null
        return parsePayload(m.groupValues[1])
    }

    private fun parsePayload(encoded: String): Map<String, String>? {
        return try {
            val padded = encoded.padEnd(encoded.length + (4 - encoded.length % 4) % 4, '=')
            val bytes = Base64.decode(padded, Base64.URL_SAFE)
            val json = JSONObject(String(bytes, Charsets.UTF_8))
            if (json.optInt("v") != VERSION) return null
            val result = HashMap(DefaultCipher.MAP)
            json.optJSONObject("c")?.let { diff ->
                diff.keys().forEach { k -> result[k] = diff.getString(k) }
            }
            json.optJSONObject("x")?.let { extra ->
                extra.keys().forEach { k -> result[k] = extra.getString(k) }
            }
            result
        } catch (e: Exception) { null }
    }

    fun buildShareUrl(cipher: Map<String, String>, baseUrl: String): String {
        val diff = JSONObject()
        val extra = JSONObject()
        for ((k, v) in cipher) {
            val def = DefaultCipher.MAP[k]
            if (def != null) {
                if (def != v) diff.put(k, v)
            } else extra.put(k, v)
        }
        val payload = JSONObject().apply {
            put("v", VERSION); put("c", diff); put("x", extra)
        }.toString()
        val encoded = Base64.encodeToString(
            payload.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        return "$baseUrl#cipher=$encoded"
    }
}
