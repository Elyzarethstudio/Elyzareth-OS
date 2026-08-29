package com.example.ui.tenants

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CorpusDiscoveryReportView(
    report: CorpusInventoryReport,
    onStartSafScan: (Context, Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val safFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            onStartSafScan(context, uri)
        }
    }

    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedBaseId by remember { mutableStateOf<String?>(null) }
    var selectedArtifactForModal by remember { mutableStateOf<DiscoveredArtifactRecord?>(null) }

    val filteredGroups = remember(report, selectedFilter, searchQuery) {
        val query = searchQuery.trim().lowercase(Locale.US)
        report.baseTitleGroups.filter { group ->
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "REVIEW" -> group.requiresHumanReview
                "MISSING" -> group.missingComponents.isNotEmpty()
                "DUPLICATES" -> group.duplicateCandidates.isNotEmpty()
                "HINDI" -> group.primaryLanguage.contains("Hindi", ignoreCase = true)
                "ENGLISH" -> group.primaryLanguage.contains("English", ignoreCase = true)
                else -> true
            }

            val matchesSearch = query.isEmpty() ||
                    group.title.lowercase(Locale.US).contains(query) ||
                    group.relativeFolder.lowercase(Locale.US).contains(query) ||
                    group.artifacts.any { it.fileName.lowercase(Locale.US).contains(query) }

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElyBackground)
    ) {
        // Sticky Header / Action Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ElyHeaderGlass,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = ElyPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "READ-ONLY CORPUS INGESTION DRY RUN",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ElyPurple
                            )
                        }
                        Text(
                            text = if (report.scanStatus == IngestionScanStatus.COMPLETED)
                                "Root: ${report.sourceRootDisplayName} • Non-Destructive Zero-Mutation Pass"
                            else "Select local directory to inspect ~390 base titles / 700+ artifacts",
                            fontSize = 11.sp,
                            color = ElyTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Scan / Re-scan Button
                    Button(
                        onClick = { safFolderLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (report.scanStatus == IngestionScanStatus.SCANNING) ElyAmberWarning else ElyPurple
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (report.scanStatus == IngestionScanStatus.SCANNING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Scanning...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DriveFolderUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Text(
                                    text = if (report.totalFilesDiscovered > 0) "📁 Re-Scan Directory" else "📁 Select Corpus Folder",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Stop condition reminder pill
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    color = ElyPurple.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyPurple.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = ElyPurple,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "GOVERNOR LOCK: Discovery & Evidence only. Curation Protocol 3.2.1.0 is paused. No files modified or auto-cured.",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = ElyPurple
                        )
                    }
                }
            }
        }

        // Main Scrollable Report Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. KPI Summary Cards Grid
            item {
                Text(
                    text = "DISCOVERY EVIDENCE SUMMARY",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "TOTAL FILES",
                        value = "${report.totalFilesDiscovered}",
                        subtitle = "Discovered",
                        icon = Icons.Default.Description,
                        accentColor = ElyPurple,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "BASE TITLES",
                        value = "${report.baseTitlesDiscovered}",
                        subtitle = "Grouped",
                        icon = Icons.Default.LibraryMusic,
                        accentColor = ElyG3Axiom,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "VERSIONS",
                        value = "${report.versionsDiscovered}",
                        subtitle = "Artifacts",
                        icon = Icons.Default.Layers,
                        accentColor = ElyCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "PARSED",
                        value = "${report.successfullyParsed}",
                        subtitle = "${report.unparsedCount} Unparsed",
                        icon = Icons.Default.CheckCircleOutline,
                        accentColor = if (report.unparsedCount == 0) ElyG3Axiom else ElyAmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "DUPLICATES",
                        value = "${report.duplicateCandidatesCount}",
                        subtitle = "Hash Matches",
                        icon = Icons.Default.ContentCopy,
                        accentColor = if (report.duplicateCandidatesCount > 0) ElyAmberWarning else ElyTextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "ORPHANS",
                        value = "${report.orphanArtifactsCount}",
                        subtitle = "Unattached",
                        icon = Icons.Default.HelpOutline,
                        accentColor = if (report.orphanArtifactsCount > 0) ElyAmberWarning else ElyG3Axiom,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Language & Evidence Breakdown Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language breakdown card
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = ElySurfaceCard,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "LANGUAGE DETECTION",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ElyTextSecondary
                            )
                            if (report.languageStats.isEmpty()) {
                                Text(
                                    text = "No lyric text scanned yet",
                                    fontSize = 11.sp,
                                    color = ElyTextTertiary
                                )
                            } else {
                                report.languageStats.forEach { (lang, count) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = lang, fontSize = 11.sp, color = ElyTextPrimary)
                                        Text(
                                            text = "$count",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = ElyCyan
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Forensic Evidence Category Breakdown
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = ElySurfaceCard,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "EVIDENCE STATUS",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ElyTextSecondary
                            )
                            if (report.evidenceStats.isEmpty()) {
                                Text(
                                    text = "No artifacts cataloged",
                                    fontSize = 11.sp,
                                    color = ElyTextTertiary
                                )
                            } else {
                                report.evidenceStats.forEach { (ev, count) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ev,
                                            fontSize = 10.sp,
                                            color = if (ev.contains("NOT MEASURED")) ElyAmberWarning else ElyTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "$count",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ev.contains("NOT MEASURED")) ElyAmberWarning else ElyG3Axiom
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Search & Filter Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search songs, files, paths...", fontSize = 11.sp, color = ElyTextTertiary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElyTextSecondary) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElyTextSecondary)
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElyPurple,
                            unfocusedBorderColor = ElyWindowBorderInactive,
                            focusedContainerColor = ElySurfaceCard,
                            unfocusedContainerColor = ElySurfaceCard
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Filter Chips
                val filterList = listOf(
                    "ALL" to "All Titles (${report.baseTitlesDiscovered})",
                    "REVIEW" to "Requires Review (${report.humanReviewItems.size})",
                    "MISSING" to "Missing Components (${report.missingExpectedComponentsCount})",
                    "DUPLICATES" to "Duplicates (${report.duplicateCandidatesCount})",
                    "HINDI" to "Hindi",
                    "ENGLISH" to "English"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filterList.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { selectedFilter = key },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) ElyPurple.copy(alpha = 0.2f) else ElySurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isSelected) ElyPurple else ElyWindowBorderInactive
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElyPurple else ElyTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. Discovered Base Titles List
            if (filteredGroups.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ElySurfaceCard,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = ElyTextTertiary
                            )
                            Text(
                                text = if (report.totalFilesDiscovered == 0) "No corpus directory scanned yet" else "No matching base titles found",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElyTextSecondary
                            )
                            Text(
                                text = if (report.totalFilesDiscovered == 0)
                                    "Tap 'Select Corpus Folder' above to point to your real corpus directory."
                                else "Try adjusting your search query or filter selection.",
                                fontSize = 11.sp,
                                color = ElyTextTertiary
                            )
                        }
                    }
                }
            } else {
                items(filteredGroups, key = { it.baseId }) { group ->
                    val isExpanded = expandedBaseId == group.baseId
                    BaseTitleGroupCard(
                        group = group,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedBaseId = if (isExpanded) null else group.baseId
                        },
                        onInspectArtifact = { selectedArtifactForModal = it }
                    )
                }
            }
        }
    }

    // Modal to inspect raw artifact snippet and hashes
    if (selectedArtifactForModal != null) {
        val artifact = selectedArtifactForModal!!
        AlertDialog(
            onDismissRequest = { selectedArtifactForModal = null },
            confirmButton = {
                TextButton(onClick = { selectedArtifactForModal = null }) {
                    Text("Close", color = ElyPurple)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = when (artifact.category) {
                            ArtifactCategory.LYRIC_TEXT -> Icons.Default.Article
                            ArtifactCategory.AUDIO_STREAM -> Icons.Default.Audiotrack
                            ArtifactCategory.STRUCTURED_SCHEMA -> Icons.Default.Code
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = ElyPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = artifact.fileName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = ElyBackground,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(text = "RELATIVE PATH: ${artifact.relativePath}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyTextSecondary)
                                Text(text = "DETERMINISTIC SHA-256:", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyTextSecondary)
                                Text(text = artifact.sha256Hash, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = ElyG3Axiom)
                                Text(text = "SIZE: ${artifact.fileSizeBytes} bytes • EXT: ${artifact.fileExtension}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyTextTertiary)
                                Text(text = "STATUS: ${artifact.discoveryState.name}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyCyan)
                            }
                        }

                        Text(
                            text = "PREVIEW / RAW OBSERVATION:",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElyTextSecondary
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            color = ElyHeaderGlass,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
                        ) {
                            Text(
                                text = artifact.snippetText ?: "(Binary or empty stream)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElyTextPrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            containerColor = ElySurfaceCard,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ElySurfaceCard,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyTextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = accentColor
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = ElyTextTertiary
            )
        }
    }
}

@Composable
private fun BaseTitleGroupCard(
    group: DiscoveredBaseTitleGroup,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onInspectArtifact: (DiscoveredArtifactRecord) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = ElySurfaceCard,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (group.requiresHumanReview) ElyAmberWarning.copy(alpha = 0.6f) else ElyWindowBorderInactive
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Row (Clickable to expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (group.requiresHumanReview) ElyAmberWarning.copy(alpha = 0.15f) else ElyPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (group.requiresHumanReview) Icons.Default.Warning else Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (group.requiresHumanReview) ElyAmberWarning else ElyPurple
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = group.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElyTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = ElyHeaderGlass,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
                            ) {
                                Text(
                                    text = group.primaryLanguage,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ElyCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${group.artifacts.size} files • ${group.lyricCount} lyrics • ${group.audioCount} audio • ${group.schemaCount} schemas",
                            fontSize = 10.sp,
                            color = ElyTextSecondary
                        )

                        if (group.missingComponents.isNotEmpty()) {
                            Text(
                                text = "⚠️ " + group.missingComponents.joinToString(" • "),
                                fontSize = 9.sp,
                                color = ElyAmberWarning,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = ElyTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded Children Files Accordion
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElyHeaderGlass)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CONSTITUENT ARTIFACTS (${group.artifacts.size})",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ElyTextSecondary
                    )

                    group.artifacts.forEach { art ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInspectArtifact(art) },
                            color = ElySurfaceCard,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, ElyWindowBorderInactive)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (art.category) {
                                            ArtifactCategory.LYRIC_TEXT -> Icons.Default.Article
                                            ArtifactCategory.AUDIO_STREAM -> Icons.Default.Audiotrack
                                            ArtifactCategory.STRUCTURED_SCHEMA -> Icons.Default.Code
                                            else -> Icons.Default.InsertDriveFile
                                        },
                                        contentDescription = null,
                                        tint = when (art.category) {
                                            ArtifactCategory.LYRIC_TEXT -> ElyPurple
                                            ArtifactCategory.AUDIO_STREAM -> ElyCyan
                                            ArtifactCategory.STRUCTURED_SCHEMA -> ElyG3Axiom
                                            else -> ElyTextSecondary
                                        },
                                        modifier = Modifier.size(14.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = art.fileName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = ElyTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${art.sha256Hash.take(18)}... • ${art.fileSizeBytes} B",
                                            fontSize = 8.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = ElyTextTertiary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = when (art.discoveryState) {
                                        IngestionDiscoveryState.PARSED -> ElyG3Axiom.copy(alpha = 0.15f)
                                        IngestionDiscoveryState.NOT_MEASURED -> ElyAmberWarning.copy(alpha = 0.15f)
                                        IngestionDiscoveryState.FAILED -> ElyError.copy(alpha = 0.15f)
                                        else -> ElyHeaderGlass
                                    }
                                ) {
                                    Text(
                                        text = art.discoveryState.name,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = when (art.discoveryState) {
                                            IngestionDiscoveryState.PARSED -> ElyG3Axiom
                                            IngestionDiscoveryState.NOT_MEASURED -> ElyAmberWarning
                                            IngestionDiscoveryState.FAILED -> ElyError
                                            else -> ElyTextSecondary
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
