package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.TotpGenerator
import com.example.model.AegisAccount
import com.example.ui.components.CountdownRing
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    accounts: List<AegisAccount>,
    searchQuery: String,
    currentTimeSeconds: Long,
    copiedCodeId: String?,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onAccountClick: (AegisAccount) -> Unit,
    onCopyCode: (AegisAccount) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = if (searchQuery.isBlank()) {
        accounts
    } else {
        accounts.filter {
            it.issuer.contains(searchQuery, ignoreCase = true) ||
                    it.accountName.contains(searchQuery, ignoreCase = true)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 76.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                // Search Bar Box
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(SecondarySystemBackground, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = SecondaryLabel,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onQueryChange,
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = InterFontFamily,
                                        fontSize = 16.sp,
                                        color = Label
                                    ),
                                    cursorBrush = SolidColor(SystemBlue),
                                    modifier = Modifier
                                        .testTag("search_input_field")
                                        .weight(1f),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "بحث في الحسابات",
                                                fontFamily = InterFontFamily,
                                                fontSize = 16.sp,
                                                color = TertiaryLabel
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Outlined.Cancel,
                                    contentDescription = "مسح البحث",
                                    tint = SecondaryLabel,
                                    modifier = Modifier
                                        .testTag("clear_search_button")
                                        .size(18.dp)
                                        .clickable { onClearQuery() }
                                )
                            }
                        }
                    }
                }

                // Results count header
                item {
                    Text(
                        text = "النتائج (${filtered.size})",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = SecondaryLabel,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Results List
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
                    ) {
                        if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "لا توجد حسابات مسجلة" else "لا توجد نتائج مطابقة لـ \"$searchQuery\"",
                                    fontFamily = InterFontFamily,
                                    fontSize = 14.sp,
                                    color = SecondaryLabel
                                )
                            }
                        } else {
                            filtered.forEachIndexed { index, account ->
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

                                SearchResultItem(
                                    account = account,
                                    formattedCode = formattedCode,
                                    remainingSeconds = remainingSeconds,
                                    isCopied = isCopied,
                                    onClick = { onAccountClick(account) },
                                    onCopy = { onCopyCode(account) }
                                )

                                if (index < filtered.size - 1) {
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

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "يتم توليد رموز التحقق محلياً على هذا الجهاز دون اتصال بالإنترنت.",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = TertiaryLabel,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Top Vignette Overlay
            TopVignetteOverlay()

            // Floating Top Header Capsule
            FloatingHeaderCapsule(
                title = "بحث",
                trailingIcon = {
                    Text(
                        text = "إلغاء",
                        fontFamily = InterFontFamily,
                        fontSize = 16.sp,
                        color = SystemBlue,
                        modifier = Modifier
                            .testTag("search_cancel_button")
                            .clickable { onCancel() }
                    )
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    account: AegisAccount,
    formattedCode: String,
    remainingSeconds: Int,
    isCopied: Boolean,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
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
                    .size(36.dp)
                    .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.badgeLetter,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
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
                        fontSize = 13.sp,
                        color = SecondaryLabel
                    )
                }
            }
        }

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
                    fontSize = 18.sp,
                    color = codeColor
                )
            }

            CountdownRing(
                remainingSeconds = remainingSeconds,
                period = account.period,
                size = 32.dp,
                strokeWidth = 2.4.dp
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onCopy() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = if (isCopied) SystemGreen else SecondaryLabel,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
