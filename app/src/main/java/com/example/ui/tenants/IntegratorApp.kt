package com.example.ui.tenants

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun IntegratorApp(
    nodes: List<PipelineNode>,
    status: PipelineRunStatus,
    progress: Float,
    executionLogs: List<String>,
    masterBundle: MasterIntegratedBundle?,
    onExecutePipeline: () -> Unit,
    onResetPipeline: () -> Unit,
    onOpenLyricApp: () -> Unit,
    onOpenCorpusApp: () -> Unit,
    onOpenArchiveApp: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Node Pipeline, 1 = Multi-Track Timeline, 2 = Master Artifact Inspector

    // Animated glow for executing state
    val infiniteTransition = rememberInfiniteTransition(label = "executing")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // App 03 Master Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ElyCyanBright.copy(alpha = 0.2f),
                            ElyViolet.copy(alpha = 0.15f),
                            ElyG3Axiom.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(1.dp, ElyCyanBright.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "APP 03 // THE INTEGRATOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyCyanBright
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (status) {
                                        PipelineRunStatus.EXECUTING -> ElyWarning.copy(alpha = 0.2f)
                                        PipelineRunStatus.COMPLETED -> ElyG3Axiom.copy(alpha = 0.2f)
                                        else -> ElyCyan.copy(alpha = 0.15f)
                                    }
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = when (status) {
                                    PipelineRunStatus.EXECUTING -> "ORCHESTRATING..."
                                    PipelineRunStatus.COMPLETED -> "G3 SEALED & SYNTHESIZED"
                                    else -> "BRIDGE READY"
                                },
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = when (status) {
                                    PipelineRunStatus.EXECUTING -> ElyWarning
                                    PipelineRunStatus.COMPLETED -> ElyG3Axiom
                                    else -> ElyCyan
                                }
                            )
                        }
                    }
                    Text(
                        text = "Cross-Tenant Synthesis & Forensic Orchestration Hub",
                        fontSize = 10.sp,
                        color = ElyTextSecondary
                    )
                }

                // Execute / Reset Trigger
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (status == PipelineRunStatus.COMPLETED) {
                        IconButton(
                            onClick = onResetPipeline,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = ElyTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Button(
                        onClick = onExecutePipeline,
                        enabled = status != PipelineRunStatus.EXECUTING,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == PipelineRunStatus.COMPLETED) ElyG3Axiom else ElyCyanBright
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        if (status == PipelineRunStatus.EXECUTING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Synthesizing...", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = if (status == PipelineRunStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (status == PipelineRunStatus.COMPLETED) "Re-Run Pipeline" else "Execute Pipeline",
                                fontSize = 10.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Progress Bar
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PIPELINE PROGRESSION",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElyTextTertiary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyCyanBright
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = ElyCyanBright,
                trackColor = ElySurfaceCard
            )
        }

        // Tab Navigation for Integrator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ElySurfaceCard)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("Visual Node Pipeline", "Multi-Track Timeline", "Master Artifact Suite")
            tabs.forEachIndexed { index, title ->
                val isSelected = activeTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ElyCyan.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = 0.5.dp,
                            color = if (isSelected) ElyCyan else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { activeTab = index }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ElyCyan else ElyTextSecondary
                    )
                }
            }
        }

        // Content Based on Active Tab
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (activeTab) {
                0 -> NodePipelineView(
                    nodes = nodes,
                    executionLogs = executionLogs,
                    pulseGlow = pulseGlow,
                    onOpenLyricApp = onOpenLyricApp,
                    onOpenCorpusApp = onOpenCorpusApp
                )
                1 -> MultiTrackTimelineView(status = status, masterBundle = masterBundle)
                2 -> MasterArtifactInspectorView(masterBundle = masterBundle, onOpenArchiveApp = onOpenArchiveApp)
            }
        }
    }
}

@Composable
private fun NodePipelineView(
    nodes: List<PipelineNode>,
    executionLogs: List<String>,
    pulseGlow: Float,
    onOpenLyricApp: () -> Unit,
    onOpenCorpusApp: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Bridge Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenCorpusApp,
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElyPurple)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Source App 02 (Corpus)", fontSize = 9.sp, color = ElyTextPrimary)
                }
                OutlinedButton(
                    onClick = onOpenLyricApp,
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElyCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Target App 01 (Lyrics)", fontSize = 9.sp, color = ElyTextPrimary)
                }
            }
        }

        // Pipeline Nodes List
        items(nodes) { node ->
            PipelineNodeCard(node = node, pulseGlow = pulseGlow)
        }

        // Real-Time Execution Console Log Output
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D16))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INTEGRATOR IPC CONSOLE LOGS",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyCyanBright
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(ElyG3Axiom)
                        )
                    }
                    executionLogs.takeLast(5).forEach { log ->
                        Text(
                            text = log,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyTextSecondary,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineNodeCard(node: PipelineNode, pulseGlow: Float) {
    val nodeColor = when (node.type) {
        NodeType.CORPUS_SOURCE -> ElyPurple
        NodeType.ELYZARETH_ENGINE_TRANSFORM -> ElyIndigo
        NodeType.LYRIC_SYNTHESIZER -> ElyCyan
        NodeType.G1_LEXICAL_GUARD -> ElyG1Lexical
        NodeType.G2_HARMONY_CHECK -> ElyG2Harmony
        NodeType.G3_AXIOMATIC_SEAL -> ElyG3Axiom
        NodeType.MASTER_OUTPUT_BUNDLE -> ElyCyanBright
        else -> ElyCyan
    }

    val isActive = node.status == VerificationState.ACTIVE
    val isVerified = node.status == VerificationState.VERIFIED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) nodeColor.copy(alpha = 0.15f * pulseGlow)
                else ElySurfaceCard
            )
            .border(
                width = if (isActive || isVerified) 1.dp else 0.5.dp,
                color = when {
                    isActive -> nodeColor.copy(alpha = pulseGlow)
                    isVerified -> nodeColor.copy(alpha = 0.6f)
                    else -> Color.White.copy(alpha = 0.08f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Node Status Indicator Dot / Icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isVerified -> nodeColor.copy(alpha = 0.2f)
                            isActive -> nodeColor.copy(alpha = 0.3f)
                            else -> Color(0xFF0F172A)
                        }
                    )
                    .border(1.dp, nodeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isVerified) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = nodeColor, modifier = Modifier.size(14.dp))
                } else if (isActive) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(nodeColor))
                } else {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ElyTextTertiary))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = node.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElyTextPrimary
                    )
                    Text(
                        text = node.outputMetric,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = nodeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = node.description,
                    fontSize = 9.sp,
                    color = ElyTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MultiTrackTimelineView(status: PipelineRunStatus, masterBundle: MasterIntegratedBundle?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "MULTIMODAL SYNTHESIS TIMELINE",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ElyCyan
            )
        }

        item {
            TrackRow(
                trackName = "TRACK 01 // CORPUS MOTIFS",
                source = "App 02 (Corpus Curator)",
                color = ElyPurple,
                segments = listOf("Codex Excerpt", "Philosophy Motif", "Axiom Lexicon", "Proportion Grid")
            )
        }

        item {
            TrackRow(
                trackName = "TRACK 02 // NEURAL LYRIC STREAM",
                source = "App 01 (Lyric Generator)",
                color = ElyCyan,
                segments = listOf("Verse 1 Cadence", "Chorus Climax", "Verse 2 Counterpoint", "Bridge Harmony")
            )
        }

        item {
            TrackRow(
                trackName = "TRACK 03 // FORENSIC GOVERNANCE",
                source = "G1 · G2 · G3 Engine Matrix",
                color = ElyG3Axiom,
                segments = listOf("G1 Meter 99.8%", "G2 Resonance", "G3 SHA-256 Seal", "Integrity Stamp")
            )
        }

        // Interactive Audio-Visualizer Simulation
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D16))
                    .border(0.5.dp, ElyCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(14.dp, 28.dp, 40.dp, 22.dp, 35.dp, 48.dp, 30.dp, 18.dp, 38.dp, 44.dp, 26.dp, 16.dp, 32.dp, 42.dp, 20.dp)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(h)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(ElyCyanBright, ElyViolet, ElyG3Axiom)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(trackName: String, source: String, color: Color, segments: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ElySurfaceCard)
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = trackName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = source, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = ElyTextTertiary)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            segments.forEach { seg ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.2f))
                        .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = seg,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MasterArtifactInspectorView(
    masterBundle: MasterIntegratedBundle?,
    onOpenArchiveApp: () -> Unit
) {
    if (masterBundle == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Cable, contentDescription = null, tint = ElyTextTertiary, modifier = Modifier.size(36.dp))
                Text(
                    text = "No Master Bundle generated yet.",
                    fontSize = 12.sp,
                    color = ElyTextSecondary
                )
                Text(
                    text = "Click 'Execute Pipeline' to orchestrate Corpus + Lyrics.",
                    fontSize = 10.sp,
                    color = ElyCyan
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElySurfaceCard)
                        .border(1.dp, ElyG3Axiom.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = masterBundle.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElyTextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElyG3Axiom.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = masterBundle.g3Hash,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ElyG3Axiom
                                )
                            }
                        }

                        Text(
                            text = masterBundle.synthesizedSummary,
                            fontSize = 11.sp,
                            color = ElyTextSecondary,
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "G1: ${masterBundle.g1Rating}", fontSize = 9.sp, color = ElyG1Lexical)
                            Text(text = "G2: ${masterBundle.g2Coherence}", fontSize = 9.sp, color = ElyG2Harmony)
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF090D16))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = masterBundle.fullMasterText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextPrimary,
                        lineHeight = 15.sp
                    )
                }
            }

            item {
                Button(
                    onClick = onOpenArchiveApp,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElyIndigo)
                ) {
                    Icon(Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View in Space Archive (App 05)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
