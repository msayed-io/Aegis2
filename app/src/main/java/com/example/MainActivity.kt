package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.screens.AddAccountSheet
import com.example.ui.screens.AddFirstCodeScreen
import com.example.ui.screens.BackupSyncScreen
import com.example.ui.screens.CodeDetailScreen
import com.example.ui.screens.EmptyStateScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.ManualEntryScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TotpExplanationScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AegisScreen
import com.example.viewmodel.AegisViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide status bar completely - immersive professional experience
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            val viewModel: AegisViewModel = viewModel()
            MyApplicationTheme {
                AegisAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AegisAppContent(viewModel: AegisViewModel) {
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
    val cloudSyncEnabled by viewModel.cloudSyncEnabled.collectAsState()

    var showScreenPickerSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = currentScreen !is AegisScreen.Main) {
        when (currentScreen) {
            is AegisScreen.CodeDetail,
            is AegisScreen.Search,
            is AegisScreen.QrScanner,
            is AegisScreen.ManualEntry,
            is AegisScreen.Settings,
            is AegisScreen.Welcome,
            is AegisScreen.TotpExplanation,
            is AegisScreen.AddFirstCode,
            is AegisScreen.EmptyState -> {
                viewModel.navigateTo(AegisScreen.Main)
            }
            is AegisScreen.BackupSync -> {
                viewModel.navigateTo(AegisScreen.Settings)
            }
            is AegisScreen.Lock -> {
                viewModel.navigateTo(AegisScreen.Main)
            }
            else -> {}
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                is AegisScreen.Lock -> {
                    LockScreen(
                        onUnlock = { viewModel.navigateTo(AegisScreen.Main) }
                    )
                }

                is AegisScreen.Main -> {
                    MainScreen(
                        accounts = accounts,
                        currentTimeSeconds = currentTimeSeconds,
                        copiedCodeId = copiedCodeId,
                        onAccountClick = { account ->
                            viewModel.navigateTo(AegisScreen.CodeDetail(account.id))
                        },
                        onCopyCode = { account ->
                            viewModel.copyCode(context, account)
                        },
                        onSearchClick = { viewModel.navigateTo(AegisScreen.Search) },
                        onSettingsClick = { viewModel.navigateTo(AegisScreen.Settings) },
                        onScanQrClick = { viewModel.navigateTo(AegisScreen.QrScanner) },
                        onAddClick = { viewModel.showAddSheet() }
                    )
                }

                is AegisScreen.CodeDetail -> {
                    val account = accounts.find { it.id == screen.accountId } ?: accounts.firstOrNull()
                    if (account != null) {
                        CodeDetailScreen(
                            account = account,
                            currentTimeSeconds = currentTimeSeconds,
                            isCopied = copiedCodeId == account.id,
                            onBack = { viewModel.navigateTo(AegisScreen.Main) },
                            onCopy = { viewModel.copyCode(context, account) },
                            onEdit = { viewModel.navigateTo(AegisScreen.ManualEntry) }
                        )
                    } else {
                        viewModel.navigateTo(AegisScreen.Main)
                    }
                }

                is AegisScreen.Search -> {
                    SearchScreen(
                        accounts = accounts,
                        searchQuery = searchQuery,
                        currentTimeSeconds = currentTimeSeconds,
                        copiedCodeId = copiedCodeId,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onClearQuery = { viewModel.clearSearchQuery() },
                        onAccountClick = { account ->
                            viewModel.navigateTo(AegisScreen.CodeDetail(account.id))
                        },
                        onCopyCode = { account ->
                            viewModel.copyCode(context, account)
                        },
                        onCancel = {
                            viewModel.clearSearchQuery()
                            viewModel.navigateTo(AegisScreen.Main)
                        }
                    )
                }

                is AegisScreen.EmptyState -> {
                    EmptyStateScreen(
                        onAddFirst = { viewModel.showAddSheet() },
                        onImport = { viewModel.resetToDemoAccounts() },
                        onSettingsClick = { viewModel.navigateTo(AegisScreen.Settings) },
                        onScanQrClick = { viewModel.navigateTo(AegisScreen.QrScanner) }
                    )
                }

                is AegisScreen.QrScanner -> {
                    QrScannerScreen(
                        onClose = { viewModel.navigateTo(AegisScreen.Main) },
                        onManualEntry = { viewModel.navigateTo(AegisScreen.ManualEntry) },
                        onScannedResult = { issuer, account, secret ->
                            viewModel.addAccount(issuer, account, secret)
                        }
                    )
                }

                is AegisScreen.ManualEntry -> {
                    ManualEntryScreen(
                        onCancel = { viewModel.navigateTo(AegisScreen.Main) },
                        onSave = { issuer, account, secret, alg, dig, per ->
                            viewModel.addAccount(issuer, account, secret, alg, dig, per)
                        }
                    )
                }

                is AegisScreen.Settings -> {
                    SettingsScreen(
                        biometricEnabled = biometricEnabled,
                        autoLockOption = autoLockOption,
                        hideCodesWhenLocked = hideCodesWhenLocked,
                        cloudSyncEnabled = cloudSyncEnabled,
                        onToggleBiometric = { viewModel.toggleBiometric(it) },
                        onAutoLockChange = { viewModel.setAutoLockOption(it) },
                        onToggleHideCodes = { viewModel.toggleHideCodes(it) },
                        onToggleCloudSync = { viewModel.toggleCloudSync(it) },
                        onNavigateToBackupSync = { viewModel.navigateTo(AegisScreen.BackupSync) },
                        onBack = { viewModel.navigateTo(AegisScreen.Main) },
                        onOpenWelcome = { viewModel.navigateTo(AegisScreen.Welcome) },
                        onOpenTotpMath = { viewModel.navigateTo(AegisScreen.TotpExplanation) }
                    )
                }

                is AegisScreen.BackupSync -> {
                    BackupSyncScreen(
                        cloudSyncEnabled = cloudSyncEnabled,
                        accountsCount = accounts.size,
                        onToggleCloudSync = { viewModel.toggleCloudSync(it) },
                        onBack = { viewModel.navigateTo(AegisScreen.Settings) }
                    )
                }

                is AegisScreen.Welcome -> {
                    WelcomeScreen(
                        onContinue = { viewModel.navigateTo(AegisScreen.TotpExplanation) },
                        onSkip = { viewModel.navigateTo(AegisScreen.Main) }
                    )
                }

                is AegisScreen.TotpExplanation -> {
                    TotpExplanationScreen(
                        currentTimeSeconds = currentTimeSeconds,
                        onContinue = { viewModel.navigateTo(AegisScreen.AddFirstCode) },
                        onSkip = { viewModel.navigateTo(AegisScreen.Main) },
                        onBack = { viewModel.navigateTo(AegisScreen.Welcome) }
                    )
                }

                is AegisScreen.AddFirstCode -> {
                    AddFirstCodeScreen(
                        currentTimeSeconds = currentTimeSeconds,
                        onAddFirst = { viewModel.showAddSheet() },
                        onDoLater = { viewModel.navigateTo(AegisScreen.Main) },
                        onSettingsClick = { viewModel.navigateTo(AegisScreen.Settings) }
                    )
                }
            }

            if (isAddSheetVisible) {
                AddAccountSheet(
                    onDismiss = { viewModel.hideAddSheet() },
                    onScanQr = { viewModel.navigateTo(AegisScreen.QrScanner) },
                    onManualEntry = { viewModel.navigateTo(AegisScreen.ManualEntry) },
                    onImportOther = {
                        viewModel.resetToDemoAccounts()
                    }
                )
            }
        }
    }
}
