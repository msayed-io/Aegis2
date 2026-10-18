package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.SystemRed
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.TotpGenerator
import com.example.model.OnyxAccount
import com.example.ui.components.CountdownRing
import com.example.ui.components.CapsuleCloseButton
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.components.CardShape
import com.example.ui.theme.*

@Composable
fun CodeDetailScreen(
    account: OnyxAccount,
    currentTimeSeconds: Long,
    isCopied: Boolean,
    onBack: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val code = TotpGenerator.generateTotpOrNull(
        secretBase32 = account.secret,
        timeSeconds = currentTimeSeconds,
        period = account.period,
        digits = account.digits,
        algorithm = account.algorithm
    )
    val formattedCode = code?.let { TotpGenerator.formatCodeWithDot(it) } ?: TotpGenerator.INVALID_CODE_LABEL
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val remainingSeconds = TotpGenerator.getRemainingSeconds(account.period, currentTimeSeconds)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 96.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Big 220dp Circular Countdown Ring
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CountdownRing(
                        remainingSeconds = remainingSeconds,
                        period = account.period,
                        size = 220.dp,
                        strokeWidth = 3.5.dp,
                        showNumberInside = false
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = formattedCode,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 28.sp,
                                letterSpacing = 1.sp,
                                color = Label
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "$remainingSeconds ثانية",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = SecondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Account Details Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account.badgeLetter,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Label
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = account.issuer,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                color = Label
                            )
                            Text(
                                text = account.accountName,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = SecondaryLabel
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons
                Button(
                    onClick = onCopy,
                    modifier = Modifier
                        .testTag("detail_copy_button")
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SystemBlue,
                        contentColor = Label
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp),
                            tint = if (isCopied) SystemGreen else Label
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCopied) "تم النسخ" else "نسخ الرمز",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Label
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .testTag("detail_secondary_edit_button")
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondarySystemBackground,
                        contentColor = Label
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "تعديل",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Label
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Grouped iOS Details Card
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تفاصيل",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = SecondaryLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SecondarySystemBackground, CardShape)
                    ) {
                        DetailRow(label = "النوع", value = "TOTP · ${account.algorithm}", isCode = true)
                        HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))
                        DetailRow(label = "الأرقام", value = "${account.digits}", isCode = true)
                        HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))
                        DetailRow(label = "الفترة", value = "${account.period} ثانية", isCode = false)
                        HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))
                        DetailRow(label = "أُضيف في", value = account.addedDate.ifBlank { "—" }, isCode = false)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Destructive action — real deletion with confirmation.
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .testTag("detail_delete_button")
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SystemRed.copy(alpha = 0.14f),
                        contentColor = SystemRed
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "حذف الحساب",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = SystemRed
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    containerColor = SecondarySystemBackground,
                    shape = CardShape,
                    title = {
                        Text(text = "حذف ${account.issuer}؟", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, color = Label)
                    },
                    text = {
                        Text(
                            text = "سيُحذف هذا الحساب نهائياً من Onyx. تأكد أن لديك طريقة أخرى لتسجيل الدخول أو نسخة احتياطية قبل المتابعة.",
                            fontFamily = InterFontFamily,
                            color = SecondaryLabel
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showDeleteConfirm = false; onDelete() }, modifier = Modifier.testTag("detail_delete_confirm")) {
                            Text(text = "حذف", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, color = SystemRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(text = "إلغاء", fontFamily = InterFontFamily, color = SystemBlue)
                        }
                    }
                )
            }

            // Top Vignette Overlay
            TopVignetteOverlay()

            // Floating Top Header Capsule
            FloatingHeaderCapsule(
                title = "الرمز",
                trailingIcon = {
                    CapsuleCloseButton(onClick = onBack, testTag = "detail_back_button")
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isCode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Label
        )

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = value,
                fontFamily = if (isCode) JetBrainsMonoFontFamily else InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = SecondaryLabel
            )
        }
    }
}
