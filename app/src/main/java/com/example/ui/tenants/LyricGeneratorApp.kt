package com.example.ui.tenants

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricGeneratorApp(
    studioMode: LyricStudioMode,
    advancedTab: AdvancedLyricTab,
    storyConcept: String,
    existingLyric: String,
    genre: String,
    rhymeScheme: String,
    stylePrompt: String,
    vocalTimbre: String,
    vocalGender: VocalGender,
    isInstrumental: Boolean,
    attachedAudio: AttachedAudio?,
    attachedVoice: AttachedVoice?,
    currentLyricEvidence: LyricEvidence? = null,
    excludeStyles: String,
    weirdness: Float,
    styleInfluence: Float,
    songTitleInput: String,
    audioProfile: AudioCadenceProfile,
    selectedMagicOp: MagicOperationType,
    activeSong: GeneratedSong?,
    rhymeQuery: String,
    rhymeSuggestions: List<RhymeSuggestion>,
    isGenerating: Boolean,
    turboValidationReport: TurboValidationReport? = null,
    activeCreativeDna: CreativeDnaProfile? = null,
    turboEngineMode: TurboEngineMode = TurboEngineMode.GENERATE,
    activeAcousticConstraint: ElyzarethAcousticConstraint = ELYZARETH_RUSTIC_ACOUSTIC_v1,
    onStudioModeChange: (LyricStudioMode) -> Unit,
    onAdvancedTabChange: (AdvancedLyricTab) -> Unit,
    onStoryConceptChange: (String) -> Unit,
    onExistingLyricChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onRhymeSchemeChange: (String) -> Unit,
    onStylePromptChange: (String) -> Unit,
    onVocalTimbreChange: (String) -> Unit,
    onVocalGenderChange: (VocalGender) -> Unit,
    onToggleInstrumental: () -> Unit,
    onAttachAudio: (AudioSourceType, String) -> Unit,
    onRemoveAttachedAudio: () -> Unit,
    onAttachVoice: (VoiceSourceType, String, String, String) -> Unit,
    onRemoveAttachedVoice: () -> Unit,
    onExcludeStylesChange: (String) -> Unit,
    onWeirdnessChange: (Float) -> Unit,
    onStyleInfluenceChange: (Float) -> Unit,
    onSongTitleInputChange: (String) -> Unit,
    onRandomizePrompt: () -> Unit,
    onAudioProfileChange: (AudioCadenceProfile) -> Unit,
    onSelectedMagicOpChange: (MagicOperationType) -> Unit,
    onExecuteLyricMagic: (MagicOperationType) -> Unit,
    onExecuteStyleMagic: () -> Unit,
    onExecuteAudioMagic: () -> Unit,
    onCommitCreate: () -> Unit,
    onSearchRhyme: (String) -> Unit,
    onSaveToArchive: () -> Unit,
    onSendToIntegrator: () -> Unit
) {
    var showAudioModal by remember { mutableStateOf(false) }
    var showVoiceModal by remember { mutableStateOf(false) }
    var showModeDropdown by remember { mutableStateOf(false) }
    var isLyricsExpanded by remember { mutableStateOf(true) }
    var isStylesExpanded by remember { mutableStateOf(true) }
    var isMoreOptionsExpanded by remember { mutableStateOf(false) }

    val quickStyleTags = listOf("hard rock", "bass", "opera", "atmospheric", "grunge", "electric guitar", "dance", "reggae")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141010))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // -------------------------------------------------------------
            // Top Bar: Simple/Advanced Dropdown & Version Badge
            // -------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode Selector (Simple / Advanced)
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showModeDropdown = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (studioMode == LyricStudioMode.SIMPLE) "Simple" else "Advanced",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElyTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Mode",
                                tint = ElyTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showModeDropdown,
                            onDismissRequest = { showModeDropdown = false },
                            modifier = Modifier.background(Color(0xFF221A1A))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Simple Mode", color = ElyTextPrimary) },
                                onClick = {
                                    onStudioModeChange(LyricStudioMode.SIMPLE)
                                    showModeDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Advanced Mode", color = ElyTextPrimary) },
                                onClick = {
                                    onStudioModeChange(LyricStudioMode.ADVANCED)
                                    showModeDropdown = false
                                }
                            )
                        }
                    }

                    // Version Tag & Turbo Engine Active Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF2E1F1A))
                                .border(0.5.dp, Color(0xFFFF6600).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFF6600)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TURBO // ${turboEngineMode.label}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9944)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF2A2020))
                                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "v38.1",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ElyTextSecondary
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = ElyTextTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // Audio & Voice Ingress Buttons / Attached Reference Pills
            // -------------------------------------------------------------
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (attachedAudio == null && attachedVoice == null) {
                        if (studioMode == LyricStudioMode.SIMPLE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF261D1D))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .clickable { showAudioModal = true }
                                    .testTag("btn_audio_option"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Audio",
                                        tint = ElyTextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audio",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ElyTextPrimary
                                    )
                                }
                            }
                        } else {
                            // Advanced mode shows + Audio and + Voice buttons side by side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF261D1D))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .clickable { showAudioModal = true }
                                        .testTag("btn_audio_option_adv"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Audio",
                                            tint = ElyTextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Audio",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ElyTextPrimary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF261D1D))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .clickable { showVoiceModal = true }
                                        .testTag("btn_voice_option_adv"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Voice",
                                            tint = ElyTextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Voice",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ElyTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Attached Audio Pill if active
                    if (attachedAudio != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2E2222))
                                .border(1.dp, ElyCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = when (attachedAudio.type) {
                                            AudioSourceType.BROWSE -> Icons.Default.Explore
                                            AudioSourceType.UPLOAD -> Icons.Default.UploadFile
                                            AudioSourceType.RECORD -> Icons.Default.Mic
                                            AudioSourceType.NONE -> Icons.Default.Audiotrack
                                        },
                                        contentDescription = null,
                                        tint = ElyCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = attachedAudio.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElyTextPrimary
                                        )
                                        Text(
                                            text = "${attachedAudio.type.label} • Audio Evidence Ready",
                                            fontSize = 10.sp,
                                            color = ElyCyan
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // ✨ Magic Wand inside Audio Pill
                                    IconButton(
                                        onClick = onExecuteAudioMagic,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(ElyCyan.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Audio Magic Alignment",
                                            tint = ElyCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = onRemoveAttachedAudio,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Audio",
                                            tint = ElyTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Attached Voice Pill if active
                    if (attachedVoice != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2E1F2A))
                                .border(1.dp, ElyViolet.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = ElyViolet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = attachedVoice.personaName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElyTextPrimary
                                        )
                                        Text(
                                            text = "${attachedVoice.type.label} • Timbre: ${attachedVoice.timbre} (${attachedVoice.pitchRange})",
                                            fontSize = 10.sp,
                                            color = ElyViolet
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onRemoveAttachedVoice,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Voice",
                                        tint = ElyTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =============================================================
            // SIMPLE MODE: SONG DESCRIPTION CARD
            // =============================================================
            if (studioMode == LyricStudioMode.SIMPLE) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF261D1D))
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Card Header: "Song Description" + Wand/Magic + Random Dice
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Song Description",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ElyTextSecondary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Refresh prompt
                                    IconButton(
                                        onClick = onRandomizePrompt,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF332727))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Regenerate Idea",
                                            tint = ElyTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // ✨ Magic Wand Button inside card
                                    IconButton(
                                        onClick = { onExecuteLyricMagic(MagicOperationType.CREATE) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3B2E2E))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Magic Transform",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Dice icon
                                    IconButton(
                                        onClick = onRandomizePrompt,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF3B2E2E))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Casino,
                                            contentDescription = "Random Prompt",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Prompt Input Field
                            OutlinedTextField(
                                value = storyConcept,
                                onValueChange = onStoryConceptChange,
                                placeholder = {
                                    Text(
                                        text = "A groovy synthwave song about a faded photo on the mantel",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.25f)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 80.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = ElyTextPrimary,
                                    unfocusedTextColor = ElyTextPrimary
                                )
                            )

                            // Tag Pills (e.g. hard rock, bass, opera, atmospheric)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF332727))
                                            .clickable { onRandomizePrompt() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh Tags",
                                            tint = ElyTextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                items(quickStyleTags) { tag ->
                                    val isSelected = genre.equals(tag, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) ElyCyan.copy(alpha = 0.25f) else Color(0xFF332727))
                                            .border(
                                                0.5.dp,
                                                if (isSelected) ElyCyan else Color.White.copy(alpha = 0.05f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { onGenreChange(tag) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) ElyCyan else ElyTextSecondary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)

                            // Bottom Toggles: Instrumental toggle & Lyrics toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Instrumental Toggle
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isInstrumental) Color(0xFF4A3838) else Color(0xFF332727))
                                        .clickable { onToggleInstrumental() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isInstrumental) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = if (isInstrumental) ElyCyan else ElyTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Instrumental",
                                        fontSize = 12.sp,
                                        color = if (isInstrumental) ElyTextPrimary else ElyTextSecondary
                                    )
                                }

                                // Lyrics Toggle / Advanced Switcher
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF332727))
                                        .clickable { onStudioModeChange(LyricStudioMode.ADVANCED) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = ElyTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Lyrics",
                                        fontSize = 12.sp,
                                        color = ElyTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =============================================================
            // ADVANCED MODE: ACCORDION CARDS (LYRICS, STYLES, MORE OPTIONS)
            // =============================================================
            if (studioMode == LyricStudioMode.ADVANCED) {
                // 1. Accordion Card: LYRICS
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF261D1D))
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { isLyricsExpanded = !isLyricsExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (isLyricsExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = ElyTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Lyrics",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElyTextPrimary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // ✨ Magic Wand inside Lyrics Card
                                    IconButton(
                                        onClick = { onExecuteLyricMagic(MagicOperationType.REWRITE) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3B2E2E))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Magic Lyrics Rewrite",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Expand icon
                                    IconButton(
                                        onClick = { onExecuteLyricMagic(MagicOperationType.EXPAND) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF332727))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "Expand Stanzas",
                                            tint = ElyTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = isLyricsExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = existingLyric,
                                        onValueChange = onExistingLyricChange,
                                        placeholder = {
                                            Text(
                                                text = "Write lyrics or a prompt...\n[Verse 1]\n[Chorus]",
                                                fontSize = 13.sp,
                                                color = Color.White.copy(alpha = 0.25f)
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 100.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF1D1616),
                                            unfocusedContainerColor = Color(0xFF1D1616),
                                            focusedBorderColor = ElyCyan.copy(alpha = 0.3f),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                                            focusedTextColor = ElyTextPrimary,
                                            unfocusedTextColor = ElyTextPrimary
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isInstrumental) Color(0xFF4A3838) else Color(0xFF332727))
                                                .clickable { onToggleInstrumental() }
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isInstrumental) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                                contentDescription = null,
                                                tint = if (isInstrumental) ElyCyan else ElyTextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Instrumental",
                                                fontSize = 12.sp,
                                                color = if (isInstrumental) ElyTextPrimary else ElyTextSecondary
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF332727))
                                                    .clickable { onExecuteLyricMagic(MagicOperationType.STRUCTURE) }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("✨ Structure", fontSize = 11.sp, color = ElyCyan)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF332727))
                                                    .clickable { onExecuteLyricMagic(MagicOperationType.RHYME_METER) }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("✨ Meter", fontSize = 11.sp, color = ElyViolet)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Accordion Card: STYLES
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF261D1D))
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { isStylesExpanded = !isStylesExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (isStylesExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = ElyTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Styles",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElyTextPrimary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // ✨ Magic Wand inside Styles Card -> invokes onExecuteStyleMagic (deriving acoustic profile from lyrics)
                                    IconButton(
                                        onClick = onExecuteStyleMagic,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3B2E2E))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Magic Style Transform",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { /* Fullscreen modal */ },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF332727))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "Expand Style",
                                            tint = ElyTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = isStylesExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = stylePrompt,
                                        onValueChange = onStylePromptChange,
                                        placeholder = {
                                            Text(
                                                text = "Describe what you want your song to sound like",
                                                fontSize = 13.sp,
                                                color = Color.White.copy(alpha = 0.25f)
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 70.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF1D1616),
                                            unfocusedContainerColor = Color(0xFF1D1616),
                                            focusedBorderColor = ElyViolet.copy(alpha = 0.3f),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                                            focusedTextColor = ElyTextPrimary,
                                            unfocusedTextColor = ElyTextPrimary
                                        )
                                    )

                                    if (currentLyricEvidence != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ElyViolet.copy(alpha = 0.12f))
                                                .border(0.5.dp, ElyViolet.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = ElyViolet,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = "Lyric Evidence Ingested by Style Engine",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ElyViolet
                                                    )
                                                }
                                                Text(
                                                    text = "Theme: ${currentLyricEvidence.theme} • Emotion: ${currentLyricEvidence.emotionalProfile}",
                                                    fontSize = 10.sp,
                                                    color = ElyTextPrimary
                                                )
                                                Text(
                                                    text = "Witness: ${currentLyricEvidence.witnessObjects.joinToString()} • Era: ${currentLyricEvidence.temporalContext}",
                                                    fontSize = 10.sp,
                                                    color = ElyTextSecondary
                                                )
                                            }
                                        }
                                    }

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF332727))
                                                    .clickable { onRandomizePrompt() },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Refresh Tags",
                                                    tint = ElyTextSecondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        items(quickStyleTags) { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color(0xFF332727))
                                                    .clickable {
                                                        if (!stylePrompt.contains(tag, ignoreCase = true)) {
                                                            onStylePromptChange(if (stylePrompt.isBlank()) tag else "$stylePrompt, $tag")
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(text = tag, fontSize = 11.sp, color = ElyTextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Accordion Card: MORE OPTIONS (Vocal Gender, Weirdness, Style Influence, Song Title)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF261D1D))
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isMoreOptionsExpanded = !isMoreOptionsExpanded },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isMoreOptionsExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = ElyTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "More Options",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElyTextPrimary
                                )
                            }

                            AnimatedVisibility(
                                visible = isMoreOptionsExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    // Vocal Gender (Male / Female)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Vocal Gender", fontSize = 13.sp, color = ElyTextSecondary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Info, contentDescription = null, tint = ElyTextTertiary, modifier = Modifier.size(14.dp))
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(VocalGender.MALE, VocalGender.FEMALE).forEach { gender ->
                                                val isSelected = vocalGender == gender
                                                Text(
                                                    text = gender.label,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) ElyCyan else ElyTextTertiary,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelected) ElyCyan.copy(alpha = 0.15f) else Color.Transparent)
                                                        .clickable { onVocalGenderChange(if (isSelected) VocalGender.ANY else gender) }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Exclude styles input
                                    OutlinedTextField(
                                        value = excludeStyles,
                                        onValueChange = onExcludeStylesChange,
                                        placeholder = { Text("Exclude styles (e.g. heavy drums, auto-tune)", fontSize = 12.sp, color = Color.White.copy(alpha = 0.25f)) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = ElyTextTertiary, modifier = Modifier.size(16.dp))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF1D1616),
                                            unfocusedContainerColor = Color(0xFF1D1616),
                                            focusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                                            focusedTextColor = ElyTextPrimary,
                                            unfocusedTextColor = ElyTextPrimary
                                        )
                                    )

                                    // Weirdness Slider
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Weirdness", fontSize = 13.sp, color = ElyTextSecondary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Info, contentDescription = null, tint = ElyTextTertiary, modifier = Modifier.size(14.dp))
                                            }
                                            Text("${weirdness.toInt()}%", fontSize = 12.sp, color = ElyTextSecondary)
                                        }
                                        Slider(
                                            value = weirdness,
                                            onValueChange = onWeirdnessChange,
                                            valueRange = 0f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFFFF3366),
                                                activeTrackColor = Color(0xFFFF3366)
                                            )
                                        )
                                    }

                                    // Style Influence Slider
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Style influence", fontSize = 13.sp, color = ElyTextSecondary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Info, contentDescription = null, tint = ElyTextTertiary, modifier = Modifier.size(14.dp))
                                            }
                                            Text("${styleInfluence.toInt()}%", fontSize = 12.sp, color = ElyTextSecondary)
                                        }
                                        Slider(
                                            value = styleInfluence,
                                            onValueChange = onStyleInfluenceChange,
                                            valueRange = 0f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFFFF3366),
                                                activeTrackColor = Color(0xFFFF3366)
                                            )
                                        )
                                    }

                                    // Song Title Input
                                    OutlinedTextField(
                                        value = songTitleInput,
                                        onValueChange = onSongTitleInputChange,
                                        placeholder = { Text("Song Title", fontSize = 12.sp, color = Color.White.copy(alpha = 0.25f)) },
                                        leadingIcon = {
                                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = ElyTextTertiary, modifier = Modifier.size(16.dp))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF1D1616),
                                            unfocusedContainerColor = Color(0xFF1D1616),
                                            focusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                                            focusedTextColor = ElyTextPrimary,
                                            unfocusedTextColor = ElyTextPrimary
                                        )
                                    )

                                    // G6 Cure Chamber Shortcut inside More Options
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF2E1A1A))
                                            .border(1.dp, ElyAmberWarning.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("G6 Cure Recovery", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElyAmberWarning)
                                                Text("Extract gems from contaminated text", fontSize = 9.sp, color = ElyTextSecondary)
                                            }
                                            Button(
                                                onClick = { onExecuteLyricMagic(MagicOperationType.CURE) },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = ElyAmberWarning)
                                            ) {
                                                Text("Cure", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // Bottom Action: Big Glowing 🎵 Create Button (Master Commit)
            // -------------------------------------------------------------
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Button(
                        onClick = onCommitCreate,
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_create_song"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4500)
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Creating Suite...",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // TURBO ENGINE VALIDATION REPORT & CREATIVE DNA CARD
            // -------------------------------------------------------------
            if (turboValidationReport != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1410))
                            .border(1.dp, Color(0xFFFF6600).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9944),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "TURBO ENGINE CRAFT GOVERNANCE",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9944)
                                    )
                                }
                                if (activeCreativeDna != null) {
                                    Text(
                                        text = activeCreativeDna.dnaId,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ElyTextSecondary
                                    )
                                }
                            }

                            Text(
                                text = turboValidationReport.engineDiagnosticSummary,
                                fontSize = 11.sp,
                                color = ElyTextPrimary
                            )

                            // Diagnostic Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2B1D16))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text("PHYSICAL_ANCHOR STATUS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyTextTertiary)
                                        val diag = turboValidationReport.physicalAnchorDiagnostic
                                        val statusText = diag?.status ?: if (turboValidationReport.physicalAnchorCount > 0) "PASS" else "FAIL"
                                        Text(
                                            text = "$statusText (${turboValidationReport.physicalAnchorCount} Objects)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (statusText == "PASS") ElyG3Axiom else ElyAmberWarning
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2B1D16))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text("ANCHOR_DENSITY", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElyTextTertiary)
                                        Text(
                                            text = turboValidationReport.physicalAnchorDiagnostic?.anchorDensity ?: "${turboValidationReport.physicalAnchorCount} anchors",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ElyCyanBright
                                        )
                                    }
                                }
                            }

                            // Anchors and Collision Flags
                            val diag = turboValidationReport.physicalAnchorDiagnostic
                            if (diag != null) {
                                if (diag.anchorObjects.isNotEmpty()) {
                                    Text(
                                        text = "ANCHOR_OBJECTS: [${diag.anchorObjects.joinToString(", ")}]",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ElyTextSecondary
                                    )
                                }
                                if (diag.collisionFlags.isNotEmpty()) {
                                    Text(
                                        text = "COLLISION_FLAGS: [${diag.collisionFlags.joinToString(", ")}]",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ElyAmberWarning
                                    )
                                }
                                if (diag.failReason != null) {
                                    Text(
                                        text = diag.failReason,
                                        fontSize = 10.sp,
                                        color = ElyAmberWarning
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // ROOM 05 RUSTIC ACOUSTIC CONSTRAINT (NON-DESTRUCTIVE DOWNSTREAM INTENT)
            // -------------------------------------------------------------
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141C1A))
                        .border(1.dp, Color(0xFF00AA88).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5AA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = activeAcousticConstraint.constraintId,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5AA)
                                )
                            }
                            Text(
                                text = "DOWNSTREAM INTENT (v${activeAcousticConstraint.schemaVersion})",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElyTextTertiary
                            )
                        }

                        Text(
                            text = "${activeAcousticConstraint.roomProfile} | T60 < ${activeAcousticConstraint.maxT60Seconds}s | Wet < ${(activeAcousticConstraint.maxWetRatioPercent).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElyTextPrimary
                        )

                        Text(
                            text = "Spec: ${activeAcousticConstraint.vocalSpec} • ${activeAcousticConstraint.instrumentationSpec}",
                            fontSize = 10.sp,
                            color = ElyTextSecondary
                        )

                        // Locked Sparse Arrangement Constraints v1.0 display
                        val sparse = activeAcousticConstraint.sparseArrangementConstraints
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0C1412))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "${sparse.constraintId} (LOCKED SPEC)",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ElyCyanBright
                                )
                                Text(
                                    text = "1. Percussion Suppression: Zero kicks, snares, hats, loops",
                                    fontSize = 9.sp,
                                    color = ElyTextSecondary
                                )
                                Text(
                                    text = "2. Pad Suppression: Zero synths, drones, bowed strings, washes",
                                    fontSize = 9.sp,
                                    color = ElyTextSecondary
                                )
                                Text(
                                    text = "3. Acoustic Core: ${sparse.coreAcousticRealization}",
                                    fontSize = 9.sp,
                                    color = ElyTextSecondary
                                )
                                Text(
                                    text = "4. Expansion: ${sparse.sectionalExpansionRule}",
                                    fontSize = 9.sp,
                                    color = ElyTextSecondary
                                )
                                Text(
                                    text = "5. Dynamic Restraint: ${sparse.dynamicRestraintProfile} (No swells/risers)",
                                    fontSize = 9.sp,
                                    color = ElyTextSecondary
                                )
                                Text(
                                    text = "6. Arrangement Drift Target: ${(sparse.arrangementDriftTargetPercent).toInt()}% (Locked Instrument Allocation)",
                                    fontSize = 9.sp,
                                    color = ElyTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF102820))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔒 DECLARED PRODUCTION CONSTRAINTS ≠ MEASURED AUDIO EVIDENCE (Forensic PCM evaluated in App 03)",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF00E5AA).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // ACTIVE GENERATED / TRANSFORMED SONG SUITE DISPLAY
            // -------------------------------------------------------------
            if (activeSong != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = activeSong.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElyTextPrimary
                            )
                            Text(
                                text = "Cadence: ${activeSong.cadence} | Rhyme: ${activeSong.rhymeScheme}",
                                fontSize = 10.sp,
                                color = ElyTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElyG3Axiom.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = activeSong.g3SealHash.take(16),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElyG3Axiom
                            )
                        }
                    }
                }

                // Stanza cards
                items(activeSong.stanzas) { stanza ->
                    StanzaCard(stanza = stanza)
                }

                // Cross-Tenant Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSendToIntegrator,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElyViolet)
                        ) {
                            Icon(Icons.Default.Cable, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send to Integrator", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onSaveToArchive,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(ElyCyan, ElyPurple)))
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElyCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Archive", fontSize = 11.sp, color = ElyTextPrimary)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // AUDIO BOTTOM SHEET MODAL (Browse, Upload, Record)
        // =========================================================================
        if (showAudioModal) {
            ModalBottomSheet(
                onDismissRequest = { showAudioModal = false },
                containerColor = Color(0xFF1E1717),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Option 1: 🧭 Browse
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAttachAudio(AudioSourceType.BROWSE, "Library_Acoustic_Loop_120BPM.wav")
                                showAudioModal = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Browse Audio",
                            tint = ElyTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Browse",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElyTextPrimary
                            )
                            Text(
                                text = "Select from sample library or community audio",
                                fontSize = 11.sp,
                                color = ElyTextTertiary
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Option 2: ⬆️ Upload
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAttachAudio(AudioSourceType.UPLOAD, "My_Uploaded_Stem.wav")
                                showAudioModal = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Upload Audio",
                            tint = ElyTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Upload",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElyTextPrimary
                            )
                            Text(
                                text = "Upload audio file or stem from device storage",
                                fontSize = 11.sp,
                                color = ElyTextTertiary
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Option 3: 🎤 Record
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAttachAudio(AudioSourceType.RECORD, "Live_Recording_Clip.m4a")
                                showAudioModal = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record Audio",
                            tint = ElyTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Record",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElyTextPrimary
                            )
                            Text(
                                text = "Record voice memo or acoustic melody directly",
                                fontSize = 11.sp,
                                color = ElyTextTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // =========================================================================
        // VOICE BOTTOM SHEET MODAL (Record, Upload, Persona Library)
        // =========================================================================
        if (showVoiceModal) {
            ModalBottomSheet(
                onDismissRequest = { showVoiceModal = false },
                containerColor = Color(0xFF1F1622),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Attach Voice Timbre / Persona",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElyTextPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Option 1: 🎤 Record Live Vocal Take
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAttachVoice(VoiceSourceType.RECORD, "Live Vocal Capture #1", "Warm & Husky", "Mezzo-Soprano")
                                showVoiceModal = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record Vocal",
                            tint = ElyViolet,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Record Voice Take",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElyTextPrimary
                            )
                            Text(
                                text = "Record a quick 10-second vocal take for acoustic timbre clone",
                                fontSize = 11.sp,
                                color = ElyTextTertiary
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Option 2: ⬆️ Upload Acapella Stem
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAttachVoice(VoiceSourceType.UPLOAD, "Uploaded_Acapella_Lead.wav", "Silky & Resonant", "Baritone / Tenor")
                                showVoiceModal = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Upload Voice",
                            tint = ElyViolet,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Upload Acapella Stem",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElyTextPrimary
                            )
                            Text(
                                text = "Attach .wav or .mp3 clean vocal stem reference",
                                fontSize = 11.sp,
                                color = ElyTextTertiary
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Option 3: 🎭 Select Persona from Library
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAttachVoice(VoiceSourceType.LIBRARY, "Sovereign Ethereal (Aria)", "Crystalline & Breathy", "High Soprano")
                                showVoiceModal = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice Library",
                            tint = ElyViolet,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Voice Persona Library",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElyTextPrimary
                            )
                            Text(
                                text = "Select pre-calibrated sovereign vocal timbre profile",
                                fontSize = 11.sp,
                                color = ElyTextTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StanzaCard(stanza: Stanza) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ElySurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (stanza.isGemFlagged) listOf(ElyAmberWarning, ElyCyan)
                else listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stanza.type,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (stanza.isGemFlagged) ElyAmberWarning else ElyCyan
                    )
                    if (stanza.isGemFlagged) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(ElyAmberWarning.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("G6 CURED GEM", fontSize = 8.sp, color = ElyAmberWarning, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    text = "Rhyme: ${(stanza.rhymeScore * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = ElyG1Lexical
                )
            }

            stanza.lines.forEachIndexed { index, line ->
                val syllables = stanza.syllableCounts.getOrElse(index) { 8 }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = line,
                        fontSize = 12.sp,
                        color = ElyTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${syllables}s",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElyTextTertiary
                    )
                }
            }
        }
    }
}
