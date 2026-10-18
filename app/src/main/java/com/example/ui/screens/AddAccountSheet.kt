package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.Separator
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.TertiaryLabel
import com.example.ui.theme.TertiarySystemBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
    onDismiss: () -> Unit,
    onScanQr: () -> Unit,
    onManualEntry: () -> Unit,
    onImportOther: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Sheet Header
            Text(
                text = "إضافة حساب",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                color = Label
            )
            Text(
                text = "اختر طريقة إضافة الرمز",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = SecondaryLabel,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Grouped Options List Container (Elevated 2: TertiarySystemBackground)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TertiarySystemBackground, RoundedCornerShape(14.dp))
            ) {
                SheetOptionRow(
                    icon = Icons.Outlined.QrCodeScanner,
                    title = "مسح رمز QR",
                    testTag = "option_scan_qr",
                    onClick = {
                        onDismiss()
                        onScanQr()
                    }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Separator,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                SheetOptionRow(
                    icon = Icons.Outlined.Keyboard,
                    title = "إدخال يدوي",
                    testTag = "option_manual_entry",
                    onClick = {
                        onDismiss()
                        onManualEntry()
                    }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Separator,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                SheetOptionRow(
                    icon = Icons.Outlined.CloudDownload,
                    title = "استيراد من تطبيق آخر",
                    testTag = "option_import_other",
                    onClick = {
                        onDismiss()
                        onImportOther()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .testTag("add_sheet_cancel_button")
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TertiarySystemBackground,
                    contentColor = SystemBlue
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "إلغاء",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = SystemBlue
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetOptionRow(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(SecondarySystemBackground, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SystemBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Label
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = null,
            tint = TertiaryLabel,
            modifier = Modifier.size(20.dp)
        )
    }
}
