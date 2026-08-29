package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioWitnessMetrics
import com.example.model.SpecimenVersion
import kotlinx.coroutines.delay
import kotlin.math.sin

// Winamp & Elyzareth Classic Deck Palette
private val WinampDeckBg = Color(0xFF1E2129)
private val WinampDeckBorder = Color(0xFFFFA500)
private val WinampLcdBg = Color(0xFF090D12)
private val WinampLcdGreen = Color(0xFF00FF66)
private val WinampLcdGreenDim = Color(0xFF00441B)
private val WinampAmber = Color(0xFFFFA500)
private val WinampAmberDim = Color(0xFF664400)
private val WinampButtonBg = Color(0xFF2B313D)
private val WinampButtonBorder = Color(0xFF454E5E)
private val WinampTextDim = Color(0xFF8E9BAE)

@Composable
fun ForensicWitnessPlayerDeck(
    specimen: SpecimenVersion?,
    songTitle: String = "Physical Audio Specimen",
    modifier: Modifier = Modifier,
    onTrackCompleted: () -> Unit = {}
) {
    val audioWitness: AudioWitnessMetrics? = specimen?.audioWitness
    val isAudioAvailable = audioWitness != null && audioWitness.isMeasured
    val durationSeconds = if (isAudioAvailable) (audioWitness?.durationSeconds ?: 180).coerceAtLeast(1) else 180

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionSeconds by remember { mutableStateOf(19f) } // default forensic marker as seen in spec
    var volume by remember { mutableStateOf(0.85f) }
    var pan by remember { mutableStateOf(0f) } // -1f (L) to +1f (R), 0 is Center
    var isLooping by remember { mutableStateOf(true) }
    var isShuffle by remember { mutableStateOf(false) }
    var isEjectInfoOpen by remember { mutableStateOf(false) }

    // Playback progress ticker
    LaunchedEffect(isPlaying, durationSeconds) {
        while (isPlaying) {
            delay(1000)
            if (currentPositionSeconds < durationSeconds) {
                currentPositionSeconds += 1f
            } else {
                if (isLooping) {
                    currentPositionSeconds = 0f
                } else {
                    isPlaying = false
                    currentPositionSeconds = 0f
                    onTrackCompleted()
                }
            }
        }
    }

    // Dynamic LED visualizer pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "winamp_vu")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vu_phase"
    )

    // Formatted Time MM:SS
    val elapsedMin = (currentPositionSeconds.toInt() / 60).toString().padStart(2, '0')
    val elapsedSec = (currentPositionSeconds.toInt() % 60).toString().padStart(2, '0')
    val totalMin = (durationSeconds / 60).toString().padStart(2, '0')
    val totalSec = (durationSeconds % 60).toString().padStart(2, '0')

    val bitrateLabel = if (isAudioAvailable) "224 kbps" else "NO PCM"
    val sampleRateLabel = if (isAudioAvailable) "${audioWitness?.sampleRateKhz ?: 44.1f} kHz" else "NOT MEASURED"
    val channelsLabel = if (isAudioAvailable) (if (audioWitness?.channels == 1) "MONO" else "STEREO") else "UNMEASURED"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WinampDeckBg)
            .border(1.2.dp, WinampAmber.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // =========================================================================
        // 1. TOP TITLE BAR & FORMAT BADGE
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Green LED status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) WinampLcdGreen else WinampLcdGreenDim)
                        .border(0.5.dp, if (isPlaying) Color.White else Color.Transparent, CircleShape)
                )

                Text(
                    text = "ELYZARETH PLAYER DECK",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = WinampAmber
                )
            }

            // Right Format Badge (9:16 Shorts / Reels or Forensic SAF)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF15181F))
                    .border(0.5.dp, WinampAmber.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "9:16 SHORTS / REELS",
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = WinampAmber
                )
            }
        }

        // =========================================================================
        // 2. RETRO LCD SCREEN (Timer, Audio Spec Badges, Title Marquee, Visualizer)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(WinampLcdBg)
                .border(1.dp, Color(0xFF2F3846), RoundedCornerShape(5.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                // Row: Timer + Audio Spec Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Large Green Phosphor Digital Readout
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isPlaying) "►" else "■",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) WinampLcdGreen else WinampLcdGreenDim
                        )
                        Text(
                            text = "$elapsedMin:$elapsedSec",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = WinampLcdGreen,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "/ $totalMin:$totalSec",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = WinampLcdGreenDim
                        )
                    }

                    // Right: Technical Spec Badges
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        LcdBadge(bitrateLabel)
                        LcdBadge(sampleRateLabel)
                        LcdBadge(channelsLabel)
                    }
                }

                // Center Marquee: Track Title & Specimen Hash Stamp
                val specimenStamp = specimen?.specimenId?.take(8) ?: "6a67000a"
                val marqueeText = "$songTitle ($specimenStamp) • Local Artist • $bitrateLabel"
                Text(
                    text = marqueeText,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = WinampAmber,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Visualizer: Multi-segment Green LED horizontal level bars
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF040608))
                ) {
                    val width = size.width
                    val height = size.height
                    val barCount = 18
                    val gap = 3.dp.toPx()
                    val barWidth = (width - (barCount - 1) * gap) / barCount

                    for (i in 0 until barCount) {
                        val x = i * (barWidth + gap)
                        val factor = if (isPlaying) {
                            (sin(pulsePhase + i * 0.45f) * 0.45f + 0.55f).coerceIn(0.1f, 1f)
                        } else {
                            0.08f
                        }
                        val barHeight = height * factor

                        // Segments per bar
                        val segments = 7
                        val segmentGap = 1.dp.toPx()
                        val segmentHeight = (height - (segments - 1) * segmentGap) / segments
                        val activeSegments = (segments * factor).toInt().coerceAtLeast(if (isPlaying) 1 else 0)

                        for (s in 0 until segments) {
                            val segY = height - (s + 1) * (segmentHeight + segmentGap)
                            val isActive = s < activeSegments
                            val segColor = when {
                                !isActive -> WinampLcdGreenDim.copy(alpha = 0.25f)
                                s >= segments - 1 -> Color(0xFFFF3333) // Peak red
                                s >= segments - 2 -> Color(0xFFFFCC00) // Warning yellow
                                else -> WinampLcdGreen // Normal green
                            }
                            drawRect(
                                color = segColor,
                                topLeft = Offset(x, segY),
                                size = Size(barWidth, segmentHeight)
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. PLAYBACK SEEK BAR / PROGRESS TRACK
        // =========================================================================
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Slider(
                value = currentPositionSeconds.coerceIn(0f, durationSeconds.toFloat()),
                onValueChange = { currentPositionSeconds = it },
                valueRange = 0f..durationSeconds.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = WinampLcdGreen,
                    inactiveTrackColor = Color(0xFF18202C)
                )
            )
        }

        // =========================================================================
        // 4. DUAL CONTROLS: VOLUME & PAN SLIDERS
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Volume Control
            Row(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF151820))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = WinampAmber,
                    modifier = Modifier.size(13.dp)
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = WinampAmber,
                        activeTrackColor = WinampAmber,
                        inactiveTrackColor = Color(0xFF28303E)
                    )
                )
                Text(
                    text = "${(volume * 100).toInt()}%",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = WinampAmber
                )
            }

            // Right: Pan Control
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF151820))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "PAN",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = WinampAmber
                )
                Slider(
                    value = pan,
                    onValueChange = { pan = it },
                    valueRange = -1f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = WinampAmber,
                        activeTrackColor = WinampAmber,
                        inactiveTrackColor = Color(0xFF28303E)
                    )
                )
                val panLabel = when {
                    pan < -0.1f -> "L${(kotlin.math.abs(pan) * 100).toInt()}"
                    pan > 0.1f -> "R${(pan * 100).toInt()}"
                    else -> "C"
                }
                Text(
                    text = panLabel,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = WinampAmber
                )
            }
        }

        // =========================================================================
        // 5. PHYSICAL BEVELED WINAMP TRANSPORT BUTTONS
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous / Restart
            WinampTransportButton(
                icon = Icons.Default.SkipPrevious,
                contentDescription = "Previous / Restart",
                modifier = Modifier.weight(1f),
                onClick = { currentPositionSeconds = 0f }
            )

            // Play / Pause (Glowing Green)
            WinampTransportButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                isPrimary = true,
                isActive = isPlaying,
                modifier = Modifier.weight(1.2f),
                onClick = { isPlaying = !isPlaying }
            )

            // Stop
            WinampTransportButton(
                icon = Icons.Default.Stop,
                contentDescription = "Stop",
                modifier = Modifier.weight(1f),
                onClick = {
                    isPlaying = false
                    currentPositionSeconds = 0f
                }
            )

            // Next Track
            WinampTransportButton(
                icon = Icons.Default.SkipNext,
                contentDescription = "Next Track",
                modifier = Modifier.weight(1f),
                onClick = {
                    currentPositionSeconds = 0f
                }
            )

            // Eject / File Info
            WinampTransportButton(
                icon = Icons.Default.Eject,
                contentDescription = "Eject / Specimen Info",
                isActive = isEjectInfoOpen,
                modifier = Modifier.weight(1f),
                onClick = { isEjectInfoOpen = !isEjectInfoOpen }
            )

            // Shuffle
            WinampTransportButton(
                icon = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                isActive = isShuffle,
                modifier = Modifier.weight(0.9f),
                onClick = { isShuffle = !isShuffle }
            )

            // Repeat / Loop
            WinampTransportButton(
                icon = Icons.Default.Repeat,
                contentDescription = "Loop / Repeat",
                isActive = isLooping,
                modifier = Modifier.weight(0.9f),
                onClick = { isLooping = !isLooping }
            )
        }

        // =========================================================================
        // 6. FORENSIC INTEGRITY NOTICE & WITNESS BANNER
        // =========================================================================
        if (isEjectInfoOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF10141D))
                    .border(0.5.dp, WinampAmber.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "SPECIMEN AUDIT METADATA",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = WinampAmber
                )
                Text(
                    text = "Specimen ID: ${specimen?.specimenId ?: "UNKNOWN"}",
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = WinampTextDim
                )
                Text(
                    text = "SHA-256: ${specimen?.sha256Hash ?: "sha256:unmeasured"}",
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    color = WinampLcdGreen
                )
                Text(
                    text = "Physical Hash: ${audioWitness?.physicalFileHash ?: "NOT MEASURED"}",
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isAudioAvailable) WinampLcdGreen else WinampAmber
                )
            }
        }
    }
}

@Composable
private fun LcdBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF131A24))
            .border(0.5.dp, WinampAmber.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = text,
            fontSize = 7.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = WinampAmber
        )
    }
}

@Composable
private fun WinampTransportButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val buttonBg = when {
        isPrimary && isActive -> Color(0xFF003816)
        isActive -> Color(0xFF3B2F17)
        else -> WinampButtonBg
    }
    val buttonBorder = when {
        isPrimary && isActive -> WinampLcdGreen
        isActive -> WinampAmber
        else -> WinampButtonBorder
    }
    val iconTint = when {
        isPrimary && isActive -> WinampLcdGreen
        isActive -> WinampAmber
        else -> Color(0xFFCCD6E6)
    }

    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(buttonBg)
            .border(1.dp, buttonBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
    }
}
