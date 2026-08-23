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
import com.example.model.EngineTelemetry
import com.example.ui.theme.*

@Composable
fun QuickSettingsDrawer(
    isOpen: Boolean,
    telemetry: EngineTelemetry,
    currentWallpaper: String,
    onSelectWallpaper: (String) -> Unit,
    onRestartOS: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight()
                .shadow(elevation = 32.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(ElyHeaderGlass)
                .border(1.dp, ElyTaskbarBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SPACE TELEMETRY & CONTROLS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElyG3Axiom.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "KERNEL ACTIVE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyG3Axiom
                        )
                    }
                }

                // G1 / G2 / G3 Real-time meters
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElySurfaceCard)
                        .padding(12.dp)
                ) {
                    TelemetryBar(label = "G1 Lyric Identity Guard", value = telemetry.g1LexicalLoad, color = ElyG1Lexical)
                    TelemetryBar(label = "G2 Realization & Harmony", value = telemetry.g2HarmonyLoad, color = ElyG2Harmony)
                    TelemetryBar(label = "G3 Performance Calibration", value = telemetry.g3AxiomLoad, color = ElyG3Axiom)
                }

                // Wallpaper switcher
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DESKTOP AMBIENCE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextTertiary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Obsidian Aurora", "Cyber Matrix", "Deep Mica").forEach { wp ->
                            val isSelected = currentWallpaper == wp
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) ElyCyan.copy(alpha = 0.2f) else ElySurfaceCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) ElyCyan else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onSelectWallpaper(wp) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = wp.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ElyCyan else ElyTextSecondary
                                )
                            }
                        }
                    }
                }

                // Engine Stats Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Tokens Processed", fontSize = 9.sp, color = ElyTextTertiary)
                        Text(
                            text = "${telemetry.totalProcessedTokens}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyCyan
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Governance Rate", fontSize = 9.sp, color = ElyTextTertiary)
                        Text(
                            text = "${telemetry.governancePassRate}%",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyG3Axiom
                        )
                    }
                }

                // Reset Action
                Button(
                    onClick = onRestartOS,
                    colors = ButtonDefaults.buttonColors(containerColor = ElySurfaceCard),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(ElyCyan, ElyPurple))),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Reset Space & Windows",
                        fontSize = 11.sp,
                        color = ElyTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryBar(label: String, value: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 9.sp, color = ElyTextSecondary)
            Text(
                text = "${(value * 100).toInt()}%",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = Color.Black.copy(alpha = 0.3f)
        )
    }
}
