package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.MigrationPayloadDecoder
import com.example.crypto.TotpGenerator
import com.example.model.OnyxAccount
import com.example.security.SecurityPreferences
import com.example.data.AccountsStore
import com.example.vault.OnyxVault
import com.example.vault.VaultFolderManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class OnyxScreen {
    object Main : OnyxScreen()
    object Lock : OnyxScreen()
    data class CodeDetail(val accountId: String) : OnyxScreen()
    object Search : OnyxScreen()
    object EmptyState : OnyxScreen()
    object QrScanner : OnyxScreen()
    object MigrationScanner : OnyxScreen()
    object ManualEntry : OnyxScreen()
    data class EditAccount(val accountId: String) : OnyxScreen()
    object Licenses : OnyxScreen()
    object Settings : OnyxScreen()
    object BackupSync : OnyxScreen()
    object Welcome : OnyxScreen()
    object TotpExplanation : OnyxScreen()
    object AddFirstCode : OnyxScreen()
}

class OnyxViewModel(application: Application) : AndroidViewModel(application) {

    private val securityPrefs = SecurityPreferences(application)
    private val accountsStore = AccountsStore(application)
    val vaultFolders = VaultFolderManager(application)

    private val _accounts = MutableStateFlow<List<OnyxAccount>>(emptyList())
    val accounts: StateFlow<List<OnyxAccount>> = _accounts.asStateFlow()

    // Start order: first launch -> onboarding; locked -> Lock; otherwise Main.
    private val _currentScreen = MutableStateFlow<OnyxScreen>(
        when {
            securityPrefs.isFirstLaunch -> OnyxScreen.Welcome
            securityPrefs.biometricEnabled -> OnyxScreen.Lock
            else -> OnyxScreen.Main
        }
    )
    val currentScreen: StateFlow<OnyxScreen> = _currentScreen.asStateFlow()

    private val _isAddSheetVisible = MutableStateFlow(false)
    val isAddSheetVisible: StateFlow<Boolean> = _isAddSheetVisible.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentTimeSeconds = MutableStateFlow(System.currentTimeMillis() / 1000L)
    val currentTimeSeconds: StateFlow<Long> = _currentTimeSeconds.asStateFlow()

    // Settings - Android Biometric (persisted)
    private val _biometricEnabled = MutableStateFlow(securityPrefs.biometricEnabled)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()


    private val _autoLockOption = MutableStateFlow(securityPrefs.autoLockOption)
    val autoLockOption: StateFlow<String> = _autoLockOption.asStateFlow()

    private val _hideCodesWhenLocked = MutableStateFlow(securityPrefs.hideCodesWhenLocked)
    val hideCodesWhenLocked: StateFlow<Boolean> = _hideCodesWhenLocked.asStateFlow()

    private val _copiedCodeId = MutableStateFlow<String?>(null)
    val copiedCodeId: StateFlow<String?> = _copiedCodeId.asStateFlow()

    init {
        _accounts.value = accountsStore.load()
        viewModelScope.launch {
            while (isActive) {
                _currentTimeSeconds.value = System.currentTimeMillis() / 1000L
                delay(500)
            }
        }
    }

    fun navigateTo(screen: OnyxScreen) {
        _currentScreen.value = screen
    }

    fun showAddSheet() { _isAddSheetVisible.value = true }
    fun hideAddSheet() { _isAddSheetVisible.value = false }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun clearSearchQuery() { _searchQuery.value = "" }

    /** Call ONLY after the system BiometricPrompt succeeded; persists the new state. */
    fun toggleBiometric(enabled: Boolean) {
        _biometricEnabled.value = enabled
        securityPrefs.biometricEnabled = enabled
    }

    fun setAutoLockOption(option: String) {
        _autoLockOption.value = option
        securityPrefs.autoLockOption = option
    }

    fun toggleHideCodes(hide: Boolean) {
        _hideCodesWhenLocked.value = hide
        securityPrefs.hideCodesWhenLocked = hide
    }

    /** Called when the onboarding flow ends (finished or skipped). */
    fun completeOnboarding(next: OnyxScreen = OnyxScreen.Main) {
        securityPrefs.isFirstLaunch = false
        _currentScreen.value = if (next == OnyxScreen.Main && _biometricEnabled.value) OnyxScreen.Lock else next
    }

    /** One-shot user-facing error (e.g. invalid QR); UI shows it and calls [clearUserMessage]. */
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()
    fun showUserMessage(message: String) { _userMessage.value = message }
    fun clearUserMessage() { _userMessage.value = null }

    /** Codes are masked on the main list when the setting is on; a tap reveals one card. */
    private val _revealedCodeId = MutableStateFlow<String?>(null)
    val revealedCodeId: StateFlow<String?> = _revealedCodeId.asStateFlow()
    fun revealCode(accountId: String) {
        _revealedCodeId.value = accountId
        viewModelScope.launch {
            delay(10_000)
            if (_revealedCodeId.value == accountId) _revealedCodeId.value = null
        }
    }

    // ---- Lock lifecycle -------------------------------------------------------------------

    private var backgroundedAt: Long = -1L

    fun unlock() {
        backgroundedAt = -1L
        _currentScreen.value = OnyxScreen.Main
    }

    fun lock() {
        if (_biometricEnabled.value) _currentScreen.value = OnyxScreen.Lock
    }

    /**
     * Set right before we hand control to a system picker (folder / file chooser) launched by us.
     * That round-trip stops/starts the Activity exactly like leaving the app, which would otherwise
     * trigger auto-lock, dispose the screen that launched the picker and drop its result.
     */
    private var systemPickerInFlight = false
    fun onSystemPickerLaunched() { systemPickerInFlight = true }

    /** Activity.onStop: remember when we left. */
    fun onAppBackgrounded() {
        if (systemPickerInFlight) return
        if (_biometricEnabled.value) backgroundedAt = SystemClock.elapsedRealtime()
    }

    /** Activity.onStart: re-lock if the configured grace period has elapsed. */
    fun onAppForegrounded() {
        if (systemPickerInFlight) { systemPickerInFlight = false; return }
        if (!_biometricEnabled.value || backgroundedAt < 0L) return
        val elapsed = SystemClock.elapsedRealtime() - backgroundedAt
        backgroundedAt = -1L
        if (elapsed >= SecurityPreferences.autoLockMillis(_autoLockOption.value)) {
            _currentScreen.value = OnyxScreen.Lock
        }
    }

    private var clipboardClearJob: Job? = null

    fun copyCode(context: Context, account: OnyxAccount) {
        val code = TotpGenerator.generateTotpOrNull(
            secretBase32 = account.secret,
            timeSeconds = _currentTimeSeconds.value,
            period = account.period,
            digits = account.digits,
            algorithm = account.algorithm
        )
        if (code == null) {
            showUserMessage("لا يمكن نسخ هذا الحساب: المفتاح غير صالح")
            return
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("TOTP Code", code).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                description.extras = PersistableBundle().apply { putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true) }
            }
        }
        clipboard.setPrimaryClip(clip)

        _copiedCodeId.value = account.id
        viewModelScope.launch {
            delay(1600)
            if (_copiedCodeId.value == account.id) _copiedCodeId.value = null
        }

        // Security: wipe the clipboard 30 s after copying, only if it still holds our code.
        clipboardClearJob?.cancel()
        clipboardClearJob = viewModelScope.launch {
            delay(CLIPBOARD_TTL_MS)
            val current = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (current == code) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) clipboard.clearPrimaryClip()
                else clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }
    }

    /**
     * Validates and adds an account. Returns null on success or an Arabic error message.
     * Nothing is auto-filled: blank issuer/account or an invalid Base32 secret is rejected.
     */
    fun addAccount(
        issuer: String,
        accountName: String,
        secret: String,
        algorithm: String = "SHA-1",
        digits: Int = 6,
        period: Int = 30
    ): String? {
        validateAccountInput(issuer, accountName, secret, algorithm, digits, period)?.let { return it }
        val newAccount = OnyxAccount(
            id = "acc_${System.currentTimeMillis()}",
            issuer = issuer.trim(),
            accountName = accountName.trim(),
            secret = secret.trim().uppercase().replace(" ", ""),
            algorithm = algorithm,
            digits = digits,
            period = period,
            addedDate = todayLabel()
        )
        _accounts.value = listOf(newAccount) + _accounts.value
        accountsStore.save(_accounts.value)
        _currentScreen.value = OnyxScreen.Main
        return null
    }

    /** Updates issuer/account name (and optionally the TOTP parameters) of an existing account. */
    fun updateAccount(
        accountId: String,
        issuer: String,
        accountName: String,
        secret: String,
        algorithm: String,
        digits: Int,
        period: Int
    ): String? {
        validateAccountInput(issuer, accountName, secret, algorithm, digits, period)?.let { return it }
        val existing = _accounts.value.firstOrNull { it.id == accountId } ?: return "الحساب غير موجود"
        val updated = existing.copy(
            issuer = issuer.trim(),
            accountName = accountName.trim(),
            secret = secret.trim().uppercase().replace(" ", ""),
            algorithm = algorithm,
            digits = digits,
            period = period
        )
        _accounts.value = _accounts.value.map { if (it.id == accountId) updated else it }
        accountsStore.save(_accounts.value)
        _currentScreen.value = OnyxScreen.CodeDetail(accountId)
        return null
    }

    private fun validateAccountInput(
        issuer: String, accountName: String, secret: String, algorithm: String, digits: Int, period: Int
    ): String? = when {
        issuer.isBlank() -> "اسم الخدمة مطلوب"
        accountName.isBlank() -> "اسم الحساب مطلوب"
        secret.isBlank() -> "المفتاح السري مطلوب"
        !TotpGenerator.isValidSecret(secret) -> "المفتاح السري ليس Base32 صالحاً (A–Z و 2–7 فقط)"
        TotpGenerator.normalizeAlgorithm(algorithm) == null -> "خوارزمية غير مدعومة"
        digits !in TotpGenerator.SUPPORTED_DIGITS -> "عدد الأرقام يجب أن يكون 6 أو 8"
        period <= 0 || period > 3600 -> "الفترة يجب أن تكون بين 1 و 3600 ثانية"
        else -> null
    }

    private fun todayLabel(): String =
        java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("ar")).format(java.util.Date())

    fun deleteAccount(accountId: String) {
        _accounts.value = _accounts.value.filterNot { it.id == accountId }
        accountsStore.save(_accounts.value)
        if (_accounts.value.isEmpty()) {
            _currentScreen.value = OnyxScreen.EmptyState
        } else {
            _currentScreen.value = OnyxScreen.Main
        }
    }

    fun clearAllAccounts() {
        _accounts.value = emptyList()
        accountsStore.save(_accounts.value)
        _currentScreen.value = OnyxScreen.EmptyState
    }

    // ---- Import from other apps (Google Authenticator migration QR) -------------------------

    /**
     * Decodes one migration QR (a self-contained batch) off the main thread, merges the accounts
     * (deduplicated by secret, then id), persists, and returns to Main.
     * Returns null on success or an error message for the scanner banner.
     */
    fun importMigrationUri(raw: String): String? {
        val result = try {
            MigrationPayloadDecoder.decode(raw)
        } catch (e: MigrationPayloadDecoder.DecodeException) {
            return e.message
        } catch (e: Exception) {
            return "تعذّر فك رمز النقل"
        }
        if (result.accounts.isEmpty()) return "الرمز لا يحتوي على حسابات TOTP مدعومة"

        viewModelScope.launch {
            val merged = withContext(Dispatchers.Default) {
                val existingSecrets = _accounts.value.map { it.secret }.toHashSet()
                val fresh = result.accounts.filter { it.secret !in existingSecrets }.distinctBy { it.secret }
                Triple(fresh, (fresh + _accounts.value).distinctBy { it.id }, result.accounts.size - fresh.size)
            }
            val (fresh, all, duplicates) = merged
            _accounts.value = all
            withContext(Dispatchers.IO) { accountsStore.save(all) }
            val msg = buildString {
                append("تم استيراد ${fresh.size} حساب بنجاح")
                if (duplicates > 0) append(" · تم تخطي $duplicates مكرر")
                if (result.skipped > 0) append(" · ${result.skipped} غير مدعوم (HOTP)")
                if (result.batchSize > 1) append(" · رمز ${result.batchIndex + 1} من ${result.batchSize}")
            }
            showUserMessage(msg)
            _currentScreen.value = OnyxScreen.Main
        }
        return null
    }

    // ---- Vault export / import (.ox) --------------------------------------------------------

    sealed class VaultResult {
        data class Success(val message: String) : VaultResult()
        data class Error(val message: String) : VaultResult()
    }

    private val _vaultResult = MutableStateFlow<VaultResult?>(null)
    val vaultResult: StateFlow<VaultResult?> = _vaultResult.asStateFlow()
    fun clearVaultResult() { _vaultResult.value = null }

    /** Encrypts all accounts and writes them into the granted "Onyx" folder. */
    fun exportVault(password: CharArray) {
        viewModelScope.launch {
            _vaultResult.value = try {
                val path = withContext(Dispatchers.Default) {
                    val bytes = OnyxVault.export(_accounts.value, password)
                    withContext(Dispatchers.IO) { vaultFolders.writeVault(bytes) }
                }
                if (path != null) VaultResult.Success("تم حفظ الخزنة في $path")
                else VaultResult.Error("تعذّر الكتابة في مجلد Onyx")
            } catch (e: Exception) {
                VaultResult.Error(e.message ?: "فشل التصدير")
            } finally {
                password.fill('\u0000')
            }
        }
    }

    /** Decrypts a picked .ox file and merges its accounts (existing ids are replaced). */
    fun importVault(uri: Uri, password: CharArray) {
        viewModelScope.launch {
            _vaultResult.value = try {
                val imported = withContext(Dispatchers.IO) {
                    OnyxVault.import(vaultFolders.readBytes(uri), password)
                }
                val merged = (imported + _accounts.value).distinctBy { it.id }
                _accounts.value = merged
                accountsStore.save(merged)
                VaultResult.Success("تم استيراد ${imported.size} حساب بنجاح")
            } catch (e: Exception) {
                VaultResult.Error(e.message ?: "فشل الاستيراد")
            } finally {
                password.fill('\u0000')
            }
        }
    }

    private companion object { const val CLIPBOARD_TTL_MS = 30_000L }
}
