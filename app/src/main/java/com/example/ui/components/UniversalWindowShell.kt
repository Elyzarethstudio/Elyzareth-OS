package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.AppId
import com.example.model.VerificationState
import com.example.model.WindowData
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun UniversalWindowShell(
    window: WindowData,
    isActive: Boolean,
    onFocus: () -> Unit,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    onDragPosition: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (window.isClosed || window.isMinimized) return

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp - 64.dp // subtract taskbar height

    // Offset and size animations
    val targetX = if (window.isMaximized) 0f else window.offsetX.coerceIn(0f, (configuration.screenWidthDp - 80f).coerceAtLeast(0f))
    val targetY = if (window.isMaximized) 0f else window.offsetY.coerceIn(0f, (configuration.screenHeightDp - 120f).coerceAtLeast(0f))
    val targetWidth = if (window.isMaximized) screenWidthDp else minOf(window.width.dp, screenWidthDp - 8.dp)
    val targetHeight = if (window.isMaximized) screenHeightDp else minOf(window.height.dp, screenHeightDp - 8.dp)

    val animatedWidth by animateDpAsState(targetValue = targetWidth, animationSpec = tween(220), label = "w")
    val animatedHeight by animateDpAsState(targetValue = targetHeight, animationSpec = tween(220), label = "h")
    val cornerRadius by animateDpAsState(targetValue = if (window.isMaximized) 0.dp else 12.dp, label = "corner")

    var dragOffsetX by remember(window.offsetX) { mutableFloatStateOf(window.offsetX) }
    var dragOffsetY by remember(window.offsetY) { mutableFloatStateOf(window.offsetY) }

    val borderColor = if (isActive) ElyCyan.copy(alpha = 0.65f) else ElyWindowBorderInactive
    val shadowElevation = if (isActive) 16.dp else 6.dp

    Box(
        modifier = modifier
            .offset {
                if (window.isMaximized) {
                    IntOffset(0, 0)
                } else {
                    IntOffset(
                        (dragOffsetX * density.density).roundToInt(),
                        (dragOffsetY * density.density).roundToInt()
                    )
                }
            }
            .size(width = animatedWidth, height = animatedHeight)
            .zIndex(window.zIndex)
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(ElySurfaceDark.copy(alpha = 0.95f))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFocus() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Windows 11 Style Titlebar / Landlord Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isActive) listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF1E293B)
                            ) else listOf(
                                Color(0xFF0F172A),
                                Color(0xFF090D16)
                            )
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                    )
                    .pointerInput(window.isMaximized) {
                        if (!window.isMaximized) {
                            detectDragGestures(
                                onDragStart = { onFocus() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetX = (dragOffsetX + dragAmount.x / density.density).coerceIn(0f, configuration.screenWidthDp - 120f)
                                    dragOffsetY = (dragOffsetY + dragAmount.y / density.density).coerceIn(0f, configuration.screenHeightDp - 160f)
                                    onDragPosition(dragOffsetX, dragOffsetY)
                                }
                            )
                        }
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: App Icon + Title + Tenant Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            when (window.appId) {
                                                AppId.LYRIC_GENERATOR -> ElyCyan
                                                AppId.CORPUS_CURATOR -> ElyPurple
                                                AppId.INTEGRATOR -> ElyCyanBright
                                                AppId.ENGINE_TERMINAL -> ElyG3Axiom
                                                AppId.SPACE_ARCHIVE -> ElyIndigo
                                            },
                                            Color(0xFF0F172A)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = window.appId.defaultIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = window.appId.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isActive) ElyTextPrimary else ElyTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Tenant Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElyCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = window.appId.tenantNumber,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = ElyCyan
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                // Memory allocation pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${"%.1f".format(window.allocatedMemoryMb)} MB",
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ElyTextTertiary
                                    )
                                }
                            }
                        }
                    }

                    // Right: Governance Status Lights + Windows 11 Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // G1/G2/G3 Governance Pills
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            GovernanceMiniDot("G1", window.g1Status, ElyG1Lexical)
                            GovernanceMiniDot("G2", window.g2Status, ElyG2Harmony)
                            GovernanceMiniDot("G3", window.g3Status, ElyG3Axiom)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Minimize Button (_)
                        WindowControlButton(
                            onClick = onMinimize,
                            hoverColor = Color.White.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Minimize,
                                contentDescription = "Minimize",
                                tint = ElyWindowMinimize,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Maximize / Restore Button (□ / ❐)
                        WindowControlButton(
                            onClick = onToggleMaximize,
                            hoverColor = Color.White.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = if (window.isMaximized) Icons.Default.FilterNone else Icons.Default.CropSquare,
                                contentDescription = if (window.isMaximized) "Restore" else "Maximize",
                                tint = ElyWindowMaximize,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Close Button (✕)
                        WindowControlButton(
                            onClick = onClose,
                            hoverColor = ElyWindowClose,
                            isClose = true
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Tenant App Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ElySurfaceDark)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun GovernanceMiniDot(label: String, status: VerificationState, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    when (status) {
                        VerificationState.VERIFIED -> color
                        VerificationState.ACTIVE -> ElyWarning
                        VerificationState.WARNING -> ElyWarning
                        VerificationState.FAILED -> ElyError
                        VerificationState.PENDING -> Color.Gray
                    }
                )
        )
        Text(
            text = label,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun WindowControlButton(
    onClick: () -> Unit,
    hoverColor: Color,
    isClose: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 28.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
