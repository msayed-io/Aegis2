package com.example.ui.screens

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.QrScannerEngine
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.TertiarySystemBackground
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onClose: () -> Unit,
    onManualEntry: () -> Unit,
    onScannedResult: (issuer: String, account: String, secret: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val lifecycleOwner = LocalLifecycleOwner.current

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffsetRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (cameraPermissionState.status.isGranted) {
            val context = LocalContext.current
            val engine = remember {
                QrScannerEngine(lifecycleOwner, PreviewView(context)) { rawBarcode ->
                    // Parse otpauth:// if present or pass raw
                    var issuer = "Aegis"
                    var account = "User"
                    var secret = rawBarcode

                    if (rawBarcode.startsWith("otpauth://", ignoreCase = true)) {
                        try {
                            val uri = android.net.Uri.parse(rawBarcode)
                            secret = uri.getQueryParameter("secret") ?: rawBarcode
                            issuer = uri.getQueryParameter("issuer") ?: "Aegis"
                            val path = uri.path?.removePrefix("/") ?: ""
                            account = if (path.contains(":")) path.substringAfter(":") else path
                        } catch (_: Exception) {}
                    }
                    onScannedResult(issuer, account, secret)
                }
            }

            DisposableEffect(Unit) {
                onDispose { engine.shutdown() }
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(SystemBackground)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Camera Preview Pipeline
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = {
                        engine.startCamera()
                    }
                )

                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("scanner_close_button")
                            .size(36.dp)
                            .background(SecondarySystemBackground.copy(alpha = 0.85f), CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "إغلاق",
                            tint = Label,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "مسح رمز QR",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Label
                    )

                    Box(modifier = Modifier.size(36.dp))
                }

                // Viewfinder Reticle Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Razor-Sharp Corner Brackets (#0A84FF)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 3.5.dp.toPx()
                            val cornerLen = 28.dp.toPx()
                            val w = size.width
                            val h = size.height

                            // Top Right
                            drawLine(SystemBlue, Offset(w - cornerLen, 0f), Offset(w, 0f), stroke)
                            drawLine(SystemBlue, Offset(w, 0f), Offset(w, cornerLen), stroke)

                            // Top Left
                            drawLine(SystemBlue, Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
                            drawLine(SystemBlue, Offset(0f, 0f), Offset(0f, cornerLen), stroke)

                            // Bottom Right
                            drawLine(SystemBlue, Offset(w - cornerLen, h), Offset(w, h), stroke)
                            drawLine(SystemBlue, Offset(w, h - cornerLen), Offset(w, h), stroke)

                            // Bottom Left
                            drawLine(SystemBlue, Offset(0f, h), Offset(cornerLen, h), stroke)
                            drawLine(SystemBlue, Offset(0f, h - cornerLen), Offset(0f, h), stroke)
                        }

                        // Laser Scanning Line (#0A84FF)
                        val laserY = 16.dp + (218.dp * laserOffsetRatio)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .offset(y = laserY - 125.dp)
                                .height(2.5.dp)
                                .shadow(6.dp, CircleShape, spotColor = SystemBlue)
                                .background(SystemBlue, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "وجّه الكاميرا إلى الرمز",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Label
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "يقرأ Aegis رموز ",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = SecondaryLabel
                        )
                        Text(
                            text = "otpauth://",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = Label,
                            modifier = Modifier
                                .background(TertiarySystemBackground, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = " فورًا",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = SecondaryLabel
                        )
                    }
                }

                // Bottom Action Area
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .testTag("scanner_manual_entry_link")
                            .clickable { onManualEntry() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Keyboard,
                            contentDescription = null,
                            tint = SystemBlue,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الإدخال اليدوي بدلًا من ذلك",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = SystemBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .width(134.dp)
                            .height(5.dp)
                            .background(Color.White.copy(alpha = 0.35f), CircleShape)
                    )
                }
            }
        } else {
            // Camera Permission Request View
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(SystemBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "يرجى السماح بالوصول للكاميرا لمسح الرموز",
                        fontFamily = InterFontFamily,
                        fontSize = 16.sp,
                        color = Label
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = SystemBlue)
                    ) {
                        Text(text = "منح الإذن", fontFamily = InterFontFamily, color = Label)
                    }
                }
            }
        }
    }
}
