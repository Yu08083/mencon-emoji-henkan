package com.emojicode.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emojicode.app.cipher.DefaultCipher
import com.emojicode.app.cipher.Decoder
import com.emojicode.app.cipher.Encoder
import com.emojicode.app.storage.CipherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(val repo: CipherRepository) : ViewModel() {

    enum class Mode { DECODE, ENCODE }

    private val _cipher = MutableStateFlow<Map<String, String>>(DefaultCipher.MAP)
    val cipher: StateFlow<Map<String, String>> = _cipher.asStateFlow()

    private val _mode = MutableStateFlow(Mode.DECODE)
    val mode: StateFlow<Mode> = _mode.asStateFlow()

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _output = MutableStateFlow("")
    val output: StateFlow<String> = _output.asStateFlow()

    private val _showImportConfirm = MutableStateFlow<Map<String, String>?>(null)
    val showImportConfirm: StateFlow<Map<String, String>?> = _showImportConfirm.asStateFlow()

    init {
        viewModelScope.launch {
            repo.cipher.collect { c ->
                _cipher.value = c
                recompute()
            }
        }
        // 起動時にpendingがあれば確認ダイアログ
        pendingImport?.let {
            _showImportConfirm.value = it
            pendingImport = null
        }
    }

    fun setMode(m: Mode) {
        _mode.value = m
        _input.value = ""
        _output.value = ""
    }

    fun updateInput(text: String) {
        _input.value = text
        recompute()
    }

    fun recompute() {
        val c = _cipher.value
        _output.value = when (_mode.value) {
            Mode.DECODE -> Decoder.decode(_input.value, c)
            Mode.ENCODE -> Encoder.encode(_input.value, c)
        }
    }

    fun acceptImport(cipher: Map<String, String>) {
        viewModelScope.launch {
            repo.setCipher(cipher)
            _showImportConfirm.value = null
        }
    }

    fun rejectImport() {
        _showImportConfirm.value = null
    }

    companion object {
        // Activity → ViewModelに値を渡すためのスタティック中継
        // （起動Intentでしか使わないのでシンプルに実装）
        @Volatile
        var pendingImport: Map<String, String>? = null
    }

    class Factory(private val ctx: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(CipherRepository(ctx)) as T
        }
    }
}
