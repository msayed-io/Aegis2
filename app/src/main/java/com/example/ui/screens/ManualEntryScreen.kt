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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.DarkToolbarSurface
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.Separator
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.TertiaryLabel

@Composable
fun ManualEntryScreen(
    onCancel: () -> Unit,
    onSave: (issuer: String, account: String, secret: String, algorithm: String, digits: Int, period: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var issuer by remember { mutableStateOf("Google") }
    var accountName by remember { mutableStateOf("user@example.com") }
    var secretKey by remember { mutableStateOf("JBSWY3DPEHPK3PXP") }
    var isSecretVisible by remember { mutableStateOf(false) }

    var algorithm by remember { mutableStateOf("SHA-1") }
    var digits by remember { mutableIntStateOf(6) }
    var period by remember { mutableIntStateOf(30) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Solid dark navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(DarkToolbarSurface)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Right (RTL start): Cancel
                Row(
                    modifier = Modifier
                        .testTag("manual_entry_cancel_button")
                        .clickable { onCancel() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = SystemBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "إلغاء",
                        fontFamily = InterFontFamily,
                        fontSize = 17.sp,
                        color = SystemBlue
                    )
                }

                // Title
                Text(
                    text = "حساب جديد",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Label
                )

                // Left (RTL end): Add
                Text(
                    text = "إضافة",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = SystemBlue,
                    modifier = Modifier
                        .testTag("manual_entry_top_save_button")
                        .clickable {
                            onSave(issuer, accountName, secretKey, algorithm, digits, period)
                        }
                        .padding(4.dp)
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = Separator)

            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
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
                        .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
                ) {
                    // Issuer Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
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
                            .height(48.dp)
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
                                    .weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    // Secret Key Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
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
                                    .weight(1f)
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
                        .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
                ) {
                    // Type Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
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
                            .height(48.dp)
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
                            .height(48.dp)
                            .clickable { digits = if (digits == 6) 8 else 6 }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الأرقام", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        Text(text = "$digits", fontFamily = JetBrainsMonoFontFamily, fontSize = 15.sp, color = SystemBlue)
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    // Period Row (30s)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الفترة", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        Text(text = "$period ثانية", fontFamily = InterFontFamily, fontSize = 15.sp, color = SecondaryLabel)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "يتم تشفير وتخزين المفتاح محلياً على هذا الجهاز باستخدام تقنية الحماية الآمنة.",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TertiaryLabel,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom CTA Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Button(
                    onClick = {
                        onSave(issuer, accountName, secretKey, algorithm, digits, period)
                    },
                    modifier = Modifier
                        .testTag("manual_entry_bottom_add_button")
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SystemBlue,
                        contentColor = Label
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "إضافة الحساب",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Label
                    )
                }
            }
        }
    }
}
