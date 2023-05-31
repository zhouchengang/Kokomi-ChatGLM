package com.zhouchengang.chatglmsample

import com.tencent.mmkv.MMKV

object SharedManager {
    const val MMKV_MAX = "MMKV_MAX"
    const val MMKV_TOP_P = "MMKV_TOP_P"
    const val MMKV_TEMP = "MMKV_TEMP"
    const val MMKV_URL = "MMKV_URL"
    const val MMKV_USE_STREAM = "MMKV_USE_STREAM"

    fun save(key: String, value: Int) {
        MMKV.defaultMMKV()?.encode(key, value)
    }

    fun get(key: String, default: Int): Int {
        return MMKV.defaultMMKV()?.decodeInt(key, default) ?: default
    }

    fun save(key: String, value: String) {
        MMKV.defaultMMKV()?.encode(key, value)
    }

    fun get(key: String, default: String): String {
        return MMKV.defaultMMKV()?.decodeString(key, default) ?: default
    }

    fun save(key: String, value: Boolean) {
        MMKV.defaultMMKV()?.encode(key, value)
    }

    fun get(key: String, default: Boolean): Boolean {
        return MMKV.defaultMMKV()?.decodeBool(key, default) ?: default
    }


    fun save(key: String, value: Float) {
        MMKV.defaultMMKV()?.encode(key, value)
    }

    fun get(key: String, default: Float): Float {
        return MMKV.defaultMMKV()?.decodeFloat(key, default) ?: default
    }


}