package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.model.OnyxAccount
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local persistence for accounts, encrypted at rest with Android Keystore
 * (EncryptedSharedPreferences: AES-256-SIV keys / AES-256-GCM values, master key in AndroidKeyStore).
 *
 * Migration: data written by earlier versions into the plain "onyx_accounts" prefs is moved into the
 * encrypted store once, then wiped from the plain file.
 */
class AccountsStore(context: Context) {
    private val appContext = context.applicationContext

    private val prefs: SharedPreferences by lazy { openEncrypted() }

    private fun openEncrypted(): SharedPreferences {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val secure = EncryptedSharedPreferences.create(
            appContext,
            SECURE_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        migrateLegacyPlainPrefs(secure)
        return secure
    }

    private fun migrateLegacyPlainPrefs(secure: SharedPreferences) {
        val legacy = appContext.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
        val raw = legacy.getString(KEY, null) ?: return
        if (!secure.contains(KEY)) {
            secure.edit().putString(KEY, raw).commit()
            Log.i(TAG, "Migrated legacy plain-text accounts into the encrypted store")
        }
        legacy.edit().clear().commit()
    }

    fun load(): List<OnyxAccount> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                OnyxAccount(
                    id = o.getString("id"), issuer = o.optString("issuer"), accountName = o.optString("accountName"),
                    secret = o.getString("secret"), algorithm = o.optString("algorithm", "SHA-1"),
                    digits = o.optInt("digits", 6), period = o.optInt("period", 30), addedDate = o.optString("addedDate", "")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(accounts: List<OnyxAccount>) {
        val arr = JSONArray()
        accounts.forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id); put("issuer", a.issuer); put("accountName", a.accountName); put("secret", a.secret)
                put("algorithm", a.algorithm); put("digits", a.digits); put("period", a.period); put("addedDate", a.addedDate)
            })
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object {
        const val TAG = "AccountsStore"
        const val SECURE_PREFS = "onyx_accounts_secure"
        const val LEGACY_PREFS = "onyx_accounts"
        const val KEY = "accounts_json"
    }
}
