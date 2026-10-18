package com.example.vault

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Manages the user-granted "Onyx" folder (Storage Access Framework, persisted permission).
 * The user picks a location once; we create/reuse a sub-folder named "Onyx" inside it.
 */
class VaultFolderManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("onyx_vault", Context.MODE_PRIVATE)

    val grantedTreeUri: Uri?
        get() = prefs.getString(KEY_TREE, null)?.let(Uri::parse)?.takeIf { uri ->
            context.contentResolver.persistedUriPermissions.any { it.uri == uri && it.isWritePermission }
        }

    fun persistTree(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        prefs.edit().putString(KEY_TREE, uri.toString()).apply()
    }

    /** Returns (creating if needed) the official "Onyx" folder inside the granted tree. */
    fun onyxFolder(): DocumentFile? {
        val root = grantedTreeUri?.let { DocumentFile.fromTreeUri(context, it) } ?: return null
        return root.findFile(FOLDER_NAME)?.takeIf { it.isDirectory } ?: root.createDirectory(FOLDER_NAME)
    }

    /** Writes the vault bytes; returns the human-readable path or null. */
    fun writeVault(bytes: ByteArray, fileName: String = OnyxVault.defaultFileName()): String? {
        val folder = onyxFolder() ?: return null
        val file = folder.createFile(OnyxVault.MIME_TYPE, fileName) ?: return null
        context.contentResolver.openOutputStream(file.uri, "w")?.use { it.write(bytes) } ?: return null
        return "$FOLDER_NAME/${file.name ?: fileName}"
    }

    fun readBytes(uri: Uri): ByteArray =
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw OnyxVault.VaultException("تعذّر قراءة الملف")

    companion object {
        const val FOLDER_NAME = "Onyx"
        private const val KEY_TREE = "tree_uri"
    }
}
