package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.components.IosSwitch
import com.example.ui.theme.*

@Composable
fun BackupSyncScreen(
    cloudSyncEnabled: Boolean,
    accountsCount: Int,
    onToggleCloudSync: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                    .padding(top = 96.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                // Large Title Header
                Text(
                    text = "النسخ الاحتياطي والمزامنة",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Label,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "رموزك محفوظة على هذا الجهاز أولًا",
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    color = SecondaryLabel,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Group 1: السحابة المشفرة
                SectionHeader("السحابة المشفرة")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(percent = 50))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Cloud, null, tint = SystemBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "النسخ السحابي",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Label
                                    )
                                    Text(
                                        text = " · مشفر من الطرف إلى الطرف",
                                        fontFamily = InterFontFamily,
                                        fontSize = 13.sp,
                                        color = SecondaryLabel
                                    )
                                }
                                Text(
                                    text = "آخر مزامنة منذ دقيقتين · $accountsCount حسابات",
                                    fontFamily = InterFontFamily,
                                    fontSize = 13.sp,
                                    color = SecondaryLabel,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        IosSwitch(
                            checked = cloudSyncEnabled,
                            onCheckedChange = onToggleCloudSync,
                            testTag = "backup_switch_cloud"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Group 2: يدوي
                SectionHeader("يدوي")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(percent = 50))
                ) {
                    Row(
                        modifier = Modifier
                            .testTag("backup_export_vault_btn")
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "تم تصدير ملف Onyx Vault (.onyx) بنجاح", Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Lock, null, tint = Label, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "تصدير خزنة مشفرة", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Label)
                                Text(text = "حفظ ملف بتنسيق Onyx محمي بكلمة مرور", fontFamily = InterFontFamily, fontSize = 13.sp, color = SecondaryLabel)
                            }
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(18.dp))
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .testTag("backup_import_vault_btn")
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "استيراد الخزنة: جميع الرموز متزامنة", Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Download, null, tint = Label, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "استيراد خزنة", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Label)
                                Text(text = "استعادة الرموز من نسخة احتياطية محلية", fontFamily = InterFontFamily, fontSize = 13.sp, color = SecondaryLabel)
                            }
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Group 3: الاسترداد
                SectionHeader("الاسترداد")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(percent = 50))
                ) {
                    Row(
                        modifier = Modifier
                            .testTag("backup_recovery_codes_btn")
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "تم حفظ رموز الاسترداد بأمان", Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Key, null, tint = Label, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "رموز الاسترداد", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Label)
                                Text(text = "مفتاح الطوارئ لإلغاء القفل واستعادة الوصول", fontFamily = InterFontFamily, fontSize = 13.sp, color = SecondaryLabel)
                            }
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Security Notice Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(percent = 50))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = SystemBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "يتم تشفير جميع النسخ الاحتياطية باستخدام مفتاح Android Keystore مع StrongBox قبل مغادرة الجهاز. لا يمكن لأحد قراءة رموزك حتى Onyx نفسه لا يمكنه قراءتها.",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = SecondaryLabel,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Top Vignette Overlay
            TopVignetteOverlay()

            // Floating Top Header Capsule
            FloatingHeaderCapsule(
                title = "المزامنة والنسخ",
                leadingIcon = {
                    Row(
                        modifier = Modifier
                            .testTag("backup_back_button")
                            .clickable { onBack() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = "رجوع",
                            tint = SystemBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingIcon = {
                    Text(
                        text = "تم",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = SystemBlue,
                        modifier = Modifier.clickable { onBack() }
                    )
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
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
