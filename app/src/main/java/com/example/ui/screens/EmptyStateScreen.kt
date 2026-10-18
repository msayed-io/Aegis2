package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AegisEmblem
import com.example.ui.components.FloatingBottomNavCapsule
import com.example.ui.components.FloatingCapsuleHeader
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue

@Composable
fun EmptyStateScreen(
    onAddFirst: () -> Unit,
    onImport: () -> Unit,
    onSettingsClick: () -> Unit,
    onScanQrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 96.dp, bottom = 128.dp)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .background(SecondarySystemBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AegisEmblem(
                        size = 54.dp,
                        tint = SystemBlue,
                        strokeWidth = 3.6f,
                        showOuterRing = true
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "ابدأ برمز واحد",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Label,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "أضف حسابك الأول للبدء.",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    color = SecondaryLabel
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onAddFirst,
                    modifier = Modifier
                        .testTag("empty_add_first_button")
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SystemBlue,
                        contentColor = Label
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Label
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "أضف حسابك الأول",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Label
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onImport,
                    modifier = Modifier
                        .testTag("empty_import_button")
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondarySystemBackground,
                        contentColor = SystemBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "استيراد من تطبيق آخر",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = SystemBlue
                    )
                }
            }

            TopVignetteOverlay()

            // One brand capsule; settings and scanning are each available only in the nav capsule.
            FloatingCapsuleHeader(
                startContent = {
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AegisEmblem(size = 20.dp, tint = SystemBlue, strokeWidth = 3f)
                        Text(
                            text = "Onyx",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Label
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )

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
