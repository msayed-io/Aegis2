package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
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
import com.example.model.OnyxAccount
import com.example.ui.components.AccountCodeCard
import com.example.ui.components.OnyxEmblem
import com.example.ui.components.CopiedToast
import com.example.ui.components.CountdownRing
import com.example.ui.components.FloatingBottomNavCapsule
import com.example.ui.components.FloatingCapsuleHeader
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.components.CardShape
import com.example.ui.theme.*

@Composable
fun MainScreen(
    accounts: List<OnyxAccount>,
    currentTimeSeconds: Long,
    copiedCodeId: String?,
    /** When true, codes are hidden behind dots until the card is tapped once. */
    hideCodes: Boolean = false,
    revealedCodeId: String? = null,
    onRevealCode: (OnyxAccount) -> Unit = {},
    onAccountClick: (OnyxAccount) -> Unit,
    onCopyCode: (OnyxAccount) -> Unit,
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
            // The list scrolls beneath the floating header and the bottom controls.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 96.dp,
                    bottom = 208.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                item {
                    Text(
                        text = "رموز التحقق",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Label,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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
                                .background(SecondarySystemBackground, CardShape)
                        ) {
                            accounts.forEachIndexed { index, account ->
                                val code = TotpGenerator.generateTotpOrNull(
                                    secretBase32 = account.secret,
                                    timeSeconds = currentTimeSeconds,
                                    period = account.period,
                                    digits = account.digits,
                                    algorithm = account.algorithm
                                )
                                val formattedCode = code?.let { TotpGenerator.formatCodeWithDot(it) } ?: TotpGenerator.INVALID_CODE_LABEL
                                val remainingSeconds = TotpGenerator.getRemainingSeconds(account.period, currentTimeSeconds)
                                val isCopied = copiedCodeId == account.id
                                val masked = hideCodes && revealedCodeId != account.id && code != null
                                val shownCode = if (masked) {
                                    if (account.digits == 8) "•••• · ••••" else "••• · •••"
                                } else formattedCode

                                AccountCodeCard(
                                    account = account,
                                    formattedCode = shownCode,
                                    remainingSeconds = remainingSeconds,
                                    isCopied = isCopied,
                                    onCopy = { if (masked) onRevealCode(account) else onCopyCode(account) },
                                    onOpenDetails = { onAccountClick(account) }
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

            // Two distinct floating capsules: the brand on the right and search on the left.
            FloatingCapsuleHeader(
                startContent = {
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OnyxEmblem(size = 20.dp, tint = SystemBlue, strokeWidth = 3f)
                        Text(
                            text = "Onyx",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Label
                        )
                    }
                },
                endContent = {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("search_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "بحث في الرموز",
                            tint = SecondaryLabel,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Keep one clear add action, lifted above the navigation capsule.
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = SystemBlue,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .testTag("fab_add_account")
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 100.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "إضافة حساب جديد",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }

            CopiedToast(
                visible = copiedCodeId != null,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

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
