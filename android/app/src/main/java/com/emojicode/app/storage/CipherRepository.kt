package com.emojicode.app.storage

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.emojicode.app.cipher.DefaultCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "emoji_code_pro")

/**
 * 暗号表のリポジトリ。
 * デフォルト差分のみJSON保存。
 */
class CipherRepository(private val context: Context) {
    private object K {
        val CIPHER_DIFF = stringPreferencesKey("cipher_diff")
        val FAVORITES = stringPreferencesKey("favorites")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val AUTO_DETECT = booleanPreferencesKey("auto_detect")
        val THEME = stringPreferencesKey("theme")
    }

    val cipher: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[K.CIPHER_DIFF] ?: return@map DefaultCipher.MAP
        try {
            val diff = JSONObject(raw)
            val out = HashMap(DefaultCipher.MAP)
            diff.keys().forEach { k -> out[k] = diff.getString(k) }
            out
        } catch (e: Exception) { DefaultCipher.MAP }
    }

    suspend fun currentCipher(): Map<String, String> = cipher.first()

    suspend fun setCipher(cipher: Map<String, String>) {
        val diff = JSONObject()
        for ((k, v) in cipher) {
            val def = DefaultCipher.MAP[k]
            // デフォルトと同じならスキップ。デフォルトに無いキーは保存。
            if (def == v) continue
            diff.put(k, v)
        }
        context.dataStore.edit { it[K.CIPHER_DIFF] = diff.toString() }
    }

    suspend fun resetCipher() {
        context.dataStore.edit { it.remove(K.CIPHER_DIFF) }
    }

    val overlayEnabled: Flow<Boolean> = context.dataStore.data.map { it[K.OVERLAY_ENABLED] ?: true }
    suspend fun setOverlayEnabled(value: Boolean) {
        context.dataStore.edit { it[K.OVERLAY_ENABLED] = value }
    }

    val autoDetect: Flow<Boolean> = context.dataStore.data.map { it[K.AUTO_DETECT] ?: true }
    suspend fun setAutoDetect(value: Boolean) {
        context.dataStore.edit { it[K.AUTO_DETECT] = value }
    }

    val theme: Flow<String> = context.dataStore.data.map { it[K.THEME] ?: "system" }
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[K.THEME] = theme }
    }

    // ===== お気に入り =====
    data class Favorite(val name: String, val cipher: Map<String, String>, val createdAt: Long)

    val favorites: Flow<List<Favorite>> = context.dataStore.data.map { prefs ->
        val raw = prefs[K.FAVORITES] ?: return@map emptyList()
        try {
            val arr = org.json.JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val cMap = HashMap<String, String>()
                val cObj = obj.getJSONObject("c")
                cObj.keys().forEach { k -> cMap[k] = cObj.getString(k) }
                Favorite(obj.getString("name"), cMap, obj.optLong("at"))
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addFavorite(name: String, cipher: Map<String, String>) {
        val list = favorites.first().toMutableList()
        val existing = list.indexOfFirst { it.name == name }
        val fav = Favorite(name, cipher, System.currentTimeMillis())
        if (existing >= 0) list[existing] = fav else list.add(fav)
        writeFavorites(list)
    }

    suspend fun deleteFavorite(index: Int) {
        val list = favorites.first().toMutableList()
        if (index in list.indices) {
            list.removeAt(index); writeFavorites(list)
        }
    }

    private suspend fun writeFavorites(list: List<Favorite>) {
        val arr = org.json.JSONArray()
        list.forEach { fav ->
            val c = JSONObject()
            fav.cipher.forEach { (k, v) -> c.put(k, v) }
            arr.put(JSONObject().apply {
                put("name", fav.name); put("c", c); put("at", fav.createdAt)
            })
        }
        context.dataStore.edit { it[K.FAVORITES] = arr.toString() }
    }
}
