package com.example.ui.tenants

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*

/**
 * 🛋️ CORPUS / LYRIC CURATOR (The Sitting Room)
 * Elyzareth OS — App 02
 *
 * PHILOSOPHY:
 * SYSTEM EXAMINES → SYSTEM RECOMMENDS → CURATOR DECIDES
 *
 * Primary UI Focus:
 * 1. The System's Recommendation (🟢 ACCEPTABLE / 🟡 REQUIRES CURATION / 🔴 REJECT)
 * 2. The Full Lyric (The dominant specimen)
 * 3. The Appropriate Action (Accept, Curate in App 01, Reject, Copy, Archive)
 *
 * Additional Capabilities:
 * - Collapsible Forensic Details (G1–G6 audit kept secondary to avoid spreadsheet noise)
 * - Lyric Dumping / Accepted Archive (with Copy to Suno/Diary & Delete with confirmation)
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
    onStartSafScan: (Context, Uri) -> Unit = { _, _ -> },
    archiveFiles: List<ArchiveFile> = emptyList(),
    onDeleteArchiveFile: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Active View Mode: "EXAMINATION" or "LYRIC_DUMPING"
    var activeViewMode by remember { mutableStateOf("EXAMINATION") }
    var isSpecimenPickerOpen by remember { mutableStateOf(false) }
    var isForensicDetailsExpanded by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<ArchiveFile?>(null) }

    // SAF Document & Folder pickers
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

    val selectedBase = baseCompositions.find { it.id == selectedBaseCompositionId }
        ?: baseCompositions.firstOrNull()
    val selectedVersion = selectedBase?.versions?.find { it.versionId == selectedVersionId }
        ?: selectedBase?.versions?.firstOrNull()

    // Filter accepted lyrics for the Lyric Dumping view
    val acceptedLyrics = remember(archiveFiles) {
        archiveFiles.filter { it.category == "LYRICS" || it.originTenant.contains("Curator", ignoreCase = true) || it.fileName.endsWith(".lyr") }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ElyBackground)
    ) {
        // =========================================================================
        // TOP CONTROL & NAVIGATION BAR
        // =========================================================================
        Surface(
            color = ElyHeaderGlass,
            border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Workspace Header & Specimen Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🛋️", fontSize = 18.sp)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SITTING ROOM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ElyPurple
                            )
                            Text(
                                text = "• CORPUS CURATOR",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElyTextSecondary
                            )
                        }

                        if (selectedBase != null && selectedVersion != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { isSpecimenPickerOpen = true }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = selectedBase.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElyTextPrimary,
                                    maxLines = 1
                                )
                                Surface(
                                    color = ElyPurple.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = selectedVersion.versionId,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = ElyPurple,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text("▾", fontSize = 11.sp, color = ElyPurple)
                            }
                        }
                    }
                }

                // Mode Tabs & Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // View Mode Switcher
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElySurfaceCard)
                            .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(6.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (activeViewMode == "EXAMINATION") ElyPurple else Color.Transparent,
                            modifier = Modifier.clickable { activeViewMode = "EXAMINATION" }
                        ) {
                            Text(
                                text = "🔬 Examination",
                                fontSize = 9.5.sp,
                                fontWeight = if (activeViewMode == "EXAMINATION") FontWeight.Bold else FontWeight.Normal,
                                color = if (activeViewMode == "EXAMINATION") Color.White else ElyTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (activeViewMode == "LYRIC_DUMPING") ElyPurple else Color.Transparent,
                            modifier = Modifier.clickable { activeViewMode = "LYRIC_DUMPING" }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "📦 Lyric Dumping",
                                    fontSize = 9.5.sp,
                                    fontWeight = if (activeViewMode == "LYRIC_DUMPING") FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeViewMode == "LYRIC_DUMPING") Color.White else ElyTextSecondary
                                )
                                if (acceptedLyrics.isNotEmpty()) {
                                    Surface(
                                        color = if (activeViewMode == "LYRIC_DUMPING") Color.White.copy(alpha = 0.25f) else ElyPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${acceptedLyrics.size}",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (activeViewMode == "LYRIC_DUMPING") Color.White else ElyPurple,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Ingress Specimen Button
                    Button(
                        onClick = { onOpenIngressDialog("LYRIC") },
                        colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                        border = BorderStroke(0.5.dp, ElyPurple.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "+ Ingress",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyPurple
                        )
                    }
                }
            }
        }

        // =========================================================================
        // BODY AREA (EXAMINATION WORKSPACE OR LYRIC DUMPING ARCHIVE)
        // =========================================================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            if (activeViewMode == "LYRIC_DUMPING") {
                // =====================================================================
                // LYRIC DUMPING // ACCEPTED ARCHIVE VIEW
                // =====================================================================
                LyricDumpingArchiveView(
                    acceptedLyrics = acceptedLyrics,
                    onCopyLyric = { lyricText, fileName ->
                        clipboardManager.setText(AnnotatedString(lyricText))
                        Toast.makeText(context, "Copied '$fileName' to clipboard for Diary/Suno", Toast.LENGTH_SHORT).show()
                    },
                    onRequestDelete = { file ->
                        fileToDelete = file
                    },
                    onSwitchToExamination = {
                        activeViewMode = "EXAMINATION"
                    }
                )
            } else {
                // =====================================================================
                // SPECIMEN EXAMINATION VIEW (System Examines -> Recommends -> Curator Decides)
                // =====================================================================
                if (selectedBase == null || selectedVersion == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No Specimen Ingressed in Sitting Room", fontSize = 14.sp, color = ElyTextSecondary)
                            Button(
                                onClick = { onOpenIngressDialog("LYRIC") },
                                colors = ButtonDefaults.buttonColors(containerColor = ElyPurple)
                            ) {
                                Text("+ Ingress Specimen", color = Color.White)
                            }
                        }
                    }
                } else {
                    SpecimenExaminationWorkspace(
                        base = selectedBase,
                        version = selectedVersion,
                        allBases = baseCompositions,
                        isForensicDetailsExpanded = isForensicDetailsExpanded,
                        onToggleForensicDetails = { isForensicDetailsExpanded = !isForensicDetailsExpanded },
                        onSelectBaseComposition = onSelectBaseComposition,
                        onSelectVersion = onSelectVersion,
                        onAccept = onAccept,
                        onCurateInEngine = onSendToEngine,
                        onReject = {
                            onCommitHumanGovernorDisposition(
                                GovernanceDispositionChoice.PERMANENT_REJECT,
                                "Rejected by Curator in Sitting Room"
                            )
                        },
                        onCopyLyric = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Lyric copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // =========================================================================
    // SPECIMEN PICKER DIALOG (Fast switcher across specimens)
    // =========================================================================
    if (isSpecimenPickerOpen) {
        Dialog(onDismissRequest = { isSpecimenPickerOpen = false }) {
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
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT SPECIMEN FOR EXAMINATION",
                            fontSize = 11.sp,
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
                                .clickable { isSpecimenPickerOpen = false }
                        )
                    }

                    HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(baseCompositions) { base ->
                            val isBaseSelected = base.id == selectedBase?.id
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isBaseSelected) ElyTileActive else ElyHeaderGlass)
                                    .border(
                                        0.5.dp,
                                        if (isBaseSelected) ElyPurple else ElyWindowBorderInactive,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = base.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElyTextPrimary
                                )
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
                                                isSpecimenPickerOpen = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• Version ${ver.versionId} (${ver.specimenId})",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isVerSelected) ElyPurple else ElyTextSecondary
                                        )
                                        val (statusLabel, statusColor) = when (ver.decision) {
                                            SpecimenDecision.ACCEPT -> "🟢 ACCEPTABLE" to ElyG3Axiom
                                            SpecimenDecision.NEEDS_HEALING -> "🟡 CURATION NEEDED" to ElyAmberWarning
                                            SpecimenDecision.NOT_ELIGIBLE -> "🔴 REJECTED" to ElyError
                                            SpecimenDecision.NOT_YET_EXAMINED -> "⚪ UNEXAMINED" to ElyTextSecondary
                                        }
                                        Text(
                                            text = statusLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = statusColor
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

    // =========================================================================
    // DELETE CONFIRMATION DIALOG (For Lyric Dumping Archive)
    // =========================================================================
    fileToDelete?.let { targetFile ->
        Dialog(onDismissRequest = { fileToDelete = null }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ElyBackground,
                border = BorderStroke(1.dp, ElyError),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🗑️", fontSize = 18.sp)
                        Text(
                            text = "DELETE ARCHIVED LYRIC?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyError
                        )
                    }

                    Text(
                        text = "Are you sure you want to permanently delete '${targetFile.fileName}' from the Lyric Dumping archive?\n\nAccepted lyrics are typically copied onward to Suno or your Diary, so removing them here safely clears local OS storage.",
                        fontSize = 11.sp,
                        color = ElyTextSecondary,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { fileToDelete = null }
                        ) {
                            Text("Cancel", color = ElyTextSecondary, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onDeleteArchiveFile(targetFile.id)
                                fileToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElyError),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Delete Permanently", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // INGRESS SPECIMEN DIALOG
    // =========================================================================
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
                            text = "FORENSIC SPECIMEN INGRESS",
                            fontSize = 11.sp,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Origin
                    Text("Source Origin:", fontSize = 9.5.sp, color = ElyTextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(IngressSourceOrigin.entries) { origin ->
                            val isSel = origin == ingressSourceOrigin
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) ElyPurple else ElyHeaderGlass,
                                border = BorderStroke(0.5.dp, if (isSel) ElyPurple else ElyWindowBorderInactive),
                                modifier = Modifier.clickable { onIngressSourceOriginChange(origin) }
                            ) {
                                Text(
                                    text = origin.name.replace('_', ' '),
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSel) Color.White else ElyTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
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
                                Text("Select local folder tree or artifact directly via SAF", fontSize = 8.sp, color = ElyTextSecondary)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { safFolderLauncher.launch(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElyPurple),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("📂 Folder", fontSize = 8.sp, color = Color.White)
                                }
                                Button(
                                    onClick = { safDocumentLauncher.launch(arrayOf("*/*")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("📄 File", fontSize = 8.sp, color = ElyTextPrimary)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
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

                    // Commit Ingress Button
                    Button(
                        onClick = onCommitIngress,
                        colors = ButtonDefaults.buttonColors(containerColor = ElyPurple),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text("COMMIT TO FORENSIC INGRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SPECIMEN EXAMINATION WORKSPACE
// Core philosophy: SYSTEM EXAMINES → SYSTEM RECOMMENDS → CURATOR DECIDES
// =============================================================================
@Composable
private fun SpecimenExaminationWorkspace(
    base: BaseComposition,
    version: SpecimenVersion,
    allBases: List<BaseComposition>,
    isForensicDetailsExpanded: Boolean,
    onToggleForensicDetails: () -> Unit,
    onSelectBaseComposition: (String) -> Unit,
    onSelectVersion: (String) -> Unit,
    onAccept: () -> Unit,
    onCurateInEngine: () -> Unit,
    onReject: () -> Unit,
    onCopyLyric: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ---------------------------------------------------------------------
        // 1. SYSTEM'S RECOMMENDATION (Top Prominent Banner)
        // ---------------------------------------------------------------------
        SystemRecommendationBanner(
            version = version,
            onCurateInEngine = onCurateInEngine
        )

        // ---------------------------------------------------------------------
        // 2. THE FULL LYRIC (The Visually Dominant Specimen)
        // ---------------------------------------------------------------------
        DominantLyricSpecimenCard(
            base = base,
            version = version,
            onCopyLyric = onCopyLyric
        )

        // ---------------------------------------------------------------------
        // 3. CURATOR DECIDES (Appropriate Primary Actions)
        // ---------------------------------------------------------------------
        CuratorDecisionBar(
            decision = version.decision,
            lyricText = version.lyricText,
            onAccept = onAccept,
            onCurateInEngine = onCurateInEngine,
            onReject = onReject,
            onCopyLyric = onCopyLyric
        )

        // ---------------------------------------------------------------------
        // 4. FORENSIC DETAILS & TECHNICAL AUDIT (Secondary / Collapsed Area)
        // ---------------------------------------------------------------------
        ForensicTechnicalAuditCollapsible(
            version = version,
            isExpanded = isForensicDetailsExpanded,
            onToggle = onToggleForensicDetails
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// =============================================================================
// 1. SYSTEM RECOMMENDATION BANNER
// =============================================================================
@Composable
private fun SystemRecommendationBanner(
    version: SpecimenVersion,
    onCurateInEngine: () -> Unit
) {
    val (recommendationTitle, bannerBg, bannerBorder, bannerIcon, subtitle) = when (version.decision) {
        SpecimenDecision.ACCEPT -> {
            Tuple5(
                "🟢 SYSTEM RECOMMENDATION: ACCEPTABLE",
                ElyG3Axiom.copy(alpha = 0.12f),
                ElyG3Axiom.copy(alpha = 0.8f),
                "✓",
                "Specimen exhibits complete sovereign integrity across lyric meter and tactile witness anchors. Ready for canonical archive and export to Diary/Suno."
            )
        }
        SpecimenDecision.NEEDS_HEALING -> {
            Tuple5(
                "🟡 SYSTEM RECOMMENDATION: REQUIRES CURATION",
                ElyAmberWarning.copy(alpha = 0.12f),
                ElyAmberWarning.copy(alpha = 0.8f),
                "⚡",
                "Identity & tactile witness anchors retained, but structural meter instability or chorus collapse detected. Permitted for automatic transfer to App 01 Lyric Generator."
            )
        }
        SpecimenDecision.NOT_ELIGIBLE -> {
            Tuple5(
                "🔴 SYSTEM RECOMMENDATION: REJECT",
                ElyError.copy(alpha = 0.12f),
                ElyError.copy(alpha = 0.8f),
                "✕",
                "Total semantic or audio degradation. Zero recoverable identity or witness anchors. Rebuild prohibited by Governance protocol."
            )
        }
        SpecimenDecision.NOT_YET_EXAMINED -> {
            Tuple5(
                "⚪ SYSTEM RECOMMENDATION: UNEXAMINED",
                ElyHeaderGlass,
                ElyWindowBorderInactive,
                "?",
                "Specimen has not completed forensic examination."
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bannerBg,
        border = BorderStroke(1.dp, bannerBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = bannerIcon,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = bannerBorder
                )
                Text(
                    text = recommendationTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = bannerBorder
                )
            }

            Text(
                text = version.decisionReason.ifBlank { subtitle },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ElyTextPrimary,
                lineHeight = 16.sp
            )

            if (version.decision == SpecimenDecision.NEEDS_HEALING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElyAmberWarning.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "💡 Clicking 'Curate in App 01' below will auto-fill the lyric and brief into the Lyric Generator.",
                        fontSize = 10.sp,
                        color = ElyAmberWarning
                    )
                }
            }
        }
    }
}

// =============================================================================
// 2. DOMINANT LYRIC SPECIMEN CARD
// =============================================================================
@Composable
private fun DominantLyricSpecimenCard(
    base: BaseComposition,
    version: SpecimenVersion,
    onCopyLyric: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ElySurfaceCard,
        border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SPECIMEN LYRIC // ${base.title}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ElyPurple
                    )
                    Text(
                        text = "${version.wordCount} words • ${version.stanzaCount} stanzas • Source: ${version.sourceOrigin.name.replace('_', ' ')}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextSecondary
                    )
                }

                Button(
                    onClick = { onCopyLyric(version.lyricText) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                    border = BorderStroke(0.5.dp, ElyPurple.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("📋", fontSize = 10.sp)
                        Text(
                            text = "Copy Lyric",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyTextPrimary
                        )
                    }
                }
            }

            HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

            // Lyric Specimen Content (Dominant Monospace / Poetic Typography)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ElyBackground,
                border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
                modifier = Modifier.fillMaxWidth()
            ) {
                SelectionContainer {
                    Text(
                        text = version.lyricText,
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextPrimary,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }
            }

            // Optional Audio Witness summary strip
            version.audioWitness?.let { audio ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElyHeaderGlass)
                        .border(0.5.dp, ElyWindowBorderInactive, RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🎵", fontSize = 12.sp)
                        Text(
                            text = "Audio Witness: ${audio.durationFormatted}",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = ElyTextPrimary
                        )
                        Text("•", fontSize = 9.5.sp, color = ElyTextTertiary)
                        Text(
                            text = if (audio.isMeasured) "PCM ${audio.sampleRateKhz}kHz" else "NOT MEASURED",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (audio.isMeasured) ElyG3Axiom else ElyAmberWarning
                        )
                    }
                    Text(
                        text = "Peak: ${audio.peakDb} dB",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextSecondary
                    )
                }
            }
        }
    }
}

// =============================================================================
// 3. CURATOR DECIDES ACTION BAR
// =============================================================================
@Composable
private fun CuratorDecisionBar(
    decision: SpecimenDecision,
    lyricText: String,
    onAccept: () -> Unit,
    onCurateInEngine: () -> Unit,
    onReject: () -> Unit,
    onCopyLyric: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ElySurfaceCard,
        border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CURATOR ACTION",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ElyTextSecondary
            )

            when (decision) {
                SpecimenDecision.ACCEPT -> {
                    // 🟢 ACCEPTABLE WORKFLOW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Highlighted ACCEPT Button
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = ElyG3Axiom),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🟢", fontSize = 14.sp)
                                Text(
                                    text = "ACCEPT & ARCHIVE",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black
                                )
                            }
                        }

                        // Copy Lyric Button
                        Button(
                            onClick = { onCopyLyric(lyricText) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                            border = BorderStroke(0.5.dp, ElyPurple.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = "📋 Copy Lyric",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElyTextPrimary
                            )
                        }
                    }
                }

                SpecimenDecision.NEEDS_HEALING -> {
                    // 🟡 REQUIRES CURATION WORKFLOW
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Highlighted CURATE Button (Opens App 01 with lyric and brief automatically transferred)
                        Button(
                            onClick = onCurateInEngine,
                            colors = ButtonDefaults.buttonColors(containerColor = ElyAmberWarning),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✨", fontSize = 14.sp)
                                Text(
                                    text = "CURATE IN APP 01 (LYRIC GENERATOR)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Secondary action row (Manual copy fallback / Force Accept / Reject)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onCopyLyric(lyricText) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                                border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Text("📋 Copy Lyric", fontSize = 9.5.sp, color = ElyTextPrimary)
                            }

                            Button(
                                onClick = onAccept,
                                colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                                border = BorderStroke(0.5.dp, ElyG3Axiom.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Text("Accept As-Is", fontSize = 9.5.sp, color = ElyG3Axiom)
                            }

                            Button(
                                onClick = onReject,
                                colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                                border = BorderStroke(0.5.dp, ElyError.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(34.dp)
                            ) {
                                Text("Reject", fontSize = 9.5.sp, color = ElyError)
                            }
                        }
                    }
                }

                SpecimenDecision.NOT_ELIGIBLE -> {
                    // 🔴 REJECT WORKFLOW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(containerColor = ElyError),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🔴", fontSize = 14.sp)
                                Text(
                                    text = "COMMIT REJECTION",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { onCopyLyric(lyricText) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                            border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("📋 Copy Raw Text", fontSize = 10.sp, color = ElyTextPrimary)
                        }
                    }
                }

                SpecimenDecision.NOT_YET_EXAMINED -> {
                    Button(
                        onClick = onCurateInEngine,
                        colors = ButtonDefaults.buttonColors(containerColor = ElyPurple),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Examine Specimen", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// 4. FORENSIC DETAILS & TECHNICAL AUDIT (Secondary Collapsible Area)
// =============================================================================
@Composable
private fun ForensicTechnicalAuditCollapsible(
    version: SpecimenVersion,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ElyHeaderGlass,
        border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔍", fontSize = 12.sp)
                    Text(
                        text = "Forensic Details & Technical Audit (G1–G6)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ElyPurple
                    )
                }
                Text(
                    text = if (isExpanded) "▲ Collapse" else "▼ Expand Details",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElyTextSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = ElyWindowBorderInactive, thickness = 0.5.dp)

                    // Witness Objects & Theme
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ElyBackground,
                        border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "THEME: ${version.evidence.theme}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElyTextPrimary
                            )
                            if (version.evidence.witnessObjects.isNotEmpty()) {
                                Text(
                                    text = "Witness Anchors: ${version.evidence.witnessObjects.joinToString(", ")}",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ElyG3Axiom
                                )
                            }
                            Text(
                                text = "Narrative: ${version.evidence.narrativeArc}",
                                fontSize = 8.5.sp,
                                color = ElyTextSecondary
                            )
                        }
                    }

                    // G1–G6 Diagnostic Gates
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        version.gates.forEach { gate ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElyBackground)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${gate.gateId} [${gate.name}]",
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = ElyPurple
                                    )
                                    Text(
                                        text = gate.summary,
                                        fontSize = 8.sp,
                                        color = ElyTextSecondary,
                                        maxLines = 1
                                    )
                                }
                                val (gateBadge, gateColor) = when (gate.status) {
                                    GateStatus.PASS -> "PASS" to ElyG3Axiom
                                    GateStatus.FLAGGED -> "FLAGGED" to ElyAmberWarning
                                    GateStatus.CURE_RECOMMENDED -> "CURE" to ElyAmberWarning
                                    GateStatus.FAIL -> "FAIL" to ElyError
                                    GateStatus.UNEXAMINED -> "UNEXAMINED" to ElyTextTertiary
                                }
                                Text(
                                    text = gateBadge,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = gateColor
                                )
                            }
                        }
                    }

                    // SHA-256 Hash
                    Text(
                        text = "EVIDENCE HASH: ${version.sha256Hash}",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextTertiary
                    )
                }
            }
        }
    }
}

// =============================================================================
// 5. LYRIC DUMPING ARCHIVE VIEW (Accepted Canonical Lyrics)
// =============================================================================
@Composable
private fun LyricDumpingArchiveView(
    acceptedLyrics: List<ArchiveFile>,
    onCopyLyric: (String, String) -> Unit,
    onRequestDelete: (ArchiveFile) -> Unit,
    onSwitchToExamination: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = ElySurfaceCard,
            border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
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
                        Text("📦", fontSize = 16.sp)
                        Text(
                            text = "LYRIC DUMPING // ACCEPTED ARCHIVE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyG3Axiom
                        )
                    }

                    Surface(
                        color = ElyG3Axiom.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${acceptedLyrics.size} ACCEPTED",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyG3Axiom,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Archive of specimens accepted by Human Governor. Copy onward to Suno or your Diary, and delete when finished.",
                    fontSize = 10.sp,
                    color = ElyTextSecondary
                )
            }
        }

        if (acceptedLyrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No Accepted Lyrics in Archive",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElyTextSecondary
                    )
                    Text(
                        text = "Examine and accept specimens in the Examination tab to populate this dumping archive.",
                        fontSize = 10.5.sp,
                        color = ElyTextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onSwitchToExamination,
                        colors = ButtonDefaults.buttonColors(containerColor = ElyPurple),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Go to Specimen Examination", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(acceptedLyrics, key = { it.id }) { file ->
                    ArchivedLyricItemCard(
                        file = file,
                        onCopy = { onCopyLyric(file.fullText, file.fileName) },
                        onDelete = { onRequestDelete(file) }
                    )
                }
            }
        }
    }
}

// =============================================================================
// ARCHIVED LYRIC ITEM CARD (With Copy for Suno/Diary & Delete with confirmation)
// =============================================================================
@Composable
private fun ArchivedLyricItemCard(
    file: ArchiveFile,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = ElySurfaceCard,
        border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📄", fontSize = 14.sp)
                    Column {
                        Text(
                            text = file.fileName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElyTextPrimary
                        )
                        Text(
                            text = "${file.originTenant} • ${file.sizeKb} KB",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyTextSecondary
                        )
                    }
                }

                // Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Copy to Clipboard (For Suno / Diary)
                    Button(
                        onClick = onCopy,
                        colors = ButtonDefaults.buttonColors(containerColor = ElyG3Axiom),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "📋 Copy Lyric",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                    }

                    // Delete Button
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = ElyHeaderGlass),
                        border = BorderStroke(0.5.dp, ElyError.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "🗑️ Delete",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyError
                        )
                    }
                }
            }

            // Lyric Preview / Full Lyric Text
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = ElyBackground,
                border = BorderStroke(0.5.dp, ElyWindowBorderInactive),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = if (isExpanded) file.fullText else file.previewText.ifBlank { file.fullText.take(150) + "..." },
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElyTextPrimary,
                            lineHeight = 16.sp
                        )
                    }

                    Text(
                        text = if (isExpanded) "▲ Click to collapse" else "▼ Click to read full lyric",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyPurple
                    )
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
