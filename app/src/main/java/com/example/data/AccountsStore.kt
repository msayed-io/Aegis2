package com.example.data

import android.content.Context
import com.example.model.AegisAccount
import org.json.JSONArray
import org.json.JSONObject

/** Local persistence for accounts (app-private storage) so imports/additions survive restarts. */
class AccountsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("onyx_accounts", Context.MODE_PRIVATE)

    fun load(): List<AegisAccount> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                AegisAccount(
                    id = o.getString("id"), issuer = o.optString("issuer"), accountName = o.optString("accountName"),
                    secret = o.getString("secret"), algorithm = o.optString("algorithm", "SHA-1"),
                    digits = o.optInt("digits", 6), period = o.optInt("period", 30), addedDate = o.optString("addedDate", "")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(accounts: List<AegisAccount>) {
        val arr = JSONArray()
        accounts.forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id); put("issuer", a.issuer); put("accountName", a.accountName); put("secret", a.secret)
                put("algorithm", a.algorithm); put("digits", a.digits); put("period", a.period); put("addedDate", a.addedDate)
            })
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object { const val KEY = "accounts_json" }
}
