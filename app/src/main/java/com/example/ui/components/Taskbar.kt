package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.model.EngineTelemetry
import com.example.model.TenantLifecycleState
import com.example.model.TenantResourceMetrics
import com.example.model.WindowData
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * ELYZARETH OS — SYSTEM TASKBAR
 *
 * Shell Rule Refinement:
 * 1. Taskbar is system/navigation, NOT an app catalogue.
 * 2. Permanent items: [Start] [Search] ... [System/Status Tray]
 * 3. Open applications appear dynamically ONLY when running/active.
 * 4. No G1-G5 exposure as general OS decoration.
 */
@Composable
fun Taskbar(
    windows: Map<AppId, WindowData>,
    tenantMetrics: Map<AppId, TenantResourceMetrics>,
    activeAppId: AppId?,
    isStartMenuOpen: Boolean,
    isQuickSettingsOpen: Boolean,
    engineTelemetry: EngineTelemetry,
    onToggleStartMenu: () -> Unit,
    onToggleQuickSettings: () -> Unit,
    onAppIconClick: (AppId) -> Unit,
    onAppTerminate: (AppId) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("MM/dd/yy", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = timeFormatter.format(now)
            currentDate = dateFormatter.format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val totalAllocatedMemory = remember(windows, tenantMetrics) {
        tenantMetrics.values.sumOf { it.allocatedMemoryMb.toDouble() }.toFloat()
    }

    val runningWindows = remember(windows) {
        windows.values.filter { !it.isClosed }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 16.dp)
            .background(ElyTaskbarGlass)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Center Group: [Start] [Search] + [Dynamic Running App Indicators]
        Row(
            modifier = Modifier.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. Start Button (Authoritative Launcher Trigger)
            StartButton(
                isOpen = isStartMenuOpen,
                onClick = onToggleStartMenu
            )

            // 2. Search Button (Quick Retrieval for Apps, Lyrics, Corpus, Spaces)
            SearchButton(
                isOpen = isStartMenuOpen,
                onClick = onToggleStartMenu
            )

            // 3. Dynamic Running Applications (Only open windows appear here)
            if (runningWindows.isNotEmpty()) {
                VerticalDivider(
                    modifier = Modifier
                        .height(22.dp)
                        .padding(horizontal = 2.dp),
                    color = Color.White.copy(alpha = 0.15f)
                )

                runningWindows.forEach { win ->
                    val appId = win.appId
                    val metrics = tenantMetrics[appId]
                    val isFocused = activeAppId == appId && !win.isMinimized

                    RunningTaskbarWindowTab(
                        appId = appId,
                        windowData = win,
                        metrics = metrics,
                        isFocused = isFocused,
                        isMinimized = win.isMinimized,
                        onClick = { onAppIconClick(appId) },
                        onTerminate = { onAppTerminate(appId) }
                    )
                }
            }
        }

        // Right-Side System / Status Area
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Memory Allocation Pill
            if (totalAllocatedMemory > 0f) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onToggleQuickSettings() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RAM ${"%.1f".format(totalAllocatedMemory)}M",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (totalAllocatedMemory > 60f) ElyWarning else ElyCyan
                    )
                }
            }

            // System Readiness State Indicator (Clean, non-decorative)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .clickable { onToggleQuickSettings() }
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ElyG3Axiom.copy(alpha = pulseGlow))
                    )
                    Text(
                        text = "READY",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ElyG3Axiom
                    )
                }
            }

            // System Clock & Date (Triggers Quick Settings Flyout)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isQuickSettingsOpen) ElyTileActive else Color.Transparent)
                    .clickable { onToggleQuickSettings() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentTime.ifEmpty { "12:00" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElyTextPrimary
                    )
                    Text(
                        text = currentDate.ifEmpty { "08/22" },
                        fontSize = 9.sp,
                        color = ElyTextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun StartButton(
    isOpen: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isOpen) ElyCyan.copy(alpha = 0.25f)
                else Color(0xFF1E293B).copy(alpha = 0.7f)
            )
            .border(
                width = 1.dp,
                color = if (isOpen) ElyCyan else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.size(6.dp).background(ElyCyan, RoundedCornerShape(1.5.dp)))
                Box(modifier = Modifier.size(6.dp).background(ElyViolet, RoundedCornerShape(1.5.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.size(6.dp).background(ElyPurple, RoundedCornerShape(1.5.dp)))
                Box(modifier = Modifier.size(6.dp).background(ElyG3Axiom, RoundedCornerShape(1.5.dp)))
            }
        }
    }
}

@Composable
private fun SearchButton(
    isOpen: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isOpen) ElyTileActive else Color.Black.copy(alpha = 0.35f))
            .border(
                width = 0.5.dp,
                color = if (isOpen) ElyCyan else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search OS",
                tint = if (isOpen) ElyCyan else ElyTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Search",
                fontSize = 11.sp,
                color = if (isOpen) ElyTextPrimary else ElyTextTertiary
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun RunningTaskbarWindowTab(
    appId: AppId,
    windowData: WindowData,
    metrics: TenantResourceMetrics?,
    isFocused: Boolean,
    isMinimized: Boolean,
    onClick: () -> Unit,
    onTerminate: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isFocused -> ElyTileActive
        isMinimized -> Color.White.copy(alpha = 0.04f)
        else -> Color.White.copy(alpha = 0.08f)
    }

    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                0.5.dp,
                if (isFocused) ElyCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = appId.defaultIcon,
                    contentDescription = appId.title,
                    tint = when (appId) {
                        AppId.LYRIC_GENERATOR -> ElyCyan
                        AppId.CORPUS_CURATOR -> ElyPurple
                        AppId.INTEGRATOR -> ElyCyanBright
                        AppId.ENGINE_TERMINAL -> ElyG3Axiom
                        AppId.SPACE_ARCHIVE -> ElyIndigo
                    },
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = appId.shortName,
                    fontSize = 10.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                    color = if (isFocused) ElyTextPrimary else if (isMinimized) ElyTextTertiary else ElyTextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Active process pill dot/line underneath running tab
            Box(
                modifier = Modifier
                    .size(width = if (isFocused) 18.dp else 8.dp, height = 2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isFocused) ElyCyanBright
                        else if (isMinimized) ElyWarning.copy(alpha = 0.7f)
                        else ElyTextSecondary.copy(alpha = 0.5f)
                    )
            )
        }

        // Process Context Menu on long-click
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier
                .background(ElySurfaceCard)
                .border(1.dp, ElyTaskbarBorder, RoundedCornerShape(8.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Column {
                        Text(appId.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElyTextPrimary)
                        Text(
                            text = "State: ${metrics?.lifecycleState?.label ?: if (isMinimized) "Minimized" else "Active"} (${"%.1f".format(metrics?.allocatedMemoryMb ?: windowData.allocatedMemoryMb)} MB RAM)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyCyan
                        )
                    }
                },
                onClick = {
                    showContextMenu = false
                    onClick()
                }
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            DropdownMenuItem(
                text = { Text(if (isMinimized) "Restore Window" else "Focus Window", fontSize = 12.sp, color = ElyTextPrimary) },
                leadingIcon = { Icon(Icons.Default.OpenWith, null, modifier = Modifier.size(16.dp), tint = ElyCyan) },
                onClick = {
                    showContextMenu = false
                    onClick()
                }
            )
            DropdownMenuItem(
                text = { Text("Terminate & Free Memory", fontSize = 12.sp, color = ElyError) },
                leadingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = ElyError) },
                onClick = {
                    showContextMenu = false
                    onTerminate()
                }
            )
        }
    }
}
