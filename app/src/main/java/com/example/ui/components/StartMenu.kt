package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.model.TenantLifecycleState
import com.example.model.TenantResourceMetrics
import com.example.model.WindowData
import com.example.ui.theme.*

@Composable
fun StartMenu(
    isOpen: Boolean,
    searchQuery: String,
    windows: Map<AppId, WindowData> = emptyMap(),
    tenantMetrics: Map<AppId, TenantResourceMetrics> = emptyMap(),
    onSearchChange: (String) -> Unit,
    onLaunchApp: (AppId) -> Unit,
    onCascadeWindows: () -> Unit = {},
    onTileWindows: () -> Unit = {},
    onMinimizeAll: () -> Unit = {},
    onPurgeMemory: () -> Unit = {},
    onRestartOS: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tenants, 1: Desktop Tools

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .height(510.dp)
                .shadow(elevation = 32.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(ElyHeaderGlass)
                .border(
                    width = 1.dp,
                    color = ElyTaskbarBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Search Bar & Tabs
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = "Search tenants, lyrics, corpus, axioms...",
                                fontSize = 12.sp,
                                color = ElyTextTertiary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = ElyCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = ElyTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ElySurfaceCard,
                            unfocusedContainerColor = ElySurfaceCard,
                            focusedBorderColor = ElyCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = ElyTextPrimary,
                            unfocusedTextColor = ElyTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )

                    // Navigation Tabs: Pinned Tenants vs One Space Utilities
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(2.dp)
                    ) {
                        TabPill(
                            title = "Tenant Apps",
                            isSelected = selectedTab == 0,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )
                        TabPill(
                            title = "Space Tools",
                            isSelected = selectedTab == 1,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 1 }
                        )
                    }

                    if (selectedTab == 0) {
                        // Pinned Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AVAILABLE TENANTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ElyTextSecondary
                            )
                            Text(
                                text = "One Space Environment",
                                fontSize = 9.sp,
                                color = ElyCyan
                            )
                        }

                        // Pinned Apps List
                        val filteredApps = AppId.values().filter {
                            if (searchQuery.isBlank()) true
                            else it.title.contains(searchQuery, ignoreCase = true) ||
                                 it.subtitle.contains(searchQuery, ignoreCase = true)
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            items(filteredApps) { appId ->
                                val win = windows[appId]
                                val metrics = tenantMetrics[appId]
                                val isRunning = win != null && !win.isClosed

                                StartMenuAppRow(
                                    appId = appId,
                                    isRunning = isRunning,
                                    memoryMb = metrics?.allocatedMemoryMb ?: (if (isRunning) win.allocatedMemoryMb else 0f),
                                    onClick = { onLaunchApp(appId) }
                                )
                            }
                        }
                    } else {
                        // Desktop & Space Management Tools
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(270.dp)
                        ) {
                            Text(
                                text = "ONE SPACE DESKTOP UTILITIES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ElyTextSecondary
                            )

                            DesktopToolRow(
                                title = "Cascade All Windows",
                                subtitle = "Neatly step open windows diagonally",
                                icon = Icons.Default.FilterNone,
                                tint = ElyCyan,
                                onClick = onCascadeWindows
                            )

                            DesktopToolRow(
                                title = "Tile Windows",
                                subtitle = "Arrange side-by-side on desktop canvas",
                                icon = Icons.Default.ViewAgenda,
                                tint = ElyPurple,
                                onClick = onTileWindows
                            )

                            DesktopToolRow(
                                title = "Minimize All to Desktop",
                                subtitle = "Clear canvas and show desktop wallpaper",
                                icon = Icons.Default.Minimize,
                                tint = ElyG3Axiom,
                                onClick = onMinimizeAll
                            )

                            DesktopToolRow(
                                title = "Garbage Collect & Purge Memory",
                                subtitle = "Flush dormant caches and reclaim RAM",
                                icon = Icons.Default.CleaningServices,
                                tint = ElyWarning,
                                onClick = onPurgeMemory
                            )
                        }
                    }
                }

                // Bottom: Landlord / User Profile & Power Controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ElyCyan, ElyViolet)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "E",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "Elyzareth Prime",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElyTextPrimary
                                )
                                Text(
                                    text = "OS Landlord // Space Kernel",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ElyG3Axiom
                                )
                            }
                        }

                        // Reboot OS Button
                        IconButton(
                            onClick = onRestartOS,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Soft Reboot OS",
                                tint = ElyError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) ElyCyan.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) ElyCyan else ElyTextSecondary
        )
    }
}

@Composable
private fun DesktopToolRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ElyTextPrimary)
                Text(subtitle, fontSize = 9.sp, color = ElyTextTertiary)
            }
        }
    }
}

@Composable
private fun StartMenuAppRow(
    appId: AppId,
    isRunning: Boolean,
    memoryMb: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isRunning) ElyCyan.copy(alpha = 0.08f) else Color(0xFF1E293B).copy(alpha = 0.4f))
            .border(
                width = 0.5.dp,
                color = if (isRunning) ElyCyan.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                when (appId) {
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
                    imageVector = appId.defaultIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = appId.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElyTextPrimary
                    )
                }
                Text(
                    text = appId.subtitle,
                    fontSize = 9.sp,
                    color = ElyTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isRunning) ElyCyan.copy(alpha = 0.2f) else ElySurfaceCard)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isRunning) "RUNNING" else appId.tenantNumber,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isRunning) ElyCyanBright else ElyTextTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isRunning && memoryMb > 0f) {
                    Text(
                        text = "${"%.1f".format(memoryMb)} MB",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextTertiary
                    )
                }
            }
        }
    }
}
