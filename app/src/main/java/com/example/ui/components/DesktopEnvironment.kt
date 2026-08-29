package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.tenants.*
import com.example.ui.theme.*
import com.example.viewmodel.ElyzarethOSViewModel

@Composable
fun DesktopEnvironment(
    viewModel: ElyzarethOSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val windows by viewModel.windows.collectAsState()
    val activeAppId by viewModel.activeAppId.collectAsState()
    val isStartMenuOpen by viewModel.isStartMenuOpen.collectAsState()
    val isQuickSettingsOpen by viewModel.isQuickSettingsOpen.collectAsState()
    val wallpaper by viewModel.desktopWallpaper.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val telemetry by viewModel.engineTelemetry.collectAsState()
    val systemToast by viewModel.systemToast.collectAsState()
    val tenantMetrics by viewModel.tenantMetrics.collectAsState()

    // Lyric Studio State & Panel Architecture
    val lyricStudioMode by viewModel.lyricStudioMode.collectAsState()
    val advancedLyricTab by viewModel.advancedLyricTab.collectAsState()
    val lyricPrompt by viewModel.lyricPrompt.collectAsState()
    val existingLyric by viewModel.existingLyric.collectAsState()
    val lyricGenre by viewModel.lyricGenre.collectAsState()
    val lyricRhymeScheme by viewModel.lyricRhymeScheme.collectAsState()
    val stylePrompt by viewModel.stylePrompt.collectAsState()
    val vocalTimbre by viewModel.vocalTimbre.collectAsState()
    val vocalGender by viewModel.vocalGender.collectAsState()
    val isInstrumental by viewModel.isInstrumental.collectAsState()
    val attachedAudio by viewModel.attachedAudio.collectAsState()
    val attachedVoice by viewModel.attachedVoice.collectAsState()
    val currentLyricEvidence by viewModel.currentLyricEvidence.collectAsState()
    val excludeStyles by viewModel.excludeStyles.collectAsState()
    val weirdness by viewModel.weirdness.collectAsState()
    val styleInfluence by viewModel.styleInfluence.collectAsState()
    val songTitleInput by viewModel.songTitleInput.collectAsState()
    val audioCadenceProfile by viewModel.audioCadenceProfile.collectAsState()
    val selectedMagicOp by viewModel.selectedMagicOp.collectAsState()
    val activeSong by viewModel.activeSong.collectAsState()
    val rhymeQuery by viewModel.rhymeQuery.collectAsState()
    val rhymeSuggestions by viewModel.rhymeSuggestions.collectAsState()
    val isGeneratingLyric by viewModel.isGeneratingLyric.collectAsState()
    val turboValidationReport by viewModel.turboValidationReport.collectAsState()
    val activeCreativeDna by viewModel.activeCreativeDna.collectAsState()
    val turboEngineMode by viewModel.turboEngineMode.collectAsState()
    val activeAcousticConstraint by viewModel.activeAcousticConstraint.collectAsState()


    // Corpus / Lyric Curator (The Sitting Room) State
    val baseCompositions by viewModel.baseCompositions.collectAsState()
    val selectedBaseCompositionId by viewModel.selectedBaseCompositionId.collectAsState()
    val selectedVersionId by viewModel.selectedVersionId.collectAsState()
    val sittingRoomTab by viewModel.sittingRoomTab.collectAsState()
    val selectedGateDiagnostic by viewModel.selectedGateDiagnostic.collectAsState()
    val isIngressDialogOpen by viewModel.isIngressDialogOpen.collectAsState()
    val ingressDialogType by viewModel.ingressDialogType.collectAsState()
    val ingressTitle by viewModel.ingressTitle.collectAsState()
    val ingressLyricText by viewModel.ingressLyricText.collectAsState()
    val ingressSourceOrigin by viewModel.ingressSourceOrigin.collectAsState()
    val ingressAudioIncluded by viewModel.ingressAudioIncluded.collectAsState()
    val ingressAudioDecoderPass by viewModel.ingressAudioDecoderPass.collectAsState()
    val corpusList by viewModel.corpusList.collectAsState()
    val selectedCorpus by viewModel.selectedCorpus.collectAsState()
    val corpusSearch by viewModel.corpusSearch.collectAsState()
    val corpusInventoryReport by viewModel.corpusInventoryReport.collectAsState()

    // Integrator State
    val pipelineNodes by viewModel.pipelineNodes.collectAsState()
    val pipelineStatus by viewModel.pipelineStatus.collectAsState()
    val pipelineProgress by viewModel.pipelineProgress.collectAsState()
    val pipelineLogs by viewModel.pipelineExecutionLogs.collectAsState()
    val masterBundle by viewModel.masterIntegratedBundle.collectAsState()

    // Engine Terminal State
    val auditLogs by viewModel.auditLogs.collectAsState()
    val testbenchInput by viewModel.testbenchInput.collectAsState()
    val testbenchResult by viewModel.testbenchResult.collectAsState()

    // Space Archive State
    val archiveFiles by viewModel.archiveFiles.collectAsState()
    val selectedArchiveFile by viewModel.selectedArchiveFile.collectAsState()
    val archiveCategoryFilter by viewModel.archiveCategoryFilter.collectAsState()

    // Load persisted dry run report on initial launch
    LaunchedEffect(Unit) {
        viewModel.loadPersistedCorpusReport(context)
    }

    // Auto-clear toast
    LaunchedEffect(systemToast) {
        if (systemToast != null) {
            kotlinx.coroutines.delay(2800)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ElyBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isStartMenuOpen) viewModel.toggleStartMenu()
                if (isQuickSettingsOpen) viewModel.toggleQuickSettings()
            }
    ) {
        // Dynamic Desktop Wallpaper Canvas
        DesktopWallpaperCanvas(wallpaper = wallpaper)

        // Elyzareth OS Desktop Workspace Canvas (Visible when windows are minimized/open)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 72.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top OS Bar & Center Branding
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // OS Status Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElyHeaderGlass)
                        .border(0.5.dp, ElyCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ElyG3Axiom))
                    Text(
                        text = "ELYZARETH OS V3.2.1 • FORENSIC WORKSPACE • G3 ACTIVE",
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ElyCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ELYZARETH OS",
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.88f),
                    letterSpacing = 6.sp
                )
                Text(
                    text = "ONE SPACE // FIVE TENANTS // FORENSIC WORKSPACE",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElyCyan.copy(alpha = 0.75f),
                    letterSpacing = 2.sp
                )
            }

            // Five Tenant Entry Cards Grid (Center of Desktop Shell)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SOVEREIGN TENANTS // ACTIVE DOMAINS",
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElyTextSecondary,
                    letterSpacing = 1.sp
                )

                AppId.values().forEach { appId ->
                    val isRunning = windows[appId]?.isClosed == false
                    val isForeground = activeAppId == appId && windows[appId]?.isMinimized == false

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isForeground) ElyPurple.copy(alpha = 0.22f)
                                else ElySurfaceCard.copy(alpha = 0.85f)
                            )
                            .border(
                                width = if (isForeground) 1.dp else 0.5.dp,
                                color = if (isForeground) ElyPurple else ElyWindowBorderInactive,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.openApp(appId) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
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
                                )
                                .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = appId.defaultIcon,
                                contentDescription = appId.title,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = appId.tenantNumber,
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = when (appId) {
                                        AppId.LYRIC_GENERATOR -> ElyCyan
                                        AppId.CORPUS_CURATOR -> ElyPurple
                                        AppId.INTEGRATOR -> ElyCyanBright
                                        AppId.ENGINE_TERMINAL -> ElyG3Axiom
                                        AppId.SPACE_ARCHIVE -> ElyIndigo
                                    }
                                )
                                Text(
                                    text = "•",
                                    fontSize = 7.5.sp,
                                    color = ElyTextTertiary
                                )
                                Text(
                                    text = appId.title,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ElyTextPrimary
                                )
                            }
                            Text(
                                text = appId.subtitle,
                                fontSize = 8.sp,
                                color = ElyTextSecondary,
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isRunning) ElyG3Axiom.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                .border(
                                    0.5.dp,
                                    if (isRunning) ElyG3Axiom.copy(alpha = 0.6f) else ElyWindowBorderInactive,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isRunning) "LAUNCHED" else "OPEN",
                                fontSize = 7.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) ElyG3Axiom else ElyTextSecondary
                            )
                        }
                    }
                }
            }

            // Bottom Quick Tips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0C1018))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tap any tenant to open in window shell", fontSize = 7.5.sp, color = ElyTextTertiary)
                Text("Taskbar controls below", fontSize = 7.5.sp, color = ElyCyan.copy(alpha = 0.7f))
            }
        }

        // Window Shells Area (Landlord Space)
        val sortedWindows = windows.values.toList().sortedBy { it.zIndex }
        sortedWindows.forEach { win ->
            val isActive = activeAppId == win.appId

            UniversalWindowShell(
                window = win,
                isActive = isActive,
                onFocus = { viewModel.focusWindow(win.appId) },
                onMinimize = { viewModel.minimizeWindow(win.appId) },
                onToggleMaximize = { viewModel.toggleMaximizeWindow(win.appId) },
                onClose = { viewModel.closeWindow(win.appId) },
                onDragPosition = { x, y -> viewModel.updateWindowPosition(win.appId, x, y) }
            ) {
                when (win.appId) {
                    AppId.LYRIC_GENERATOR -> {
                        LyricGeneratorApp(
                            studioMode = lyricStudioMode,
                            advancedTab = advancedLyricTab,
                            storyConcept = lyricPrompt,
                            existingLyric = existingLyric,
                            genre = lyricGenre,
                            rhymeScheme = lyricRhymeScheme,
                            stylePrompt = stylePrompt,
                            vocalTimbre = vocalTimbre,
                            vocalGender = vocalGender,
                            isInstrumental = isInstrumental,
                            attachedAudio = attachedAudio,
                            attachedVoice = attachedVoice,
                            currentLyricEvidence = currentLyricEvidence,
                            excludeStyles = excludeStyles,
                            weirdness = weirdness,
                            styleInfluence = styleInfluence,
                            songTitleInput = songTitleInput,
                            audioProfile = audioCadenceProfile,
                            selectedMagicOp = selectedMagicOp,
                            activeSong = activeSong,
                            rhymeQuery = rhymeQuery,
                            rhymeSuggestions = rhymeSuggestions,
                            isGenerating = isGeneratingLyric,
                            turboValidationReport = turboValidationReport,
                            activeCreativeDna = activeCreativeDna,
                            turboEngineMode = turboEngineMode,
                            activeAcousticConstraint = activeAcousticConstraint,
                            onStudioModeChange = viewModel::setLyricStudioMode,
                            onAdvancedTabChange = viewModel::setAdvancedLyricTab,
                            onStoryConceptChange = viewModel::setLyricPrompt,
                            onExistingLyricChange = viewModel::setExistingLyric,
                            onGenreChange = viewModel::setLyricGenre,
                            onRhymeSchemeChange = viewModel::setLyricRhymeScheme,
                            onStylePromptChange = viewModel::setStylePrompt,
                            onVocalTimbreChange = viewModel::setVocalTimbre,
                            onVocalGenderChange = viewModel::setVocalGender,
                            onToggleInstrumental = viewModel::toggleInstrumental,
                            onAttachAudio = viewModel::attachAudio,
                            onRemoveAttachedAudio = viewModel::removeAttachedAudio,
                            onAttachVoice = viewModel::attachVoice,
                            onRemoveAttachedVoice = viewModel::removeAttachedVoice,
                            onExcludeStylesChange = viewModel::setExcludeStyles,
                            onWeirdnessChange = viewModel::setWeirdness,
                            onStyleInfluenceChange = viewModel::setStyleInfluence,
                            onSongTitleInputChange = viewModel::setSongTitleInput,
                            onRandomizePrompt = viewModel::randomizePrompt,
                            onAudioProfileChange = viewModel::setAudioCadenceProfile,
                            onSelectedMagicOpChange = viewModel::setSelectedMagicOp,
                            onExecuteLyricMagic = viewModel::executeLyricMagic,
                            onExecuteStyleMagic = viewModel::executeStyleMagic,
                            onExecuteAudioMagic = viewModel::executeAudioMagic,
                            onCommitCreate = viewModel::executeCommitCreate,
                            onSearchRhyme = viewModel::searchRhymes,
                            onSaveToArchive = viewModel::saveSongToArchive,
                            onSendToIntegrator = viewModel::sendSongToIntegrator
                        )
                    }
                    AppId.CORPUS_CURATOR -> {
                        CorpusCuratorApp(
                            baseCompositions = baseCompositions,
                            selectedBaseCompositionId = selectedBaseCompositionId,
                            selectedVersionId = selectedVersionId,
                            sittingRoomTab = sittingRoomTab,
                            selectedGateDiagnostic = selectedGateDiagnostic,
                            corpusSearch = corpusSearch,
                            corpusInventoryReport = corpusInventoryReport,
                            isIngressDialogOpen = isIngressDialogOpen,
                            ingressDialogType = ingressDialogType,
                            ingressTitle = ingressTitle,
                            ingressLyricText = ingressLyricText,
                            ingressSourceOrigin = ingressSourceOrigin,
                            ingressAudioIncluded = ingressAudioIncluded,
                            ingressAudioDecoderPass = ingressAudioDecoderPass,
                            onSelectBaseComposition = viewModel::selectBaseComposition,
                            onSelectVersion = viewModel::selectSpecimenVersion,
                            onSelectTab = viewModel::setSittingRoomTab,
                            onSelectGate = viewModel::selectGateDiagnostic,
                            onPreserve = viewModel::preserveSpecimen,
                            onAccept = viewModel::acceptSpecimenToVault,
                            onSendToEngine = viewModel::sendSpecimenToEngine,
                            onSearchChange = viewModel::setCorpusSearch,
                            onOpenIngressDialog = viewModel::openIngressDialog,
                            onCloseIngressDialog = viewModel::closeIngressDialog,
                            onIngressTitleChange = viewModel::setIngressTitle,
                            onIngressLyricTextChange = viewModel::setIngressLyricText,
                            onIngressSourceOriginChange = viewModel::setIngressSourceOrigin,
                            onIngressAudioIncludedChange = viewModel::setIngressAudioIncluded,
                            onIngressAudioDecoderPassChange = viewModel::setIngressAudioDecoderPass,
                            onCommitIngress = viewModel::commitSpecimenIngress,
                            onCommitHumanGovernorDisposition = viewModel::commitHumanGovernorDisposition,
                            onCommitHumanEarDisposition = viewModel::commitHumanEarDisposition,
                            onUpdateHumanEarReview = viewModel::updateHumanEarReview,
                            onIngestSafFolder = viewModel::ingestFromSafFolderUri,
                            onIngestSafDocument = viewModel::ingestFromSafDocumentUri,
                            onStartSafScan = viewModel::scanCorpusDirectoryDryRun
                        )
                    }
                    AppId.INTEGRATOR -> {
                        IntegratorApp(
                            nodes = pipelineNodes,
                            status = pipelineStatus,
                            progress = pipelineProgress,
                            executionLogs = pipelineLogs,
                            masterBundle = masterBundle,
                            onExecutePipeline = viewModel::executeIntegratorPipeline,
                            onResetPipeline = viewModel::resetPipeline,
                            onOpenLyricApp = { viewModel.openApp(AppId.LYRIC_GENERATOR) },
                            onOpenCorpusApp = { viewModel.openApp(AppId.CORPUS_CURATOR) },
                            onOpenArchiveApp = { viewModel.openApp(AppId.SPACE_ARCHIVE) }
                        )
                    }
                    AppId.ENGINE_TERMINAL -> {
                        EngineTerminalApp(
                            telemetry = telemetry,
                            auditLogs = auditLogs,
                            testbenchInput = testbenchInput,
                            testbenchResult = testbenchResult,
                            onTestbenchInputChange = viewModel::setTestbenchInput,
                            onRunTestbench = viewModel::runForensicTestbench,
                            onUpdateParameters = viewModel::updateEngineParameters
                        )
                    }
                    AppId.SPACE_ARCHIVE -> {
                        SpaceArchiveApp(
                            files = archiveFiles,
                            selectedFile = selectedArchiveFile,
                            categoryFilter = archiveCategoryFilter,
                            onCategorySelect = viewModel::setArchiveCategoryFilter,
                            onSelectFile = viewModel::selectArchiveFile,
                            onCopyContent = viewModel::copyFileContentToClipboard
                        )
                    }
                }
            }
        }

        // Start Menu Floating Pop-up (Centered above taskbar)
        StartMenu(
            isOpen = isStartMenuOpen,
            searchQuery = searchQuery,
            windows = windows,
            tenantMetrics = tenantMetrics,
            onSearchChange = viewModel::setSearchQuery,
            onLaunchApp = viewModel::openApp,
            onCascadeWindows = viewModel::cascadeWindows,
            onTileWindows = viewModel::tileWindows,
            onMinimizeAll = viewModel::minimizeAll,
            onPurgeMemory = viewModel::purgeMemory,
            onRestartOS = viewModel::restartOS,
            onClose = viewModel::toggleStartMenu,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )

        // Quick Settings / Telemetry Drawer (Bottom-Right above taskbar)
        QuickSettingsDrawer(
            isOpen = isQuickSettingsOpen,
            telemetry = telemetry,
            currentWallpaper = wallpaper,
            onSelectWallpaper = viewModel::setWallpaper,
            onRestartOS = viewModel::restartOS,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 64.dp, end = 12.dp)
        )

        // System Notification Toast Pill
        AnimatedVisibility(
            visible = systemToast != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        ) {
            systemToast?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ElyHeaderGlass)
                        .border(1.dp, ElyCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ElyCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElyTextPrimary
                        )
                    }
                }
            }
        }

        // Bottom Windows 11 Taskbar (Fixed at bottom)
        Taskbar(
            windows = windows,
            tenantMetrics = tenantMetrics,
            activeAppId = activeAppId,
            isStartMenuOpen = isStartMenuOpen,
            isQuickSettingsOpen = isQuickSettingsOpen,
            engineTelemetry = telemetry,
            onToggleStartMenu = viewModel::toggleStartMenu,
            onToggleQuickSettings = viewModel::toggleQuickSettings,
            onAppIconClick = { appId ->
                val win = windows[appId]
                if (win == null || win.isClosed) {
                    viewModel.openApp(appId)
                } else if (win.isMinimized) {
                    viewModel.focusWindow(appId)
                } else if (activeAppId == appId) {
                    viewModel.minimizeWindow(appId)
                } else {
                    viewModel.focusWindow(appId)
                }
            },
            onAppTerminate = { appId ->
                viewModel.terminateTenantProcess(appId)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DesktopWallpaperCanvas(wallpaper: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dark gradient base
        drawRect(
            brush = Brush.radialGradient(
                colors = when (wallpaper) {
                    "Cyber Matrix" -> listOf(
                        Color(0xFF064E3B).copy(alpha = 0.4f),
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                    "Deep Mica" -> listOf(
                        Color(0xFF1E1B4B).copy(alpha = 0.35f),
                        Color(0xFF0F172A),
                        Color(0xFF0B0F19)
                    )
                    else -> listOf(
                        Color(0xFF0369A1).copy(alpha = 0.3f),
                        Color(0xFF311042).copy(alpha = 0.25f),
                        Color(0xFF090D16)
                    )
                },
                center = Offset(width * 0.5f, height * 0.35f),
                radius = width * 0.9f
            )
        )

        // Subdued futuristic grid
        val step = 60f
        var x = 0f
        while (x < width) {
            drawLine(
                color = Color.White.copy(alpha = 0.02f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += step
        }
        var y = 0f
        while (y < height) {
            drawLine(
                color = Color.White.copy(alpha = 0.02f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }
}

@Composable
private fun DesktopShortcutIcon(
    appId: AppId,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
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
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = appId.defaultIcon,
                contentDescription = appId.title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = appId.shortName,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = ElyTextPrimary,
            maxLines = 1
        )
    }
}
