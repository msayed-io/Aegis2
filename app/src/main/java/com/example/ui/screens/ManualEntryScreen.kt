package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CapsuleCloseButton
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.components.CardShape
import com.example.model.OnyxAccount
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.Separator
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.SystemRed
import com.example.ui.theme.TertiaryLabel

@Composable
fun ManualEntryScreen(
    onCancel: () -> Unit,
    /** Returns null on success or an error message to show. */
    onSave: (issuer: String, account: String, secret: String, algorithm: String, digits: Int, period: Int) -> String?,
    existing: OnyxAccount? = null,
    modifier: Modifier = Modifier
) {
    // Nothing is pre-filled for a new account. In edit mode the real account values are loaded.
    var issuer by remember { mutableStateOf(existing?.issuer ?: "") }
    var accountName by remember { mutableStateOf(existing?.accountName ?: "") }
    var secretKey by remember { mutableStateOf(existing?.secret ?: "") }
    var isSecretVisible by remember { mutableStateOf(false) }

    var algorithm by remember { mutableStateOf(existing?.algorithm ?: "SHA-1") }
    var digits by remember { mutableIntStateOf(existing?.digits ?: 6) }
    var period by remember { mutableIntStateOf(existing?.period ?: 30) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isEdit = existing != null
    val canSave = issuer.isNotBlank() && accountName.isNotBlank() && secretKey.isNotBlank()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            // Scrollable Content - padded to avoid overlapping floating capsules
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 96.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = if (isEdit) "عدّل بيانات الحساب ثم احفظ" else "أدخل بيانات حسابك يدوياً",
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    color = SecondaryLabel,
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )

                // Section 1: بيانات الحساب
                Text(
                    text = "بيانات الحساب",
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
                    // Issuer Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الجهة",
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = Label,
                            modifier = Modifier.width(76.dp)
                        )
                        BasicTextField(
                            value = issuer,
                            onValueChange = { issuer = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = InterFontFamily,
                                fontSize = 16.sp,
                                color = Label
                            ),
                            cursorBrush = SolidColor(SystemBlue),
                            modifier = Modifier
                                .testTag("input_issuer")
                                .weight(1f)
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    // Account Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الحساب",
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = Label,
                            modifier = Modifier.width(76.dp)
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            BasicTextField(
                                value = accountName,
                                onValueChange = { accountName = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 15.sp,
                                    color = Label
                                ),
                                cursorBrush = SolidColor(SystemBlue),
                                modifier = Modifier
                                    .testTag("input_account_name")
                                    .weight(1f),
                                decorationBox = { inner ->
                                    if (accountName.isEmpty()) Text(text = "name@email.com", fontFamily = JetBrainsMonoFontFamily, fontSize = 14.sp, color = TertiaryLabel)
                                    inner()
                                }
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    // Secret Key Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المفتاح",
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = Label,
                            modifier = Modifier.width(76.dp)
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            BasicTextField(
                                value = secretKey,
                                onValueChange = { secretKey = it },
                                singleLine = true,
                                visualTransformation = if (isSecretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp,
                                    color = Label
                                ),
                                cursorBrush = SolidColor(SystemBlue),
                                modifier = Modifier
                                    .testTag("input_secret_key")
                                    .weight(1f),
                                decorationBox = { inner ->
                                    if (secretKey.isEmpty()) Text(text = "Base32 key", fontFamily = JetBrainsMonoFontFamily, fontSize = 14.sp, color = TertiaryLabel)
                                    inner()
                                }
                            )
                        }
                        IconButton(
                            onClick = { isSecretVisible = !isSecretVisible },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSecretVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "تبديل الرؤية",
                                tint = SecondaryLabel,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: الإعدادات المتقدمة
                Text(
                    text = "الإعدادات المتقدمة",
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
                    // Type Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "النوع", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        Text(text = "TOTP", fontFamily = JetBrainsMonoFontFamily, fontSize = 15.sp, color = SecondaryLabel)
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    // Algorithm Row (Toggleable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable {
                                algorithm = when (algorithm) {
                                    "SHA-1" -> "SHA-256"
                                    "SHA-256" -> "SHA-512"
                                    else -> "SHA-1"
                                }
                            }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الخوارزمية", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = algorithm, fontFamily = JetBrainsMonoFontFamily, fontSize = 15.sp, color = SystemBlue)
                            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null, tint = SecondaryLabel, modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    // Digits Row (Toggleable 6 / 8)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { digits = if (digits == 6) 8 else 6 }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الأرقام", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        Text(text = "$digits", fontFamily = JetBrainsMonoFontFamily, fontSize = 15.sp, color = SystemBlue)
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    // Period Row (cycles 30 → 60 → 90 → 15 → 30)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable {
                                period = when (period) {
                                    30 -> 60
                                    60 -> 90
                                    90 -> 15
                                    else -> 30
                                }
                            }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الفترة", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        Text(text = "$period ثانية", fontFamily = JetBrainsMonoFontFamily, fontSize = 15.sp, color = SystemBlue)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = SystemRed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "يُخزَّن المفتاح مشفّراً على هذا الجهاز عبر Android Keystore ولا يغادره.",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TertiaryLabel,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom CTA Button - inside scroll for consistency with other capsule screens
                Button(
                    onClick = {
                        errorMessage = onSave(issuer, accountName, secretKey, algorithm, digits, period)
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .testTag("manual_entry_bottom_add_button")
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SystemBlue,
                        contentColor = Label,
                        disabledContainerColor = SystemBlue.copy(alpha = 0.35f),
                        disabledContentColor = Label.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = if (isEdit) "حفظ التعديلات" else "إضافة الحساب",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Label
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Top Vignette Overlay for smooth scrolling behind floating header
            TopVignetteOverlay()

            // Floating Top Header Capsule - NEW CAPSULE SYSTEM
            FloatingHeaderCapsule(
                title = if (isEdit) "تعديل الحساب" else "حساب جديد",
                trailingIcon = {
                    CapsuleCloseButton(onClick = onCancel, testTag = "manual_entry_cancel_button", contentDescription = "إلغاء")
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
