package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.model.AegisAccount
import com.example.ui.components.AegisEmblem
import com.example.ui.components.CountdownRing
import com.example.ui.components.FloatingBottomNavCapsule
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.theme.*

@Composable
fun MainScreen(
    accounts: List<AegisAccount>,
    currentTimeSeconds: Long,
    copiedCodeId: String?,
    onAccountClick: (AegisAccount) -> Unit,
    onCopyCode: (AegisAccount) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            // Scrollable Content scrolling behind floating header and bottom nav
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 76.dp, bottom = 96.dp, start = 16.dp, end = 16.dp)
            ) {
                // Brand Headline & Search Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AegisEmblem(
                                size = 22.dp,
                                tint = SystemBlue,
                                strokeWidth = 3f
                            )
                            Text(
                                text = "Aegis",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                color = Label,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .testTag("search_icon_button")
                                .size(38.dp)
                                .background(SecondarySystemBackground, CircleShape)
                                .clickable { onSearchClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "بحث في الرموز",
                                tint = SecondaryLabel,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                // Subtitle Count & Sync Status
                item {
                    val countLabel = when (accounts.size) {
                        0 -> "لا توجد حسابات"
                        1 -> "حساب واحد"
                        2 -> "حسابان"
                        in 3..10 -> "${accounts.size} حسابات"
                        else -> "${accounts.size} حساب"
                    }
                    Text(
                        text = "$countLabel · تمت المزامنة منذ دقيقتين",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = SecondaryLabel,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Accounts List Container
                if (accounts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد حسابات مسجلة. انقر على + لإضافة حسابك الأول.",
                                fontFamily = InterFontFamily,
                                fontSize = 15.sp,
                                color = SecondaryLabel
                            )
                        }
                    }
                } else {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SecondarySystemBackground, RoundedCornerShape(16.dp))
                        ) {
                            accounts.forEachIndexed { index, account ->
                                val code = TotpGenerator.generateTotp(
                                    secretBase32 = account.secret,
                                    timeSeconds = currentTimeSeconds,
                                    period = account.period,
                                    digits = account.digits,
                                    algorithm = account.algorithm
                                )
                                val formattedCode = TotpGenerator.formatCodeWithDot(code)
                                val remainingSeconds = TotpGenerator.getRemainingSeconds(account.period, currentTimeSeconds)
                                val isCopied = copiedCodeId == account.id

                                AccountListItem(
                                    account = account,
                                    formattedCode = formattedCode,
                                    remainingSeconds = remainingSeconds,
                                    isCopied = isCopied,
                                    onClick = { onAccountClick(account) },
                                    onCopy = { onCopyCode(account) }
                                )

                                if (index < accounts.size - 1) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = Separator,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top Vignette Overlay for smooth scrolling behind floating header
            TopVignetteOverlay()

            // Floating Top Header Capsule (zIndex = 50)
            FloatingHeaderCapsule(
                title = "رموز التحقق",
                leadingIcon = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("top_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "الإعدادات",
                            tint = SecondaryLabel,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier.testTag("top_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "إضافة حساب",
                            tint = SystemBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Floating Action Button (FAB) at Bottom Right
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = SystemBlue,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .testTag("fab_add_account")
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 80.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "إضافة حساب جديد",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }

            // Floating Bottom Navigation Capsule (zIndex = 40)
            FloatingBottomNavCapsule(
                currentTab = 0,
                onAuthClick = {},
                onScanClick = onScanQrClick,
                onSettingsClick = onSettingsClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun AccountListItem(
    account: AegisAccount,
    formattedCode: String,
    remainingSeconds: Int,
    isCopied: Boolean,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .testTag("account_item_${account.id}")
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TertiarySystemBackground, RoundedCornerShape(10.dp)),
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
                        fontSize = 16.sp,
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

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val codeColor = when {
                remainingSeconds <= 1 -> SystemRed
                remainingSeconds <= 5 -> SystemOrange
                else -> Label
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = formattedCode,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp,
                    color = codeColor
                )
            }

            CountdownRing(
                remainingSeconds = remainingSeconds,
                period = account.period,
                size = 32.dp,
                strokeWidth = 2.6.dp
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clickable { onCopy() },
                contentAlignment = Alignment.Center
            ) {
                if (isCopied) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "تم النسخ",
                        tint = SystemGreen,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "نسخ الرمز",
                        tint = SecondaryLabel,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
