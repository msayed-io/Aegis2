package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.components.CapsuleCloseButton
import com.example.ui.components.CardShape
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.theme.*
import com.example.viewmodel.AegisViewModel
import com.example.vault.VaultFolderManager

/**
 * Backup screen — everything here is REAL:
 *  • folder: the user grants a location once; an official "Onyx" folder is created inside it.
 *  • export: AES-256-GCM encrypted ".ox" vault written into that folder.
 *  • import: system file picker -> password -> decrypt -> merge.
 */
@Composable
fun BackupSyncScreen(
    accountsCount: Int,
    folderManager: VaultFolderManager,
    vaultResult: AegisViewModel.VaultResult?,
    onExport: (password: CharArray) -> Unit,
    onImport: (uri: Uri, password: CharArray) -> Unit,
    onClearResult: () -> Unit,
    onSystemPickerLaunched: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var folderReady by remember { mutableStateOf(folderManager.grantedTreeUri != null) }
    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var pickedImportUri by remember { mutableStateOf<Uri?>(null) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            folderManager.persistTree(uri)
            folderReady = true
            if (pendingAction == PendingAction.EXPORT) showExportSheet = true
        }
        pendingAction = null
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pickedImportUri = uri
    }

    fun startExport() {
        if (folderManager.grantedTreeUri == null) {
            pendingAction = PendingAction.EXPORT
            onSystemPickerLaunched()
            folderPicker.launch(null)
        } else {
            showExportSheet = true
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 96.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "رموزك محفوظة على هذا الجهاز · $accountsCount حساب",
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    color = SecondaryLabel,
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )

                // ---- Folder ----
                SectionHeader("مجلد النسخ")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                ) {
                    ActionRow(
                        icon = Icons.Outlined.Folder,
                        title = if (folderReady) "مجلد Onyx جاهز" else "اختيار مكان مجلد Onyx",
                        subtitle = if (folderReady) "يُحفظ كل ملف .ox داخل المجلد الرسمي للتطبيق"
                        else "يطلب التطبيق إذناً مرة واحدة ثم ينشئ مجلد Onyx",
                        testTag = "backup_choose_folder",
                        trailing = {
                            if (folderReady) {
                                Icon(Icons.Outlined.Check, null, tint = SystemGreen, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(18.dp))
                            }
                        },
                        onClick = { pendingAction = null; onSystemPickerLaunched(); folderPicker.launch(null) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---- Vault ----
                SectionHeader("الخزنة المشفرة (.ox)")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                ) {
                    ActionRow(
                        icon = Icons.Outlined.Lock,
                        title = "تصدير خزنة",
                        subtitle = "تشفير AES-256 بكلمة مرور تختارها",
                        testTag = "backup_export_vault_btn",
                        onClick = { startExport() }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))
                    ActionRow(
                        icon = Icons.Outlined.UploadFile,
                        title = "استيراد خزنة",
                        subtitle = "اختر ملف .ox وأدخل كلمة المرور",
                        testTag = "backup_import_vault_btn",
                        onClick = { pickedImportUri = null; showImportSheet = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.Lock, null, tint = SystemBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "المفتاح يُشتق من كلمة مرورك (PBKDF2 · 200 ألف تكرار) ولا يُحفظ في أي مكان. بدونها لا يمكن لأحد — ولا Onyx — فتح الملف.",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = SecondaryLabel,
                        lineHeight = 19.sp
                    )
                }
            }

            TopVignetteOverlay()

            FloatingHeaderCapsule(
                title = "النسخ الاحتياطي",
                trailingIcon = { CapsuleCloseButton(onClick = onBack, testTag = "backup_back_button") },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // ---- Sheets ----
            if (showExportSheet) {
                PasswordSheet(
                    title = "تصدير الخزنة",
                    subtitle = "اختر كلمة مرور قوية لتشفير $accountsCount حساب",
                    confirmLabel = "تصدير",
                    requireConfirm = true,
                    onDismiss = { showExportSheet = false },
                    onConfirm = { pwd -> showExportSheet = false; onExport(pwd) }
                )
            }

            if (showImportSheet) {
                ImportSheet(
                    pickedUri = pickedImportUri,
                    onPickFile = { onSystemPickerLaunched(); filePicker.launch(arrayOf("*/*")) },
                    onDismiss = { showImportSheet = false; pickedImportUri = null },
                    onConfirm = { pwd ->
                        val uri = pickedImportUri
                        showImportSheet = false
                        pickedImportUri = null
                        if (uri != null) onImport(uri, pwd)
                    }
                )
            }

            vaultResult?.let { result ->
                ResultSheet(result = result, onDismiss = onClearResult)
            }
        }
    }
}

private enum class PendingAction { EXPORT }

// ------------------------------------------------------------------------------------------------
// Rows
// ------------------------------------------------------------------------------------------------

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(18.dp))
    }
) {
    Row(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(TertiarySystemBackground, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = Label, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Label)
                Text(text = subtitle, fontFamily = InterFontFamily, fontSize = 13.sp, color = SecondaryLabel)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        trailing()
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = InterFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = SecondaryLabel,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

// ------------------------------------------------------------------------------------------------
// Sheets (same visual language as AddAccountSheet)
// ------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnyxSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        containerColor = SecondarySystemBackground,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .background(SecondaryLabel, CircleShape)
            )
        }
    ) {
        HideStatusBarInThisDialog()
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                content = content
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetTitle(title: String, subtitle: String) {
    Text(text = title, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 21.sp, color = Label)
    Text(
        text = subtitle,
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        color = SecondaryLabel,
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String
) {
    var visible by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(TertiarySystemBackground, RoundedCornerShape(percent = 50))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 16.sp, color = Label),
                cursorBrush = SolidColor(SystemBlue),
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .weight(1f)
                    .testTag(testTag),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(placeholder, fontFamily = InterFontFamily, fontSize = 15.sp, color = TertiaryLabel)
                    }
                    inner()
                }
            )
        }
        Icon(
            imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = null,
            tint = SecondaryLabel,
            modifier = Modifier
                .size(20.dp)
                .clickable { visible = !visible }
        )
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean, testTag: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = SystemBlue,
            contentColor = Color.White,
            disabledContainerColor = SystemBlue.copy(alpha = 0.35f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text = label, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
    }
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(containerColor = TertiarySystemBackground, contentColor = SystemBlue),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text = label, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = SystemBlue)
    }
}

@Composable
private fun PasswordSheet(
    title: String,
    subtitle: String,
    confirmLabel: String,
    requireConfirm: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (CharArray) -> Unit
) {
    var pwd by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val valid = pwd.length >= 6 && (!requireConfirm || confirm == pwd)

    OnyxSheet(onDismiss = onDismiss) {
        SheetTitle(title, subtitle)
        PasswordField(pwd, { pwd = it }, "كلمة المرور (6 أحرف على الأقل)", "vault_password")
        if (requireConfirm) {
            Spacer(modifier = Modifier.height(10.dp))
            PasswordField(confirm, { confirm = it }, "تأكيد كلمة المرور", "vault_password_confirm")
            if (confirm.isNotEmpty() && confirm != pwd) {
                Text(
                    text = "كلمتا المرور غير متطابقتين",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = SystemRed,
                    modifier = Modifier.padding(start = 12.dp, top = 6.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        PrimaryButton(confirmLabel, valid, "vault_confirm_button") { onConfirm(pwd.toCharArray()) }
        Spacer(modifier = Modifier.height(10.dp))
        SecondaryButton("إلغاء", onDismiss)
    }
}

@Composable
private fun ImportSheet(
    pickedUri: Uri?,
    onPickFile: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (CharArray) -> Unit
) {
    var pwd by remember { mutableStateOf("") }
    val fileName = pickedUri?.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')

    OnyxSheet(onDismiss = onDismiss) {
        SheetTitle("استيراد خزنة", "اختر ملف .ox من هاتفك ثم أدخل كلمة المرور")

        // Upload box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .background(TertiarySystemBackground, CardShape)
                .border(1.dp, if (pickedUri != null) SystemBlue else Separator, CardShape)
                .clickable { onPickFile() }
                .testTag("import_pick_file"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (pickedUri != null) Icons.Outlined.Check else Icons.Outlined.UploadFile,
                contentDescription = null,
                tint = SystemBlue,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = fileName ?: "اضغط لاختيار الملف",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Label,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (pickedUri == null) {
                Text(text = "Onyx Vault · .ox", fontFamily = InterFontFamily, fontSize = 12.sp, color = TertiaryLabel)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        PasswordField(pwd, { pwd = it }, "كلمة مرور الخزنة", "import_password")
        Spacer(modifier = Modifier.height(18.dp))
        PrimaryButton("استيراد", pickedUri != null && pwd.isNotEmpty(), "import_confirm_button") { onConfirm(pwd.toCharArray()) }
        Spacer(modifier = Modifier.height(10.dp))
        SecondaryButton("إلغاء", onDismiss)
    }
}

@Composable
private fun ResultSheet(result: AegisViewModel.VaultResult, onDismiss: () -> Unit) {
    val success = result is AegisViewModel.VaultResult.Success
    val message = when (result) {
        is AegisViewModel.VaultResult.Success -> result.message
        is AegisViewModel.VaultResult.Error -> result.message
    }
    OnyxSheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background((if (success) SystemGreen else SystemRed).copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (success) Icons.Outlined.Check else Icons.Outlined.Close,
                    contentDescription = null,
                    tint = if (success) SystemGreen else SystemRed,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (success) "تم بنجاح" else "لم تكتمل العملية",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Label
            )
            Text(
                text = message,
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                color = SecondaryLabel,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )
            PrimaryButton("حسناً", true, "vault_result_ok", onDismiss)
        }
    }
}

@Composable
private fun HideStatusBarInThisDialog() {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
