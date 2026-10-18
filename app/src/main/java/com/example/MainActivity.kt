package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.security.BiometricAuthenticator
import com.example.security.findFragmentActivity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.screens.AddAccountSheet
import com.example.ui.screens.AddFirstCodeScreen
import com.example.ui.screens.BackupSyncScreen
import com.example.ui.screens.CodeDetailScreen
import com.example.ui.screens.EmptyStateScreen
import com.example.ui.screens.LicensesScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.ManualEntryScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TotpExplanationScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.OnyxScreen
import com.example.viewmodel.OnyxViewModel

// FragmentActivity is REQUIRED by androidx.biometric.BiometricPrompt.
class MainActivity : FragmentActivity() {
    private val viewModel: OnyxViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }

    /**
     * Status bar policy. The system re-shows bars after the IME, dialogs (BiometricPrompt)
     * or focus changes on many OEM skins, so this is re-applied on every focus gain / resume,
     * not only once in onCreate.
     */
    private fun applyImmersiveStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveStatusBar()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyImmersiveStatusBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Security: block screenshots / screen recording / recent-apps thumbnails of OTP codes.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        applyImmersiveStatusBar()
        setContent {
            MyApplicationTheme {
                OnyxAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun OnyxAppContent(viewModel: OnyxViewModel) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAddSheetVisible by viewModel.isAddSheetVisible.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentTimeSeconds by viewModel.currentTimeSeconds.collectAsState()
    val copiedCodeId by viewModel.copiedCodeId.collectAsState()

    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val autoLockOption by viewModel.autoLockOption.collectAsState()
    val hideCodesWhenLocked by viewModel.hideCodesWhenLocked.collectAsState()
    val revealedCodeId by viewModel.revealedCodeId.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    // One-shot messages (e.g. invalid QR / invalid secret) via a plain system toast.
    LaunchedEffect(userMessage) {
        userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearUserMessage()
        }
    }

    var showScreenPickerSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = currentScreen !is OnyxScreen.Main) {
        when (currentScreen) {
            is OnyxScreen.CodeDetail,
            is OnyxScreen.Search,
            is OnyxScreen.QrScanner,
            is OnyxScreen.ManualEntry,
            is OnyxScreen.EmptyState -> {
                viewModel.navigateTo(OnyxScreen.Main)
            }
            is OnyxScreen.Welcome,
            is OnyxScreen.TotpExplanation,
            is OnyxScreen.AddFirstCode -> {
                viewModel.completeOnboarding()
            }
            is OnyxScreen.EditAccount -> {
                viewModel.navigateTo(OnyxScreen.CodeDetail((currentScreen as OnyxScreen.EditAccount).accountId))
            }
            is OnyxScreen.Settings -> {
                viewModel.navigateTo(OnyxScreen.Main)
            }
            is OnyxScreen.BackupSync,
            is OnyxScreen.Licenses -> {
                viewModel.navigateTo(OnyxScreen.Settings)
            }
            is OnyxScreen.Lock -> {
                context.findFragmentActivity()?.moveTaskToBack(true)
            }
            else -> {}
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                is OnyxScreen.Lock -> {
                    LockScreen(
                        onUnlocked = { viewModel.unlock() }
                    )
                }

                is OnyxScreen.Main -> {
                    MainScreen(
                        accounts = accounts,
                        currentTimeSeconds = currentTimeSeconds,
                        copiedCodeId = copiedCodeId,
                        hideCodes = hideCodesWhenLocked,
                        revealedCodeId = revealedCodeId,
                        onRevealCode = { viewModel.revealCode(it.id) },
                        onAccountClick = { account ->
                            viewModel.navigateTo(OnyxScreen.CodeDetail(account.id))
                        },
                        onCopyCode = { account ->
                            viewModel.copyCode(context, account)
                        },
                        onSearchClick = { viewModel.navigateTo(OnyxScreen.Search) },
                        onSettingsClick = { viewModel.navigateTo(OnyxScreen.Settings) },
                        onScanQrClick = { viewModel.navigateTo(OnyxScreen.QrScanner) },
                        onAddClick = { viewModel.showAddSheet() }
                    )
                }

                is OnyxScreen.CodeDetail -> {
                    val account = accounts.find { it.id == screen.accountId } ?: accounts.firstOrNull()
                    if (account != null) {
                        CodeDetailScreen(
                            account = account,
                            currentTimeSeconds = currentTimeSeconds,
                            isCopied = copiedCodeId == account.id,
                            onBack = { viewModel.navigateTo(OnyxScreen.Main) },
                            onCopy = { viewModel.copyCode(context, account) },
                            onEdit = { viewModel.navigateTo(OnyxScreen.EditAccount(account.id)) },
                            onDelete = { viewModel.deleteAccount(account.id) }
                        )
                    } else {
                        viewModel.navigateTo(OnyxScreen.Main)
                    }
                }

                is OnyxScreen.Search -> {
                    SearchScreen(
                        accounts = accounts,
                        searchQuery = searchQuery,
                        currentTimeSeconds = currentTimeSeconds,
                        copiedCodeId = copiedCodeId,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onClearQuery = { viewModel.clearSearchQuery() },
                        onAccountClick = { account ->
                            viewModel.navigateTo(OnyxScreen.CodeDetail(account.id))
                        },
                        onCopyCode = { account ->
                            viewModel.copyCode(context, account)
                        },
                        onCancel = {
                            viewModel.clearSearchQuery()
                            viewModel.navigateTo(OnyxScreen.Main)
                        }
                    )
                }

                is OnyxScreen.EmptyState -> {
                    EmptyStateScreen(
                        onAddFirst = { viewModel.showAddSheet() },
                        onImport = { viewModel.navigateTo(OnyxScreen.BackupSync) },
                        onSettingsClick = { viewModel.navigateTo(OnyxScreen.Settings) },
                        onScanQrClick = { viewModel.navigateTo(OnyxScreen.QrScanner) }
                    )
                }

                is OnyxScreen.QrScanner -> {
                    QrScannerScreen(
                        onClose = { viewModel.navigateTo(OnyxScreen.Main) },
                        onManualEntry = { viewModel.navigateTo(OnyxScreen.ManualEntry) },
                        onScannedResult = { parsed ->
                            viewModel.addAccount(
                                issuer = parsed.issuer,
                                accountName = parsed.accountName,
                                secret = parsed.secret,
                                algorithm = parsed.algorithm,
                                digits = parsed.digits,
                                period = parsed.period
                            )
                        }
                    )
                }

                is OnyxScreen.ManualEntry -> {
                    ManualEntryScreen(
                        onCancel = { viewModel.navigateTo(OnyxScreen.Main) },
                        onSave = { issuer, account, secret, alg, dig, per ->
                            viewModel.addAccount(issuer, account, secret, alg, dig, per)
                        }
                    )
                }

                is OnyxScreen.EditAccount -> {
                    val account = accounts.find { it.id == screen.accountId }
                    if (account != null) {
                        ManualEntryScreen(
                            existing = account,
                            onCancel = { viewModel.navigateTo(OnyxScreen.CodeDetail(account.id)) },
                            onSave = { issuer, acc, secret, alg, dig, per ->
                                viewModel.updateAccount(account.id, issuer, acc, secret, alg, dig, per)
                            }
                        )
                    } else {
                        viewModel.navigateTo(OnyxScreen.Main)
                    }
                }

                is OnyxScreen.Licenses -> {
                    LicensesScreen(onBack = { viewModel.navigateTo(OnyxScreen.Settings) })
                }

                is OnyxScreen.Settings -> {
                    SettingsScreen(
                        biometricEnabled = biometricEnabled,
                        autoLockOption = autoLockOption,
                        hideCodesWhenLocked = hideCodesWhenLocked,
                        onToggleBiometric = { wanted ->
                            val activity = context.findFragmentActivity()
                            when {
                                activity == null -> Unit
                                !BiometricAuthenticator.isAvailable(activity) -> Toast.makeText(
                                    context,
                                    "لا توجد بصمة أو قفل شاشة مسجّل على هذا الجهاز. فعّلهما من إعدادات أندرويد أولاً.",
                                    Toast.LENGTH_LONG
                                ).show()
                                else -> BiometricAuthenticator.authenticate(
                                    activity = activity,
                                    title = if (wanted) "تفعيل القفل البيومتري" else "إيقاف القفل البيومتري",
                                    subtitle = "تأكيد هويتك لحفظ هذا الإعداد",
                                    onSuccess = { viewModel.toggleBiometric(wanted) }
                                )
                            }
                        },
                        onAutoLockChange = { viewModel.setAutoLockOption(it) },
                        onToggleHideCodes = { viewModel.toggleHideCodes(it) },
                        onNavigateToBackupSync = { viewModel.navigateTo(OnyxScreen.BackupSync) },
                        onBack = { viewModel.navigateTo(OnyxScreen.Main) },
                        onOpenWelcome = { viewModel.navigateTo(OnyxScreen.Welcome) },
                        onOpenTotpMath = { viewModel.navigateTo(OnyxScreen.TotpExplanation) },
                        onOpenLicenses = { viewModel.navigateTo(OnyxScreen.Licenses) }
                    )
                }

                is OnyxScreen.BackupSync -> {
                    val vaultResult by viewModel.vaultResult.collectAsState()
                    BackupSyncScreen(
                        accountsCount = accounts.size,
                        folderManager = viewModel.vaultFolders,
                        vaultResult = vaultResult,
                        onExport = { viewModel.exportVault(it) },
                        onImport = { uri, pwd -> viewModel.importVault(uri, pwd) },
                        onClearResult = { viewModel.clearVaultResult() },
                        onSystemPickerLaunched = { viewModel.onSystemPickerLaunched() },
                        onBack = { viewModel.navigateTo(OnyxScreen.Settings) }
                    )
                }

                is OnyxScreen.Welcome -> {
                    WelcomeScreen(
                        onContinue = { viewModel.navigateTo(OnyxScreen.TotpExplanation) },
                        onSkip = { viewModel.completeOnboarding() }
                    )
                }

                is OnyxScreen.TotpExplanation -> {
                    TotpExplanationScreen(
                        currentTimeSeconds = currentTimeSeconds,
                        onContinue = { viewModel.navigateTo(OnyxScreen.AddFirstCode) },
                        onSkip = { viewModel.completeOnboarding() },
                        onBack = { viewModel.navigateTo(OnyxScreen.Welcome) }
                    )
                }

                is OnyxScreen.AddFirstCode -> {
                    AddFirstCodeScreen(
                        currentTimeSeconds = currentTimeSeconds,
                        onAddFirst = { viewModel.completeOnboarding(); viewModel.showAddSheet() },
                        onDoLater = { viewModel.completeOnboarding() },
                        onSettingsClick = { viewModel.completeOnboarding(OnyxScreen.Settings) }
                    )
                }
            }

            if (isAddSheetVisible) {
                AddAccountSheet(
                    onDismiss = { viewModel.hideAddSheet() },
                    onScanQr = { viewModel.navigateTo(OnyxScreen.QrScanner) },
                    onManualEntry = { viewModel.navigateTo(OnyxScreen.ManualEntry) },
                    onImportOther = { viewModel.navigateTo(OnyxScreen.BackupSync) }
                )
            }
        }
    }
}
