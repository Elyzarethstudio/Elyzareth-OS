package com.example.ui.tenants

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.components.ForensicWitnessPlayerDeck
import com.example.ui.theme.*

/**
 * 🛋️ CORPUS / LYRIC CURATOR (The Sitting Room)
 * Elyzareth OS — App 02
 *
 * Forensic Examination Workspace & Curatorial Triage.
 * Primary Visible Language: WITNESS • EXAMINATION • FINDINGS • DISPOSITION
 *
 * Responsive:
 * - Compact / Mobile Window: Vertically stacked curatorial sections, high-contrast readable typography,
 *   convenient specimen selector pill & quick ingress actions.
 * - Expanded / Desktop Window: Multi-column workstation layout with Corpus Explorer sidebar.
 *
 * Evidence Boundaries:
 * - G1–G5 internal technical details available in AUDIT / TECHNICAL DETAILS.
 * - NOT MEASURED is strictly displayed when physical audio/acoustic evidence is absent.
 */
@Composable
fun CorpusCuratorApp(
    baseCompositions: List<BaseComposition>,
    selectedBaseCompositionId: String,
    selectedVersionId: String,
    sittingRoomTab: String,
    selectedGateDiagnostic: GateDiagnostic?,
    corpusSearch: String = "",
    corpusInventoryReport: CorpusInventoryReport = CorpusInventoryReport(),
    isIngressDialogOpen: Boolean = false,
    ingressDialogType: String = "LYRIC",
    ingressTitle: String = "",
    ingressLyricText: String = "",
    ingressSourceOrigin: IngressSourceOrigin = IngressSourceOrigin.LAPTOP,
    ingressAudioIncluded: Boolean = false,
    ingressAudioDecoderPass: Boolean = true,
    onSelectBaseComposition: (String) -> Unit,
    onSelectVersion: (String) -> Unit,
    onSelectTab: (String) -> Unit,
    onSelectGate: (GateDiagnostic?) -> Unit,
    onPreserve: () -> Unit,
    onAccept: () -> Unit,
    onSendToEngine: () -> Unit,
    onSearchChange: (String) -> Unit = {},
    onOpenIngressDialog: (String) -> Unit = {},
    onCloseIngressDialog: () -> Unit = {},
    onIngressTitleChange: (String) -> Unit = {},
    onIngressLyricTextChange: (String) -> Unit = {},
    onIngressSourceOriginChange: (IngressSourceOrigin) -> Unit = {},
    onIngressAudioIncludedChange: (Boolean) -> Unit = {},
    onIngressAudioDecoderPassChange: (Boolean) -> Unit = {},
    onCommitIngress: () -> Unit = {},
    onCommitHumanGovernorDisposition: (GovernanceDispositionChoice, String) -> Unit = { _, _ -> },
    onCommitHumanEarDisposition: (String, String, HumanEarDisposition, String) -> Unit = { _, _, _, _ -> },
    onUpdateHumanEarReview: (String, String, HumanEarReview) -> Unit = { _, _, _ -> },
    onIngestSafFolder: (Context, Uri) -> Unit = { _, _ -> },
    onIngestSafDocument: (Context, Uri) -> Unit = { _, _ -> },
    onStartSafScan: (Context, Uri) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    // Android SAF System Picker Launchers
    val safFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            onStartSafScan(context, uri)
            onIngestSafFolder(context, uri)
        }
    }

    val safDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onIngestSafDocument(context, uri)
        }
    }
    val filteredBases = remember(baseCompositions, corpusSearch) {
        if (corpusSearch.isBlank()) baseCompositions
        else baseCompositions.filter {
            it.title.contains(corpusSearch, ignoreCase = true) ||
            it.era.contains(corpusSearch, ignoreCase = true) ||
            it.versions.any { v -> v.specimenId.contains(corpusSearch, ignoreCase = true) || v.lyricText.contains(corpusSearch, ignoreCase = true) }
        }
    }

    val selectedBase = baseCompositions.find { it.id == selectedBaseCompositionId }
        ?: baseCompositions.firstOrNull()
    val selectedVersion = selectedBase?.versions?.find { it.versionId == selectedVersionId }
        ?: selectedBase?.versions?.firstOrNull()

    // Normalize curatorial view tab: INGEST → VERIFY → MEASURE → HUMAN EAR REVIEW → DISPOSITION
    val currentTab = when (sittingRoomTab.uppercase()) {
        "DRY RUN", "DRY RUN (~390)", "DRY_RUN", "INVENTORY" -> "DRY RUN (~390)"
        "ALL" -> "DRY RUN (~390)"
        "LYRIC", "WITNESS" -> "WITNESS"
        "AUDIO", "EXAMINATION" -> "EXAMINATION"
        "EVIDENCE", "FINDINGS" -> "FINDINGS"
        "EAR REVIEW", "HUMAN EAR REVIEW", "LISTENING", "FORENSIC LISTENING" -> "HUMAN EAR REVIEW"
        "DISPOSITION" -> "DISPOSITION"
        "HISTORY", "AUDIT", "AUDIT & TECHNICAL" -> "AUDIT & TECHNICAL"
        else -> "DRY RUN (~390)"
    }

    var isExplorerVisible by remember { mutableStateOf(true) }
    var isMobileSpecimenPickerOpen by remember { mutableStateOf(false) }
    var isTechnicalAuditExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ElyBackground)
    ) {
        // App 02 Top Forensic Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElyHeaderGlass)
                .border(0.5.dp, ElyWindowBorderInactive)
                .padding(horizontal = 10.dp, vertical = 6.dp)
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
                    Text(
                        text = "🛋️",
                        fontSize = 16.sp
                    )
                    Column {
                        Text(
                            text = "ELYZARETH OS // SITTING ROOM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyPurple
                        )
                        Text(
                            text = "Forensic Examination Workspace & Curatorial Triage",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElyTextPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElyHeaderGlass)
                            .border(0.5.dp, ElyG3Axiom.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "IMMUTABLE WITNESS",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyG3Axiom
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElyPurple.copy(alpha = 0.15f))
                            .border(0.5.dp, ElyPurple.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "HUMAN GOVERNOR",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyPurple
                        )
                    }
                }
            }
        }

        // Responsive Work Area Container
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val totalWidth = maxWidth
            val isCompact = totalWidth < 580.dp

            if (isCompact) {
                // ==========================================
                // MOBILE / COMPACT WINDOW PRESENTATION
                // ==========================================
                MobileCuratorialWorkspace(
                    selectedBase = selectedBase,
                    selectedVersion = selectedVersion,
                    baseCompositions = baseCompositions,
                    currentTab = currentTab,
                    corpusInventoryReport = corpusInventoryReport,
                    selectedGateDiagnostic = selectedGateDiagnostic,
                    isTechnicalAuditExpanded = isTechnicalAuditExpanded,
                    onSelectTab = onSelectTab,
                    onSelectVersion = onSelectVersion,
                    onSelectGate = onSelectGate,
                    onToggleTechnicalAudit = { isTechnicalAuditExpanded = !isTechnicalAuditExpanded },
                    onOpenPicker = { isMobileSpecimenPickerOpen = true },
                    onOpenIngressDialog = onOpenIngressDialog,
                    onPreserve = onPreserve,
                    onAccept = onAccept,
                    onSendToEngine = onSendToEngine,
                    onCommitHumanGovernorDisposition = onCommitHumanGovernorDisposition,
                    onCommitHumanEarDisposition = onCommitHumanEarDisposition,
                    onUpdateHumanEarReview = onUpdateHumanEarReview,
                    onStartSafScan = onStartSafScan
                )
            } else {
                // ==========================================
                // DESKTOP / EXPANDED WORKSTATION PRESENTATION
                // ==========================================
                Row(modifier = Modifier.fillMaxSize()) {
                    // LEFT ZONE: Corpus Explorer Sidebar
                    if (isExplorerVisible) {
                        Column(
                            modifier = Modifier
                                .width(150.dp)
                                .fillMaxHeight()
                                .background(ElySurfaceCard.copy(alpha = 0.7f))
                                .border(0.5.dp, ElyWindowBorderInactive)
                                .padding(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CORPUS EXPLORER",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = ElyTextSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide Explorer",
                                    tint = ElyTextTertiary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { isExplorerVisible = false }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // Search Bar
                            OutlinedTextField(
                                value = corpusSearch,
                                onValueChange = onSearchChange,
                                placeholder = { Text("Search...", fontSize = 8.5.sp, color = ElyTextTertiary) },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 9.sp, color = ElyTextPrimary),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElyPurple,
                                    unfocusedBorderColor = ElyWindowBorderInactive,
                                    focusedContainerColor = ElyHeaderGlass,
                                    unfocusedContainerColor = ElyHeaderGlass
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                shape = RoundedCornerShape(4.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Dry Run Quick Launcher
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (currentTab == "DRY RUN (~390)") ElyPurple else ElyHeaderGlass)
                                    .border(0.5.dp, ElyPurple.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .clickable { onSelectTab("DRY RUN (~390)") }
                                    .padding(vertical = 5.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡ DRY RUN (~390)",
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentTab == "DRY RUN (~390)") Color.White else ElyPurple
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Ingress Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                IngressButton("+ CORP", { onOpenIngressDialog("CORPUS") }, Modifier.weight(1f))
                                IngressButton("+ LYR", { onOpenIngressDialog("LYRIC") }, Modifier.weight(1f))
                                IngressButton("+ AUD", { onOpenIngressDialog("AUDIO") }, Modifier.weight(1f))
                                IngressButton("📁 SAF", { safFolderLauncher.launch(null) }, Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Corpus Hierarchy List
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                items(filteredBases) { base ->
                                    val isBaseSelected = base.id == selectedBase?.id
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isBaseSelected) ElyTileActive else Color.Transparent)
                                            .clickable { onSelectBaseComposition(base.id) }
                                            .padding(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = if (isBaseSelected) "📂" else "📁",
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = base.title,
                                                fontSize = 8.5.sp,
                                                fontWeight = if (isBaseSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isBaseSelected) ElyTextPrimary else ElyTextSecondary,
                                                maxLines = 1
                                            )
                                        }

                                        // Child Version Nodes
                                        if (isBaseSelected) {
                                            Column(modifier = Modifier.padding(start = 10.dp, top = 3.dp)) {
                                                base.versions.forEach { version ->
                                                    val isVerSelected = version.versionId == selectedVersion?.versionId
                                                    val (statusColor, statusLabel) = when (version.decision) {
                                                        SpecimenDecision.ACCEPT -> Pair(ElyG3Axiom, "🟢")
                                                        SpecimenDecision.NEEDS_HEALING -> Pair(ElyAmberWarning, "🟡")
                                                        SpecimenDecision.NOT_ELIGIBLE -> Pair(ElyError, "🔴")
                                                        SpecimenDecision.NOT_YET_EXAMINED -> Pair(ElyTextTertiary, "⚪")
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(if (isVerSelected) ElyHeaderGlass else Color.Transparent)
                                                            .clickable { onSelectVersion(version.versionId) }
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(text = statusLabel, fontSize = 7.sp)
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Column {
                                                            Text(
                                                                text = version.versionId,
                                                                fontSize = 8.sp,
                                                                fontFamily = FontFamily.Monospace,
                                                                fontWeight = if (isVerSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isVerSelected) ElyTextPrimary else ElyTextSecondary
                                                            )
                                                            Text(
                                                                text = version.specimenId,
                                                                fontSize = 6.5.sp,
                                                                fontFamily = FontFamily.Monospace,
                                                                color = ElyTextTertiary
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // RIGHT ZONE: Expanded Forensic Examination Workstation
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(8.dp)
                    ) {
                        if (selectedBase != null && selectedVersion != null) {
                            // Specimen Header Banner
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!isExplorerVisible) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ElyHeaderGlass)
                                                .border(0.5.dp, ElyPurple.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .clickable { isExplorerVisible = true }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = ElyPurple, modifier = Modifier.size(12.dp))
                                                Text("Corpus", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = ElyPurple)
                                            }
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = selectedBase.title.uppercase(),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElyTextPrimary
                                        )
                                        Text(
                                            text = "Specimen: ${selectedVersion.specimenId} • Version: ${selectedVersion.versionId} • Origin: ${selectedVersion.sourceOrigin.name.replace('_', ' ')}",
                                            fontSize = 9.sp,
                                            color = ElyTextSecondary
                                        )
                                    }
                                }

                                // Immutable Stamp Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElyHeaderGlass)
                                        .border(0.5.dp, ElyG3Axiom.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ElyG3Axiom, modifier = Modifier.size(10.dp))
                                        Text(text = "WITNESS VERIFIED", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyG3Axiom)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Curatorial Navigation Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElyHeaderGlass)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                val tabs = listOf("DRY RUN (~390)", "WITNESS", "EXAMINATION", "FINDINGS", "HUMAN EAR REVIEW", "DISPOSITION", "AUDIT & TECHNICAL")
                                tabs.forEach { tab ->
                                    val isTabSelected = (currentTab == tab)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isTabSelected) ElyTileActive else Color.Transparent)
                                            .clickable { onSelectTab(tab) }
                                            .padding(vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tab,
                                            fontSize = 9.sp,
                                            fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isTabSelected) ElyPurple else ElyTextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Tab Body Area
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElySurfaceCard)
                                    .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                when (currentTab) {
                                    "DRY RUN (~390)", "DRY RUN" -> CorpusDiscoveryReportView(
                                        report = corpusInventoryReport,
                                        onStartSafScan = onStartSafScan
                                    )
                                    "WITNESS" -> DesktopWitnessView(selectedVersion)
                                    "EXAMINATION" -> ExaminationSectionView(selectedVersion)
                                    "FINDINGS" -> FindingsSectionView(selectedVersion)
                                    "HUMAN EAR REVIEW", "EAR REVIEW" -> HumanEarReviewSectionView(
                                        baseComposition = selectedBase,
                                        selectedVersion = selectedVersion,
                                        onCommitHumanEarDisposition = onCommitHumanEarDisposition,
                                        onUpdateHumanEarReview = onUpdateHumanEarReview
                                    )
                                    "DISPOSITION" -> DispositionSectionView(
                                        selectedVersion = selectedVersion,
                                        onPreserve = onPreserve,
                                        onAccept = onAccept,
                                        onSendToEngine = onSendToEngine,
                                        onCommitHumanGovernorDisposition = onCommitHumanGovernorDisposition
                                    )
                                    "AUDIT & TECHNICAL" -> AuditSectionView(
                                        selectedVersion = selectedVersion,
                                        selectedGateDiagnostic = selectedGateDiagnostic,
                                        onSelectGate = onSelectGate
                                    )
                                    else -> DesktopWitnessView(selectedVersion)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Status Bar
        if (selectedVersion != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElyHeaderGlass)
                    .border(0.5.dp, ElyWindowBorderInactive)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "STATUS:",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyTextSecondary
                        )
                        Text(
                            text = "Witness: VERIFIED",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyG3Axiom
                        )
                        Text(text = "•", fontSize = 8.5.sp, color = ElyTextTertiary)
                        Text(
                            text = "Lyric: EXAMINED",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyG3Axiom
                        )
                        Text(text = "•", fontSize = 8.5.sp, color = ElyTextTertiary)
                        val audioMeasured = selectedVersion.audioWitness?.isMeasured == true
                        Text(
                            text = if (audioMeasured) "Audio: MEASURED" else "Audio: NOT MEASURED",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (audioMeasured) ElyG3Axiom else ElyAmberWarning
                        )
                        Text(text = "•", fontSize = 8.5.sp, color = ElyTextTertiary)
                        Text(
                            text = "Governor: AWAITING",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyPurple
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(ElyTileActive)
                            .border(0.5.dp, ElyPurple.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                            .clickable {
                                onSelectTab(if (currentTab == "AUDIT & TECHNICAL") "ALL" else "AUDIT & TECHNICAL")
                                isTechnicalAuditExpanded = !isTechnicalAuditExpanded
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isTechnicalAuditExpanded || currentTab == "AUDIT & TECHNICAL") "Hide Audit ✕" else "Audit Details ▾",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyPurple
                        )
                    }
                }
            }
        }
    }

    // Mobile Specimen Picker Sheet Dialog
    if (isMobileSpecimenPickerOpen) {
        Dialog(onDismissRequest = { isMobileSpecimenPickerOpen = false }) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ElyBackground,
                border = BorderStroke(1.dp, ElyPurple),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT CORPUS SPECIMEN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyPurple
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ElyTextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { isMobileSpecimenPickerOpen = false }
                        )
                    }

                    HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(baseCompositions) { base ->
                            val isBaseSelected = base.id == selectedBase?.id
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isBaseSelected) ElyTileActive else ElyHeaderGlass)
                                    .border(0.5.dp, if (isBaseSelected) ElyPurple else ElyWindowBorderInactive, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectBaseComposition(base.id)
                                            base.versions.firstOrNull()?.let { onSelectVersion(it.versionId) }
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📂", fontSize = 14.sp)
                                    Text(
                                        text = base.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElyTextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                base.versions.forEach { ver ->
                                    val isVerSelected = ver.versionId == selectedVersion?.versionId
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isVerSelected) ElyPurple.copy(alpha = 0.2f) else Color.Transparent)
                                            .clickable {
                                                onSelectBaseComposition(base.id)
                                                onSelectVersion(ver.versionId)
                                                isMobileSpecimenPickerOpen = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• ${ver.versionId} (${ver.specimenId})",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isVerSelected) ElyPurple else ElyTextSecondary
                                        )
                                        val statusBadge = when (ver.decision) {
                                            SpecimenDecision.ACCEPT -> "🟢 ACCEPT"
                                            SpecimenDecision.NEEDS_HEALING -> "🟡 HEAL"
                                            SpecimenDecision.NOT_ELIGIBLE -> "🔴 REJECT"
                                            SpecimenDecision.NOT_YET_EXAMINED -> "⚪ UNEXAMINED"
                                        }
                                        Text(
                                            text = statusBadge,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = ElyTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Ingress Specimen Modal Dialog
    if (isIngressDialogOpen) {
        Dialog(onDismissRequest = onCloseIngressDialog) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ElyBackground,
                border = BorderStroke(1.dp, ElyPurple),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FORENSIC SPECIMEN INGRESS // $ingressDialogType",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyPurple
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ElyTextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onCloseIngressDialog() }
                        )
                    }
                    HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

                    // Title
                    Text("Composition / Song Title:", fontSize = 9.5.sp, color = ElyTextSecondary)
                    OutlinedTextField(
                        value = ingressTitle,
                        onValueChange = onIngressTitleChange,
                        placeholder = { Text("e.g., Midnight Railway & Silver Coin", fontSize = 9.5.sp, color = ElyTextTertiary) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 10.5.sp, color = ElyTextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElyPurple,
                            unfocusedBorderColor = ElyWindowBorderInactive,
                            focusedContainerColor = ElyHeaderGlass,
                            unfocusedContainerColor = ElyHeaderGlass
                        ),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Origin
                    Text("Physical Source Origin:", fontSize = 9.5.sp, color = ElyTextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(IngressSourceOrigin.entries) { origin ->
                            val isSel = origin == ingressSourceOrigin
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) ElyPurple else ElyHeaderGlass)
                                    .border(0.5.dp, if (isSel) ElyPurple else ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                                    .clickable { onIngressSourceOriginChange(origin) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = origin.name.replace('_', ' '),
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSel) Color.White else ElyTextSecondary
                                )
                            }
                        }
                    }

                    if (ingressSourceOrigin == IngressSourceOrigin.LOCAL_FOLDER) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElyPurple.copy(alpha = 0.15f))
                                .border(0.5.dp, ElyPurple.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ANDROID SAF SYSTEM PICKER", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyPurple)
                                Text("Select local folder tree or artifact directly via Android SAF", fontSize = 8.sp, color = ElyTextSecondary)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { safFolderLauncher.launch(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElyPurple),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("📂 Folder", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                                }
                                Button(
                                    onClick = { safDocumentLauncher.launch(arrayOf("*/*")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("📄 File", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = ElyTextPrimary)
                                }
                            }
                        }
                    }

                    // Raw Lyric Specimen
                    Text("Canonical Text Lyric (Immutable Specimen):", fontSize = 9.5.sp, color = ElyTextSecondary)
                    OutlinedTextField(
                        value = ingressLyricText,
                        onValueChange = onIngressLyricTextChange,
                        placeholder = { Text("[Verse 1]\nAcross the wooden table sits the silver coin...", fontSize = 9.5.sp, color = ElyTextTertiary) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = ElyTextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElyPurple,
                            unfocusedBorderColor = ElyWindowBorderInactive,
                            focusedContainerColor = ElyHeaderGlass,
                            unfocusedContainerColor = ElyHeaderGlass
                        ),
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Audio Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElyHeaderGlass)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ATTACH PHYSICAL AUDIO WITNESS", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyTextPrimary)
                            Text("Provide PCM/decoder stream for physical audio verification", fontSize = 8.sp, color = ElyTextSecondary)
                        }
                        Switch(
                            checked = ingressAudioIncluded,
                            onCheckedChange = onIngressAudioIncludedChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = ElyPurple, checkedTrackColor = ElyPurple.copy(alpha = 0.5f))
                        )
                    }

                    if (ingressAudioIncluded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Audio Decoder Health:", fontSize = 9.5.sp, color = ElyTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onIngressAudioDecoderPassChange(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (ingressAudioDecoderPass) ElyG3Axiom else ElyHeaderGlass),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("PASS (Clean PCM)", fontSize = 8.5.sp, color = if (ingressAudioDecoderPass) Color.Black else ElyTextPrimary)
                                }
                                Button(
                                    onClick = { onIngressAudioDecoderPassChange(false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (!ingressAudioDecoderPass) ElyError else ElyHeaderGlass),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("FAIL (Corrupt Stream)", fontSize = 8.5.sp, color = if (!ingressAudioDecoderPass) Color.White else ElyTextPrimary)
                                }
                            }
                        }
                    }

                    // Commit Ingress Button
                    Button(
                        onClick = onCommitIngress,
                        colors = ButtonDefaults.buttonColors(containerColor = ElyPurple),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("COMMIT TO FORENSIC INGRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
                    }
                }
            }
        }
    }
}

// ==============================================================================
// MOBILE CURATORIAL WORKSPACE (Vertically Stacked, Highly Readable)
// ==============================================================================
@Composable
private fun MobileCuratorialWorkspace(
    selectedBase: BaseComposition?,
    selectedVersion: SpecimenVersion?,
    baseCompositions: List<BaseComposition>,
    currentTab: String,
    corpusInventoryReport: CorpusInventoryReport = CorpusInventoryReport(),
    selectedGateDiagnostic: GateDiagnostic?,
    isTechnicalAuditExpanded: Boolean,
    onSelectTab: (String) -> Unit,
    onSelectVersion: (String) -> Unit,
    onSelectGate: (GateDiagnostic?) -> Unit,
    onToggleTechnicalAudit: () -> Unit,
    onOpenPicker: () -> Unit,
    onOpenIngressDialog: (String) -> Unit,
    onPreserve: () -> Unit,
    onAccept: () -> Unit,
    onSendToEngine: () -> Unit,
    onCommitHumanGovernorDisposition: (GovernanceDispositionChoice, String) -> Unit,
    onCommitHumanEarDisposition: (String, String, HumanEarDisposition, String) -> Unit = { _, _, _, _ -> },
    onUpdateHumanEarReview: (String, String, HumanEarReview) -> Unit = { _, _, _ -> },
    onStartSafScan: (Context, Uri) -> Unit = { _, _ -> }
) {
    if (currentTab == "DRY RUN" || currentTab == "DRY RUN (~390)") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Curatorial Navigation Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ElyHeaderGlass)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val sections = listOf("DRY RUN", "ALL", "WITNESS", "EXAMINATION", "FINDINGS", "HUMAN EAR REVIEW", "DISPOSITION")
                sections.forEach { sec ->
                    val isSelected = currentTab.startsWith(sec)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) ElyTileActive else Color.Transparent)
                            .clickable { onSelectTab(sec) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sec,
                            fontSize = 7.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) ElyPurple else ElyTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                CorpusDiscoveryReportView(
                    report = corpusInventoryReport,
                    onStartSafScan = onStartSafScan
                )
            }
        }
        return
    }

    if (selectedBase == null || selectedVersion == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No specimen selected in Sitting Room.", color = ElyTextSecondary, fontSize = 12.sp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Specimen Selector Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(ElyHeaderGlass)
                .border(0.5.dp, ElyPurple.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenPicker() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = ElyPurple, modifier = Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = selectedBase.title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElyTextPrimary,
                            maxLines = 1
                        )
                        Text("▾", fontSize = 10.sp, color = ElyPurple)
                    }
                    Text(
                        text = "Ver: ${selectedVersion.versionId} • ${selectedVersion.specimenId.take(14)}...",
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextSecondary
                    )
                }
            }

            // Quick Ingress Action
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElyPurple.copy(alpha = 0.2f))
                        .border(0.5.dp, ElyPurple.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .clickable { onOpenIngressDialog("LYRIC") }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("+ Ingress", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyPurple)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Curatorial Navigation Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(ElyHeaderGlass)
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val sections = listOf("DRY RUN", "ALL", "WITNESS", "EXAMINATION", "FINDINGS", "HUMAN EAR REVIEW", "DISPOSITION")
            sections.forEach { sec ->
                val isSelected = currentTab == sec
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) ElyTileActive else Color.Transparent)
                        .clickable { onSelectTab(sec) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sec,
                        fontSize = 7.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) ElyPurple else ElyTextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Scrollable Examination Workspace
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. WITNESS SECTION
            if (currentTab == "ALL" || currentTab == "WITNESS") {
                SectionHeaderBadge("1. WITNESS", "IMMUTABLE SPECIMEN", ElyG3Axiom)
                WitnessSectionCard(selectedVersion)
            }

            // 2. EXAMINATION SECTION
            if (currentTab == "ALL" || currentTab == "EXAMINATION") {
                SectionHeaderBadge("2. EXAMINATION", "FORENSIC DIAGNOSTIC SCOPE", ElyPurple)
                ExaminationSectionView(selectedVersion)
            }

            // 3. FINDINGS SECTION
            if (currentTab == "ALL" || currentTab == "FINDINGS") {
                SectionHeaderBadge("3. FINDINGS", "EVIDENCE & ANCHOR SUMMARY", ElyCyan)
                FindingsSectionView(selectedVersion)
            }

            // 4. HUMAN EAR REVIEW (FORENSIC LISTENING LAYER)
            if (currentTab == "ALL" || currentTab == "HUMAN EAR REVIEW" || currentTab == "EAR REVIEW") {
                SectionHeaderBadge("4. HUMAN EAR REVIEW", "FORENSIC LISTENING LAYER & AUDITORY DEFECT SCREENING", ElyPurple)
                HumanEarReviewSectionView(
                    baseComposition = selectedBase,
                    selectedVersion = selectedVersion,
                    onCommitHumanEarDisposition = onCommitHumanEarDisposition,
                    onUpdateHumanEarReview = onUpdateHumanEarReview
                )
            }

            // 5. DISPOSITION SECTION
            if (currentTab == "ALL" || currentTab == "DISPOSITION") {
                SectionHeaderBadge("5. DISPOSITION", "HUMAN GOVERNOR PROTOCOL (3.2.1.0)", ElyAmberWarning)
                DispositionSectionView(
                    selectedVersion = selectedVersion,
                    onPreserve = onPreserve,
                    onAccept = onAccept,
                    onSendToEngine = onSendToEngine,
                    onCommitHumanGovernorDisposition = onCommitHumanGovernorDisposition
                )
            }

            // 5. AUDIT & TECHNICAL DETAILS SECTION
            if (currentTab == "ALL" || currentTab == "AUDIT & TECHNICAL") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElyHeaderGlass)
                        .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                        .clickable { onToggleTechnicalAudit() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIT & TECHNICAL DETAILS (G1–G6)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ElyPurple
                    )
                    Text(
                        text = if (isTechnicalAuditExpanded || currentTab == "AUDIT & TECHNICAL") "▲ Collapse" else "▼ Expand",
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextSecondary
                    )
                }

                if (isTechnicalAuditExpanded || currentTab == "AUDIT & TECHNICAL") {
                    AuditSectionView(
                        selectedVersion = selectedVersion,
                        selectedGateDiagnostic = selectedGateDiagnostic,
                        onSelectGate = onSelectGate
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==============================================================================
// 1. WITNESS SECTION VIEWS
// ==============================================================================
@Composable
private fun WitnessSectionCard(selectedVersion: SpecimenVersionNode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ElySurfaceCard)
            .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Witness Metrics Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(ElyHeaderGlass)
                .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Words: ${selectedVersion.wordCount}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyTextPrimary)
            Text(text = "Stanzas: ${selectedVersion.stanzaCount}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyTextPrimary)
            Text(text = "Anchors: ${selectedVersion.objectCount}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyG3Axiom)
            Text(text = "SHA: ${selectedVersion.sha256Hash.take(8)}...", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = ElyTextTertiary)
        }

        // Canonical Lyric Specimen Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(ElyHeaderGlass.copy(alpha = 0.5f))
                .border(0.5.dp, ElyPurple.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CANONICAL LYRIC SPECIMEN (IMMUTABLE TEXT WITNESS)",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ElyPurple
                )
                Text(
                    text = "LOCKED 🔒",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyG3Axiom
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Specimen Text Content (Clear, Large, Monospace)
            Text(
                text = selectedVersion.lyricText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = ElyTextPrimary,
                lineHeight = 17.sp
            )
        }

        // Witness Trustworthiness & Origin Stamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SHA-256: ${selectedVersion.sha256Hash}",
                fontSize = 7.5.sp,
                fontFamily = FontFamily.Monospace,
                color = ElyTextTertiary
            )
        }
    }
}

@Composable
private fun DesktopWitnessView(selectedVersion: SpecimenVersionNode) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left: Canonical Monospace Lyric Text
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .padding(end = 8.dp)
        ) {
            Text(
                text = "CANONICAL LYRIC SPECIMEN (IMMUTABLE TEXT WITNESS)",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ElyPurple
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(4.dp))
                    .background(ElyHeaderGlass.copy(alpha = 0.4f))
                    .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = selectedVersion.lyricText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElyTextPrimary,
                    lineHeight = 17.sp
                )
            }
        }

        // Right: Witness Integrity Sidebar
        Column(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxHeight()
                .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                .background(ElyHeaderGlass.copy(alpha = 0.5f))
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "WITNESS INTEGRITY",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ElyPurple
            )
            HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

            CuratorialMetric("Text Witness", "VERIFIED (Intact)", ElyG3Axiom)
            val audio = selectedVersion.audioWitness
            if (audio != null && audio.isMeasured) {
                CuratorialMetric("Audio Witness", "MEASURED (${audio.decoderStatus})", ElyG3Axiom)
            } else {
                CuratorialMetric("Audio Witness", "NOT MEASURED", ElyAmberWarning)
            }

            HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)
            Text(
                text = "SPECIMEN METRICS",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ElyTextSecondary
            )
            MetricRow("Words", "${selectedVersion.wordCount}")
            MetricRow("Sections", "${selectedVersion.sectionCount}")
            MetricRow("Stanzas", "${selectedVersion.stanzaCount}")
            MetricRow("Sensory Anchors", "${selectedVersion.objectCount}")

            HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)
            Text(
                text = "DETERMINISTIC SHA-256",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = ElyTextSecondary
            )
            Text(
                text = selectedVersion.sha256Hash,
                fontSize = 7.sp,
                fontFamily = FontFamily.Monospace,
                color = ElyG3Axiom
            )
        }
    }
}

// ==============================================================================
// 2. EXAMINATION SECTION VIEWS
// ==============================================================================
@Composable
private fun ExaminationSectionView(selectedVersion: SpecimenVersionNode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ElySurfaceCard)
            .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "FORENSIC EXAMINATION SCOPE & DOMAINS",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ElyPurple
        )
        Text(
            text = "Forensic scope establishes what evidence was physically examined vs. what remains unmeasured.",
            fontSize = 8.5.sp,
            color = ElyTextSecondary
        )
        HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

        // Domain 1: Text & Linguistic Witness Domain
        ExaminationDomainCard(
            domainName = "1. Text & Linguistic Witness Domain",
            statusLabel = "EXAMINED",
            statusColor = ElyG3Axiom,
            details = listOf(
                "Examined: Frozen 8-field evidence schema & semantic payload",
                "Examined: Physical sensory anchor presence (tactile grounding)",
                "Examined: Prohibited algorithmic cliché and trope screening",
                "Examined: Metric structure, stanzas (${selectedVersion.stanzaCount}), words (${selectedVersion.wordCount})"
            )
        )

        // Domain 2: Audio & Vocal Performance Domain
        val audio = selectedVersion.audioWitness
        if (audio != null && audio.isMeasured) {
            ExaminationDomainCard(
                domainName = "2. Audio & Vocal Performance Domain",
                statusLabel = "EXAMINED",
                statusColor = ElyG3Axiom,
                details = listOf(
                    "Examined: Decoder stream integrity (${audio.decoderStatus})",
                    "Examined: PCM transient structure (${audio.transientStatus})",
                    "Examined: Peak level (${audio.peakDb} dB) & sample rate (${audio.sampleRateKhz} kHz)"
                )
            )
        } else {
            ExaminationDomainCard(
                domainName = "2. Audio & Vocal Performance Domain",
                statusLabel = "NOT MEASURED",
                statusColor = ElyAmberWarning,
                details = listOf(
                    "Status: Physical audio/vocal evidence unavailable",
                    "Rule: Vocal naturalness, formant stability, tempo/pitch constraints are NOT MEASURED",
                    "Note: No synthetic or default values generated. Awaiting audio render."
                )
            )
        }

        // Domain 3: Acoustic Environment & Spatial Morphology Domain
        if (audio != null && audio.isMeasured) {
            ExaminationDomainCard(
                domainName = "3. Acoustic Environment & Spatial Morphology Domain",
                statusLabel = "EXAMINED / INFORMATIONAL",
                statusColor = ElyCyan,
                details = listOf(
                    "Status: Non-blocking acoustic observation recorded",
                    "Space: Class A Dry Space profile observed",
                    "Rule: Zero arbitrary numeric T60 blocking thresholds applied"
                )
            )
        } else {
            ExaminationDomainCard(
                domainName = "3. Acoustic Environment & Spatial Morphology Domain",
                statusLabel = "NOT MEASURED",
                statusColor = ElyTextTertiary,
                details = listOf(
                    "Status: Physical acoustic evidence unavailable",
                    "Rule: Reverb decay, T60, and acoustic morphology are NOT MEASURED",
                    "Note: Qualitative acoustic observations are not fabricated from lyric text."
                )
            )
        }

        // Domain 4: Human Governance Authority Domain
        ExaminationDomainCard(
            domainName = "4. Human Governance Authority Domain",
            statusLabel = "AWAITING HUMAN GOVERNOR",
            statusColor = ElyPurple,
            details = listOf(
                "Protocol: 3.2.1.0 (Listen → Evaluate → Decide → Freeze)",
                "AI Automation: Disabled (Zero algorithmic automated approvals)",
                "Authority: Final disposition reserved exclusively for Human Governor"
            )
        )
    }
}

// ==============================================================================
// 3. FINDINGS SECTION VIEWS
// ==============================================================================
@Composable
private fun FindingsSectionView(selectedVersion: SpecimenVersionNode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ElySurfaceCard)
            .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "FORENSIC FINDINGS & EVIDENCE SUMMARY",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ElyPurple
        )
        HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

        // Finding 1: Physical Sensory Anchors
        val diag = selectedVersion.g2Diagnostic
        val anchorCount = diag?.physicalAnchorCount ?: selectedVersion.objectCount
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(ElyHeaderGlass)
                .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1. PHYSICAL SENSORY ANCHORS",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyG3Axiom
                )
                Text(
                    text = "$anchorCount Anchors Found",
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElyG3Axiom
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val anchors = selectedVersion.evidence.witnessObjects.ifEmpty {
                listOf("coat", "coin", "table", "photograph", "railway")
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(anchors) { anchor ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(ElyG3Axiom.copy(alpha = 0.15f))
                            .border(0.5.dp, ElyG3Axiom.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(text = "⚓ $anchor", fontSize = 8.5.sp, color = ElyG3Axiom, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Finding 2: Trope & Cliché Screen
        val clicheCount = diag?.prohibitedLexiconCount ?: 0
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(ElyHeaderGlass)
                .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. PROHIBITED TROPE & CLICHÉ SCREEN",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (clicheCount == 0) ElyG3Axiom else ElyError
                )
                Text(
                    text = if (clicheCount == 0) "0 Clichés Detected (Clean)" else "$clicheCount Clichés Found",
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (clicheCount == 0) ElyG3Axiom else ElyError
                )
            }
        }

        // Finding 3: Frozen 8-Field Evidence Schema
        val evid = selectedVersion.evidence
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(ElyHeaderGlass)
                .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "3. CORE EVIDENCE SCHEMA (8-FIELD CONTRACT)",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ElyPurple
            )
            EvidenceField("Theme", evid.theme)
            EvidenceField("Narrative Arc", evid.narrativeArc)
            EvidenceField("Emotional Profile", evid.emotionalProfile)
            EvidenceField("Temporal Context", evid.temporalContext)
            EvidenceField("Energy Profile", evid.energyProfile)
            EvidenceField("Language Characteristics", evid.languageCharacteristics)
        }

        // Finding 4: Audio / Physical Evidence
        val audio = selectedVersion.audioWitness
        if (audio != null && audio.isMeasured) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(ElyHeaderGlass)
                    .border(0.5.dp, ElyG3Axiom.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "4. AUDIO WITNESS FINDINGS (MEASURED)",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyG3Axiom
                )
                AudioMetricRow("Decoder Health", audio.decoderStatus, ElyG3Axiom)
                AudioMetricRow("Duration", audio.durationFormatted, ElyTextPrimary)
                AudioMetricRow("Sample Rate", "${audio.sampleRateKhz} kHz", ElyTextPrimary)
                AudioMetricRow("Peak Level", "${audio.peakDb} dB", ElyG3Axiom)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(ElyHeaderGlass)
                    .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "4. AUDIO & VOCAL FINDINGS: NOT MEASURED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyAmberWarning
                )
                Text(
                    text = "No physical audio payload attached to this specimen. Vocal naturalness, formant stability, and acoustic room decay remain unmeasured pending audio render.",
                    fontSize = 8.5.sp,
                    color = ElyTextTertiary
                )
            }
        }
    }
}

// ==============================================================================
// 4B. DEDICATED FORENSIC LISTENING LAYER (HUMAN EAR REVIEW)
// ==============================================================================
@Composable
private fun HumanEarReviewSectionView(
    baseComposition: BaseComposition?,
    selectedVersion: SpecimenVersionNode?,
    onCommitHumanEarDisposition: (String, String, HumanEarDisposition, String) -> Unit,
    onUpdateHumanEarReview: (String, String, HumanEarReview) -> Unit
) {
    if (selectedVersion == null || baseComposition == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Select a verified specimen to initiate Human Ear Review", color = ElyTextSecondary, fontSize = 11.sp)
        }
        return
    }

    val currentReview = selectedVersion.humanEarReview ?: HumanEarReview()
    var awkwardWording by remember(selectedVersion.versionId, selectedVersion.humanEarReview) {
        mutableStateOf(selectedVersion.humanEarReview?.hasAwkwardWording ?: false)
    }
    var foreignInjection by remember(selectedVersion.versionId, selectedVersion.humanEarReview) {
        mutableStateOf(selectedVersion.humanEarReview?.hasForeignLanguageInjection ?: false)
    }
    var pronunciationAnomalies by remember(selectedVersion.versionId, selectedVersion.humanEarReview) {
        mutableStateOf(selectedVersion.humanEarReview?.hasPronunciationAnomalies ?: false)
    }
    var lyricMismatch by remember(selectedVersion.versionId, selectedVersion.humanEarReview) {
        mutableStateOf(selectedVersion.humanEarReview?.hasLyricAudioMismatch ?: false)
    }
    var unnaturalPhrasing by remember(selectedVersion.versionId, selectedVersion.humanEarReview) {
        mutableStateOf(selectedVersion.humanEarReview?.hasUnnaturalSungPhrasing ?: false)
    }
    var performanceAnomalies by remember(selectedVersion.versionId, selectedVersion.humanEarReview) {
        mutableStateOf(selectedVersion.humanEarReview?.hasPerformanceAnomaly ?: false)
    }
    var curatorNotes by remember(selectedVersion.versionId, selectedVersion.humanEarReview) {
        mutableStateOf(selectedVersion.humanEarReview?.curatorNotes ?: "")
    }

    val audioWitness = selectedVersion.audioWitness
    val isAudioMeasured = audioWitness != null && audioWitness.isMeasured

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. FORENSIC WORKFLOW BANNER (INGEST → VERIFY → MEASURE → HUMAN EAR REVIEW → DISPOSITION)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF141923))
                .border(1.dp, ElyPurple.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🎧 FORENSIC LISTENING LAYER",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ElyPurple
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(currentReview.disposition.badgeColor).copy(alpha = 0.2f))
                        .border(0.5.dp, Color(currentReview.disposition.badgeColor), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentReview.disposition.label,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(currentReview.disposition.badgeColor)
                    )
                }
            }

            Text(
                text = "Machine proves the artifact. The player lets the human hear the artifact. The curator decides.",
                fontSize = 9.sp,
                color = ElyTextSecondary
            )

            // Workflow Pipeline Track: INGEST → VERIFY → MEASURE → HUMAN EAR REVIEW → DISPOSITION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0D1117))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkflowStepChip("INGEST", true)
                Text("→", fontSize = 8.sp, color = ElyTextTertiary)
                WorkflowStepChip("VERIFY", true)
                Text("→", fontSize = 8.sp, color = ElyTextTertiary)
                WorkflowStepChip("MEASURE", isAudioMeasured)
                Text("→", fontSize = 8.sp, color = ElyTextTertiary)
                WorkflowStepChip("HUMAN EAR REVIEW", true, isCurrent = true)
                Text("→", fontSize = 8.sp, color = ElyTextTertiary)
                WorkflowStepChip("DISPOSITION", currentReview.disposition != HumanEarDisposition.PENDING_REVIEW)
            }

            // Invariant chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ForensicInvariantPill("SOURCE READ-ONLY", ElyG3Axiom)
                ForensicInvariantPill("SHA-256 IMMUTABLE", ElyG3Axiom)
                ForensicInvariantPill(if (isAudioMeasured) "PCM VERIFIED" else "AUDIO NOT_MEASURED", if (isAudioMeasured) ElyG3Axiom else ElyAmberWarning)
                ForensicInvariantPill("G1/G2 PRESERVED", ElyG3Axiom)
            }
        }

        // 2. THE WINAMP-STYLE FORENSIC WITNESS PLAYER DECK
        ForensicWitnessPlayerDeck(
            specimen = selectedVersion,
            songTitle = baseComposition.title,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. CURATOR AUDITORY DEFECT SCREENING CHECKLIST & LYRICS REFERENCE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Column: 6 Auditory Defect Screening Checklist
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ElyHeaderGlass)
                    .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "AUDITORY WITNESS SCREENING",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyTextPrimary
                )
                Text(
                    text = "Listen specifically for linguistic & musical delivery defects:",
                    fontSize = 8.sp,
                    color = ElyTextSecondary
                )

                AuditoryChecklistItem(
                    title = "Awkward / Unnatural wording",
                    subtitle = "Phrases that sound unnatural or grammatically distorted when sung",
                    isChecked = awkwardWording,
                    onToggle = {
                        val updated = !awkwardWording
                        awkwardWording = updated
                        onUpdateHumanEarReview(
                            baseComposition.id,
                            selectedVersion.versionId,
                            currentReview.copy(hasAwkwardWording = updated)
                        )
                    }
                )

                AuditoryChecklistItem(
                    title = "Unexpected foreign-language injection",
                    subtitle = "Unintentional foreign words or phonetic garble injected into vocal stream",
                    isChecked = foreignInjection,
                    onToggle = {
                        val updated = !foreignInjection
                        foreignInjection = updated
                        onUpdateHumanEarReview(
                            baseComposition.id,
                            selectedVersion.versionId,
                            currentReview.copy(hasForeignLanguageInjection = updated)
                        )
                    }
                )

                AuditoryChecklistItem(
                    title = "Pronunciation anomalies",
                    subtitle = "Mispronounced syllables, artificial vowels, or robotic cadence",
                    isChecked = pronunciationAnomalies,
                    onToggle = {
                        val updated = !pronunciationAnomalies
                        pronunciationAnomalies = updated
                        onUpdateHumanEarReview(
                            baseComposition.id,
                            selectedVersion.versionId,
                            currentReview.copy(hasPronunciationAnomalies = updated)
                        )
                    }
                )

                AuditoryChecklistItem(
                    title = "Lyric / Audio mismatch",
                    subtitle = "Vocalist sings words that diverge from canonical lyric text witness",
                    isChecked = lyricMismatch,
                    onToggle = {
                        val updated = !lyricMismatch
                        lyricMismatch = updated
                        onUpdateHumanEarReview(
                            baseComposition.id,
                            selectedVersion.versionId,
                            currentReview.copy(hasLyricAudioMismatch = updated)
                        )
                    }
                )

                AuditoryChecklistItem(
                    title = "Unnatural sung phrasing",
                    subtitle = "Awkward breath placement, rushed meter, or dissonant melody line",
                    isChecked = unnaturalPhrasing,
                    onToggle = {
                        val updated = !unnaturalPhrasing
                        unnaturalPhrasing = updated
                        onUpdateHumanEarReview(
                            baseComposition.id,
                            selectedVersion.versionId,
                            currentReview.copy(hasUnnaturalSungPhrasing = updated)
                        )
                    }
                )

                AuditoryChecklistItem(
                    title = "Performance defect despite gates",
                    subtitle = "Subjective human-ear unacceptability despite automated verification passing",
                    isChecked = performanceAnomalies,
                    onToggle = {
                        val updated = !performanceAnomalies
                        performanceAnomalies = updated
                        onUpdateHumanEarReview(
                            baseComposition.id,
                            selectedVersion.versionId,
                            currentReview.copy(hasPerformanceAnomaly = updated)
                        )
                    }
                )
            }

            // Right Column: Canonical Lyric Witness Reference
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ElyHeaderGlass)
                    .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CANONICAL LYRIC WITNESS",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ElyG3Axiom
                    )
                    Text(
                        text = "${selectedVersion.wordCount} words",
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextTertiary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0E121A))
                        .padding(6.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = selectedVersion.lyricText,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextPrimary,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // 4. CURATOR EAR OBSERVATIONS & QUALITATIVE NOTES
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(ElySurfaceCard)
                .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "CURATOR QUALITATIVE EAR OBSERVATIONS",
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ElyTextSecondary
            )
            OutlinedTextField(
                value = curatorNotes,
                onValueChange = {
                    curatorNotes = it
                    onUpdateHumanEarReview(
                        baseComposition.id,
                        selectedVersion.versionId,
                        currentReview.copy(curatorNotes = it)
                    )
                },
                placeholder = {
                    Text(
                        "Record auditory observations (e.g., phrasing naturalness, vocal clarity, timbre stability)...",
                        fontSize = 8.5.sp,
                        color = ElyTextTertiary
                    )
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElyPurple,
                    unfocusedBorderColor = ElyWindowBorderInactive,
                    focusedTextColor = ElyTextPrimary,
                    unfocusedTextColor = ElyTextPrimary
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
            )
        }

        // 5. HUMAN EAR DISPOSITION ASSIGNMENT ACTION BAR (KEEP / CURE / REJECT / FREEZE)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF161A22))
                .border(1.dp, ElyPurple.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ASSIGN HUMAN EAR DISPOSITION",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyTextPrimary
                )
                Text(
                    text = "Human Listening Witness Record",
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElyTextTertiary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. KEEP
                Button(
                    onClick = {
                        onCommitHumanEarDisposition(
                            baseComposition.id,
                            selectedVersion.versionId,
                            HumanEarDisposition.KEEP,
                            curatorNotes
                        )
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00783E)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("🟢 KEEP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // 2. CURE
                Button(
                    onClick = {
                        onCommitHumanEarDisposition(
                            baseComposition.id,
                            selectedVersion.versionId,
                            HumanEarDisposition.CURE,
                            curatorNotes
                        )
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF996600)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("🟡 CURE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // 3. REJECT
                Button(
                    onClick = {
                        onCommitHumanEarDisposition(
                            baseComposition.id,
                            selectedVersion.versionId,
                            HumanEarDisposition.REJECT,
                            curatorNotes
                        )
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("🔴 REJECT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // 4. FREEZE
                Button(
                    onClick = {
                        onCommitHumanEarDisposition(
                            baseComposition.id,
                            selectedVersion.versionId,
                            HumanEarDisposition.FREEZE,
                            curatorNotes
                        )
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005E7A)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("🔵 FREEZE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun WorkflowStepChip(label: String, isComplete: Boolean, isCurrent: Boolean = false) {
    val (bg, border, color) = when {
        isCurrent -> Triple(ElyPurple.copy(alpha = 0.25f), ElyPurple, ElyPurple)
        isComplete -> Triple(ElyG3Axiom.copy(alpha = 0.15f), ElyG3Axiom.copy(alpha = 0.6f), ElyG3Axiom)
        else -> Triple(Color(0xFF151820), ElyWindowBorderInactive, ElyTextTertiary)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun ForensicInvariantPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 6.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun AuditoryChecklistItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isChecked) ElyAmberWarning.copy(alpha = 0.12f) else Color(0xFF12151D))
            .border(0.5.dp, if (isChecked) ElyAmberWarning.copy(alpha = 0.6f) else ElyWindowBorderInactive, RoundedCornerShape(4.dp))
            .clickable { onToggle() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(16.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = ElyAmberWarning,
                checkmarkColor = Color.Black,
                uncheckedColor = ElyTextTertiary
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isChecked) ElyAmberWarning else ElyTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 7.sp,
                color = ElyTextTertiary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        if (isChecked) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(ElyAmberWarning.copy(alpha = 0.2f))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text("FLAGGED", fontSize = 6.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyAmberWarning)
            }
        }
    }
}

// ==============================================================================
// 4. DISPOSITION SECTION VIEWS
// ==============================================================================
@Composable
private fun DispositionSectionView(
    selectedVersion: SpecimenVersionNode,
    onPreserve: () -> Unit,
    onAccept: () -> Unit,
    onSendToEngine: () -> Unit,
    onCommitHumanGovernorDisposition: (GovernanceDispositionChoice, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ElySurfaceCard)
            .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CURATORIAL DISPOSITION & GOVERNOR ACTIONS",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = ElyPurple
        )
        HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

        // Current Specimen Decision Summary Card
        val (decIcon, decColor, decText) = when (selectedVersion.decision) {
            SpecimenDecision.ACCEPT -> Triple("🟢", ElyG3Axiom, "ACCEPT (SURVIVOR VAULT)")
            SpecimenDecision.NEEDS_HEALING -> Triple("🟡", ElyAmberWarning, "NEEDS HEALING (ENGINE)")
            SpecimenDecision.NOT_ELIGIBLE -> Triple("🔴", ElyError, "NOT ELIGIBLE (PERMANENT REJECT)")
            SpecimenDecision.NOT_YET_EXAMINED -> Triple("⚪", ElyTextTertiary, "AWAITING HUMAN GOVERNOR REVIEW")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(decColor.copy(alpha = 0.08f))
                .border(0.5.dp, decColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = decIcon, fontSize = 12.sp)
                Text(
                    text = decText,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = decColor
                )
            }
            Text(
                text = selectedVersion.decisionReason,
                fontSize = 9.5.sp,
                color = ElyTextPrimary,
                modifier = Modifier.padding(top = 3.dp)
            )
        }

        // Human Governor Protocol (3.2.1.0) Action Console
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(ElyHeaderGlass)
                .border(0.5.dp, ElyPurple.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "HUMAN GOVERNOR PROTOCOL (3.2.1.0: Listen → Evaluate → Decide → Freeze):",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ElyAmberWarning
            )
            Text(
                text = "Select an authoritative human curatorial disposition pathway for this specimen:",
                fontSize = 8.5.sp,
                color = ElyTextSecondary
            )

            // Responsive 2x2 Grid or Row of Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { onCommitHumanGovernorDisposition(GovernanceDispositionChoice.RELEASE_ACCEPT, "Human Governor authorized release to Survivor Vault.") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElyG3Axiom),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp).weight(1f)
                ) {
                    Text("🟢 Accept", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Button(
                    onClick = { onCommitHumanGovernorDisposition(GovernanceDispositionChoice.MINOR_CURE, "Human Governor authorized localized minor cure.") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElyAmberWarning),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp).weight(1f)
                ) {
                    Text("🟡 Minor Cure", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Button(
                    onClick = { onCommitHumanGovernorDisposition(GovernanceDispositionChoice.FULL_RECONSTRUCTION, "Human Governor authorized full architectural reconstruction.") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElyPurple),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp).weight(1f)
                ) {
                    Text("🟠 Rebuild", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = { onCommitHumanGovernorDisposition(GovernanceDispositionChoice.PERMANENT_REJECT, "Human Governor committed permanent reject; witness sealed.") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElyError),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp).weight(1f)
                ) {
                    Text("🔴 Reject", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Secondary Curatorial Pipeline Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onPreserve,
                colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp).weight(1f)
            ) {
                Text("🔒 Preserve Witness", fontSize = 9.sp, color = ElyTextPrimary)
            }

            if (selectedVersion.decision == SpecimenDecision.ACCEPT) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = ElyG3Axiom),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).weight(1f)
                ) {
                    Text("Accept to Vault", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            if (selectedVersion.decision == SpecimenDecision.NEEDS_HEALING) {
                Button(
                    onClick = onSendToEngine,
                    colors = ButtonDefaults.buttonColors(containerColor = ElyAmberWarning),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).weight(1f)
                ) {
                    Text("Send to Engine", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

// ==============================================================================
// 5. AUDIT & TECHNICAL DETAILS SECTION VIEWS
// ==============================================================================
@Composable
private fun AuditSectionView(
    selectedVersion: SpecimenVersionNode,
    selectedGateDiagnostic: GateDiagnostic?,
    onSelectGate: (GateDiagnostic?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ElySurfaceCard)
            .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "INTERNAL GOVERNANCE GATE RECORDS (G1–G6)",
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = ElyTextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            selectedVersion.gates.forEach { gate ->
                val isGateSelected = selectedGateDiagnostic?.gateId == gate.gateId
                val (gateBg, gateBorder, gateColor) = when (gate.status) {
                    GateStatus.PASS -> Triple(ElyG3Axiom.copy(alpha = 0.15f), ElyG3Axiom, ElyG3Axiom)
                    GateStatus.FLAGGED -> Triple(ElyAmberWarning.copy(alpha = 0.15f), ElyAmberWarning, ElyAmberWarning)
                    GateStatus.CURE_RECOMMENDED -> Triple(ElyAmberWarning.copy(alpha = 0.2f), ElyAmberWarning, ElyAmberWarning)
                    GateStatus.FAIL -> Triple(ElyError.copy(alpha = 0.15f), ElyError, ElyError)
                    GateStatus.UNEXAMINED -> Triple(ElySurfaceCard, ElyWindowBorderInactive, ElyTextTertiary)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isGateSelected) gateBorder.copy(alpha = 0.3f) else gateBg)
                        .border(0.5.dp, if (isGateSelected) ElyPurple else gateBorder, RoundedCornerShape(4.dp))
                        .clickable { onSelectGate(if (isGateSelected) null else gate) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = gate.gateId,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = gateColor
                    )
                    Text(
                        text = if (gate.status == GateStatus.UNEXAMINED) "NOT MEASURED" else gate.status.name,
                        fontSize = 6.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (gate.status == GateStatus.UNEXAMINED) ElyTextTertiary else ElyTextPrimary,
                        maxLines = 1
                    )
                }
            }
        }

        // Selected Gate Diagnostic Drawer
        AnimatedVisibility(visible = selectedGateDiagnostic != null) {
            if (selectedGateDiagnostic != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElyHeaderGlass)
                        .border(0.5.dp, ElyPurple.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GATE: ${selectedGateDiagnostic.gateId} (${selectedGateDiagnostic.name})",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyPurple
                        )
                        Text(
                            text = "Close ✕",
                            fontSize = 8.5.sp,
                            color = ElyTextSecondary,
                            modifier = Modifier.clickable { onSelectGate(null) }
                        )
                    }
                    Text(
                        text = selectedGateDiagnostic.summary,
                        fontSize = 9.sp,
                        color = ElyTextPrimary,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                    selectedGateDiagnostic.detailedEvidence.forEach { line ->
                        Text(
                            text = "• $line",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyTextSecondary
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

        // Provenance & Audit History
        Text(
            text = "PROVENANCE & AUDIT LOGS",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = ElyTextSecondary
        )

        if (selectedVersion.historyTrail.isEmpty()) {
            Text("No audit history entries recorded.", fontSize = 9.sp, color = ElyTextTertiary)
        } else {
            selectedVersion.historyTrail.forEach { entry ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElyHeaderGlass)
                        .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = entry.action, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyPurple)
                        Text(text = entry.timestamp, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace, color = ElyTextTertiary)
                    }
                    Text(text = entry.detail, fontSize = 8.5.sp, color = ElyTextPrimary)
                    Text(text = "Origin: ${entry.sourceOrigin}", fontSize = 7.5.sp, color = ElyTextSecondary)
                }
            }
        }
    }
}

// ==============================================================================
// SHARED HELPER COMPOSABLES
// ==============================================================================
@Composable
private fun SectionHeaderBadge(sectionNumber: String, sectionName: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = sectionNumber,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = accentColor
        )
        Text(
            text = "// $sectionName",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = ElyTextPrimary
        )
    }
}

@Composable
private fun IngressButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ElyHeaderGlass)
            .border(0.5.dp, ElyPurple.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyPurple)
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 8.5.sp, color = ElyTextSecondary)
        Text(text = value, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyTextPrimary)
    }
}

@Composable
private fun CuratorialMetric(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 8.5.sp, color = ElyTextSecondary)
        Text(text = value, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun AudioMetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 8.5.sp, color = ElyTextSecondary)
        Text(text = value, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun EvidenceField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = label.uppercase(), fontSize = 7.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ElyPurple)
        Text(text = value, fontSize = 9.sp, color = ElyTextPrimary)
    }
}

@Composable
private fun ExaminationDomainCard(
    domainName: String,
    statusLabel: String,
    statusColor: Color,
    details: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(ElyHeaderGlass)
            .border(0.5.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = domainName,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ElyTextPrimary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(0.5.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
        details.forEach { detail ->
            Text(
                text = "• $detail",
                fontSize = 8.sp,
                color = ElyTextSecondary,
                lineHeight = 12.sp
            )
        }
    }
}
