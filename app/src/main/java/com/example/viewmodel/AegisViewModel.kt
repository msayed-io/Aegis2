package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.TotpGenerator
import com.example.model.AegisAccount
import com.example.security.SecurityPreferences
import com.example.data.AccountsStore
import com.example.vault.OnyxVault
import com.example.vault.VaultFolderManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class AegisScreen {
    object Main : AegisScreen()
    object Lock : AegisScreen()
    data class CodeDetail(val accountId: String) : AegisScreen()
    object Search : AegisScreen()
    object EmptyState : AegisScreen()
    object QrScanner : AegisScreen()
    object ManualEntry : AegisScreen()
    object Settings : AegisScreen()
    object BackupSync : AegisScreen()
    object Welcome : AegisScreen()
    object TotpExplanation : AegisScreen()
    object AddFirstCode : AegisScreen()
}

class AegisViewModel(application: Application) : AndroidViewModel(application) {

    private val securityPrefs = SecurityPreferences(application)
    private val accountsStore = AccountsStore(application)
    val vaultFolders = VaultFolderManager(application)

    private val initialAccounts = listOf<AegisAccount>()

    private val _accounts = MutableStateFlow<List<AegisAccount>>(emptyList())
    val accounts: StateFlow<List<AegisAccount>> = _accounts.asStateFlow()

    // Start LOCKED whenever biometric protection is enabled (persisted across launches).
    private val _currentScreen = MutableStateFlow<AegisScreen>(
        if (securityPrefs.biometricEnabled) AegisScreen.Lock else AegisScreen.Main
    )
    val currentScreen: StateFlow<AegisScreen> = _currentScreen.asStateFlow()

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

    private val _cloudSyncEnabled = MutableStateFlow(securityPrefs.cloudSyncEnabled)
    val cloudSyncEnabled: StateFlow<Boolean> = _cloudSyncEnabled.asStateFlow()

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

    fun navigateTo(screen: AegisScreen) {
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

    fun toggleCloudSync(enabled: Boolean) {
        _cloudSyncEnabled.value = enabled
        securityPrefs.cloudSyncEnabled = enabled
    }

    // ---- Lock lifecycle -------------------------------------------------------------------

    private var backgroundedAt: Long = -1L

    fun unlock() {
        backgroundedAt = -1L
        _currentScreen.value = AegisScreen.Main
    }

    fun lock() {
        if (_biometricEnabled.value) _currentScreen.value = AegisScreen.Lock
    }

    /** Activity.onStop: remember when we left. */
    fun onAppBackgrounded() {
        if (_biometricEnabled.value) backgroundedAt = SystemClock.elapsedRealtime()
    }

    /** Activity.onStart: re-lock if the configured grace period has elapsed. */
    fun onAppForegrounded() {
        if (!_biometricEnabled.value || backgroundedAt < 0L) return
        val elapsed = SystemClock.elapsedRealtime() - backgroundedAt
        backgroundedAt = -1L
        if (elapsed >= SecurityPreferences.autoLockMillis(_autoLockOption.value)) {
            _currentScreen.value = AegisScreen.Lock
        }
    }

    fun copyCode(context: Context, account: AegisAccount) {
        val code = TotpGenerator.generateTotp(
            secretBase32 = account.secret,
            timeSeconds = _currentTimeSeconds.value,
            period = account.period,
            digits = account.digits,
            algorithm = account.algorithm
        )
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("TOTP Code", code)
        clipboard.setPrimaryClip(clip)

        _copiedCodeId.value = account.id

        viewModelScope.launch {
            delay(1600)
            if (_copiedCodeId.value == account.id) {
                _copiedCodeId.value = null
            }
        }
    }

    fun addAccount(
        issuer: String,
        accountName: String,
        secret: String,
        algorithm: String = "SHA-1",
        digits: Int = 6,
        period: Int = 30
    ) {
        val newAccount = AegisAccount(
            id = "acc_${System.currentTimeMillis()}",
            issuer = issuer.ifBlank { "خدمة جديدة" },
            accountName = accountName.ifBlank { "user@example.com" },
            secret = secret.ifBlank { "JBSWY3DPEHPK3PXP" },
            algorithm = algorithm,
            digits = digits,
            period = period,
            addedDate = "اليوم"
        )
        _accounts.value = listOf(newAccount) + _accounts.value
        accountsStore.save(_accounts.value)
        _currentScreen.value = AegisScreen.Main
    }

    fun deleteAccount(accountId: String) {
        _accounts.value = _accounts.value.filterNot { it.id == accountId }
        accountsStore.save(_accounts.value)
        if (_accounts.value.isEmpty()) {
            _currentScreen.value = AegisScreen.EmptyState
        } else {
            _currentScreen.value = AegisScreen.Main
        }
    }

    fun resetToDemoAccounts() {
        _accounts.value = initialAccounts
        _currentScreen.value = AegisScreen.Main
    }

    fun clearAllAccounts() {
        _accounts.value = emptyList()
        accountsStore.save(_accounts.value)
        _currentScreen.value = AegisScreen.EmptyState
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
}
