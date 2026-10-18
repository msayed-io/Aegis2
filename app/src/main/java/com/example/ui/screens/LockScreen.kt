package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AegisEmblem
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val buttonScale = remember { Animatable(1f) }
    val emblemScale = remember { Animatable(1f) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            // Status bar simulated time
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "9:41",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Label
                )
            }

            // Center Emblem & Lock Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(emblemScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    AegisEmblem(
                        size = 80.dp,
                        tint = SystemBlue,
                        strokeWidth = 3.6f,
                        showOuterRing = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Onyx",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Label,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "التطبيق مقفل",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    color = SecondaryLabel
                )
            }

            // Bottom Actions Container
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Action: Face ID Unlock Button (Pill 50dp height, #0A84FF)
                Button(
                    onClick = {
                        scope.launch {
                            buttonScale.animateTo(0.96f, tween(100))
                            buttonScale.animateTo(1f, tween(150))
                            emblemScale.animateTo(1.12f, tween(200))
                            emblemScale.animateTo(1f, tween(150))
                            delay(100)
                            onUnlock()
                        }
                    },
                    modifier = Modifier
                        .testTag("face_id_unlock_button")
                        .fillMaxWidth()
                        .height(50.dp)
                        .scale(buttonScale.value),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SystemBlue,
                        contentColor = Label
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Face,
                            contentDescription = "Face ID",
                            modifier = Modifier.size(22.dp),
                            tint = Label
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فتح باستخدام Face ID",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Label
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Text Link: Use Passcode
                Text(
                    text = "استخدام رمز الدخول",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = SystemBlue,
                    modifier = Modifier
                        .testTag("passcode_unlock_button")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onUnlock()
                        }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // iOS Native Home Indicator
                Box(
                    modifier = Modifier
                        .width(138.dp)
                        .height(5.dp)
                        .background(Color.White.copy(alpha = 0.35f), CircleShape)
                )
            }
        }
    }
}
