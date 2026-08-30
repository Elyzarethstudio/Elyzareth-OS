package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.model.AppId
import com.example.model.EngineTelemetry
import com.example.model.TenantLifecycleState
import com.example.model.TenantResourceMetrics
import com.example.model.WindowData
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * ELYZARETH OS — SYSTEM TASKBAR (Windows 11 Desktop Style)
 *
 * Sits anchored at the bottom of the UniversalWindowShell / Desktop workspace:
 * 1. Start Button (Windows 11 inspired multi-tile launcher trigger)
 * 2. Search Button (Quick search across OS and apps)
 * 3. Running App Indicators (Displayed dynamically when apps are open)
 * 4. System Tray (Clock, date, WiFi, Volume, Battery, and Quick Settings trigger)
 */
@Composable
fun Taskbar(
    windows: Map<AppId, WindowData> = emptyMap(),
    tenantMetrics: Map<AppId, TenantResourceMetrics> = emptyMap(),
    activeAppId: AppId? = null,
    isStartMenuOpen: Boolean = false,
    isQuickSettingsOpen: Boolean = false,
    engineTelemetry: EngineTelemetry = EngineTelemetry(),
    onToggleStartMenu: () -> Unit = {},
    onToggleQuickSettings: () -> Unit = {},
    onAppIconClick: (AppId) -> Unit = {},
    onAppTerminate: (AppId) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = timeFormatter.format(now)
            currentDate = dateFormatter.format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    val runningWindows = remember(windows) {
        windows.values.filter { !it.isClosed }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("taskbar")
            .shadow(elevation = 16.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE2E8F0).copy(alpha = 0.82f),
                        Color(0xFFCBD5E1).copy(alpha = 0.90f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.80f),
                        Color.White.copy(alpha = 0.30f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Left Group: [Start] [Search] + [Running App Indicators]
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .testTag("taskbar_center_container"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. Start Button (Windows 11 style)
            StartButton(
                isOpen = isStartMenuOpen,
                onClick = onToggleStartMenu
            )

            // 2. Search Button
            SearchButton(
                isOpen = isStartMenuOpen,
                onClick = onToggleStartMenu
            )

            // 3. Running App Indicators (Displayed ONLY when apps are actually open)
            if (runningWindows.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .testTag("active_windows_tray"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    runningWindows.forEach { win ->
                        val appId = win.appId
                        val isFocused = activeAppId == appId && !win.isMinimized

                        RunningAppIconPill(
                            appId = appId,
                            isFocused = isFocused,
                            isMinimized = win.isMinimized,
                            onClick = { onAppIconClick(appId) },
                            onClose = { onAppTerminate(appId) }
                        )
                    }
                }
            }
        }

        // Right Side: Clean Windows 11 System Tray
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .wrapContentWidth()
                .testTag("system_status_tray"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // System Glyphs Group: ^  📶  🔊  🔋
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Quick Settings",
                        onClick = onToggleQuickSettings
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Hidden icons",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(14.dp)
                )
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Network",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(14.dp)
                )
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(14.dp)
                )
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Battery",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(14.dp)
                )
            }

            // System Clock & Date
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isQuickSettingsOpen) Color.White.copy(alpha = 0.45f) else Color.Transparent)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Calendar and Clock",
                        onClick = onToggleQuickSettings
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentTime.ifEmpty { "11:51 AM" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = currentDate.ifEmpty { "08-30-2026" },
                        fontSize = 9.sp,
                        color = Color(0xFF475569)
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
    val animatedBg by animateColorAsState(
        targetValue = if (isOpen) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.25f),
        label = "start_bg"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .testTag("start_button")
            .clip(RoundedCornerShape(8.dp))
            .background(animatedBg)
            .border(
                width = 0.5.dp,
                color = if (isOpen) ElyCyan else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                role = Role.Button,
                onClickLabel = "Start Menu",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Windows 11 4-square cyan logo
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.size(6.5.dp).background(Color(0xFF0284C7), RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.size(6.5.dp).background(Color(0xFF0284C7), RoundedCornerShape(1.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.size(6.5.dp).background(Color(0xFF0284C7), RoundedCornerShape(1.dp)))
                Box(modifier = Modifier.size(6.5.dp).background(Color(0xFF0284C7), RoundedCornerShape(1.dp)))
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
            .testTag("search_button")
            .clip(RoundedCornerShape(8.dp))
            .background(if (isOpen) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.25f))
            .border(
                width = 0.5.dp,
                color = if (isOpen) ElyCyan else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                role = Role.Button,
                onClickLabel = "Search OS",
                onClick = onClick
            )
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
                tint = Color(0xFF334155),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Search",
                fontSize = 11.sp,
                color = Color(0xFF334155)
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun RunningAppIconPill(
    appId: AppId,
    isFocused: Boolean,
    isMinimized: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isFocused -> Color.White.copy(alpha = 0.65f)
        isMinimized -> Color.White.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.35f)
    }

    Box(
        modifier = Modifier
            .height(40.dp)
            .testTag("taskbar_tab_${appId.name}")
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                0.5.dp,
                if (isFocused) Color(0xFF0284C7).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true },
                role = Role.Tab,
                onClickLabel = "${appId.title} Window"
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
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = appId.shortName,
                    fontSize = 10.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFF0F172A),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Windows 11 style running indicator bar underneath active app
            Box(
                modifier = Modifier
                    .size(width = if (isFocused) 16.dp else 6.dp, height = 2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isFocused) Color(0xFF0284C7)
                        else if (isMinimized) Color(0xFF94A3B8)
                        else Color(0xFF64748B)
                    )
            )
        }

        // Process Context Menu
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                clippingEnabled = false
            ),
            modifier = Modifier
                .background(ElySurfaceCard)
                .border(1.dp, ElyTaskbarBorder, RoundedCornerShape(8.dp))
        ) {
            DropdownMenuItem(
                text = { Text(appId.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElyTextPrimary) },
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
                text = { Text("Close Window", fontSize = 12.sp, color = ElyError) },
                leadingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = ElyError) },
                onClick = {
                    showContextMenu = false
                    onClose()
                }
            )
        }
    }
}

