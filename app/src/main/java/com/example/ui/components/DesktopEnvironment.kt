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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                            onStartSafScan = viewModel::scanCorpusDirectoryDryRun,
                            archiveFiles = archiveFiles,
                            onDeleteArchiveFile = viewModel::deleteArchiveFile
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
    if (wallpaper != "Cyber Matrix" && wallpaper != "Deep Mica" && wallpaper != "Obsidian Aurora") {
        // Alpine Dawn (Elyzareth Official Landscape Wallpaper)
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val horizonY = height * 0.58f

                // 1. Sky Gradient (Luminous Azure Blue to Soft Peach Sunrise at Horizon)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2563EB), // Azure deep blue
                            Color(0xFF38BDF8), // Radiant sky blue
                            Color(0xFF7DD3FC), // Soft cyan
                            Color(0xFFBAE6FD), // Pale morning blue
                            Color(0xFFFED7AA), // Sunrise peach
                            Color(0xFFFEF08A)  // Golden dawn glow
                        ),
                        startY = 0f,
                        endY = horizonY
                    ),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(width, horizonY)
                )

                // 2. Soft Sunrise Clouds
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(width * 0.22f, horizonY * 0.75f),
                        radius = width * 0.35f
                    ),
                    radius = width * 0.35f,
                    center = Offset(width * 0.22f, horizonY * 0.75f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFED7AA).copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(width * 0.78f, horizonY * 0.72f),
                        radius = width * 0.38f
                    ),
                    radius = width * 0.38f,
                    center = Offset(width * 0.78f, horizonY * 0.72f)
                )

                // 3. Central Rising Sun Glow & Beams
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFEF08A).copy(alpha = 0.95f),
                            Color(0xFFFDBA74).copy(alpha = 0.6f),
                            Color(0xFF38BDF8).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.5f, horizonY),
                        radius = width * 0.42f
                    ),
                    radius = width * 0.42f,
                    center = Offset(width * 0.5f, horizonY)
                )

                // 4. Distant Mountain Silhouette & Snow-Capped Alpine Peaks
                // Far mountain range
                val farRange = Path().apply {
                    moveTo(0f, horizonY)
                    lineTo(0f, horizonY * 0.78f)
                    lineTo(width * 0.08f, horizonY * 0.66f)
                    lineTo(width * 0.18f, horizonY * 0.74f)
                    lineTo(width * 0.28f, horizonY * 0.60f)
                    lineTo(width * 0.38f, horizonY * 0.70f)
                    lineTo(width * 0.45f, horizonY * 0.85f)
                    lineTo(width * 0.55f, horizonY * 0.85f)
                    lineTo(width * 0.64f, horizonY * 0.68f)
                    lineTo(width * 0.75f, horizonY * 0.58f)
                    lineTo(width * 0.85f, horizonY * 0.68f)
                    lineTo(width * 0.94f, horizonY * 0.62f)
                    lineTo(width, horizonY * 0.75f)
                    lineTo(width, horizonY)
                    close()
                }
                drawPath(
                    path = farRange,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF475569), Color(0xFF64748B), Color(0xFF94A3B8)),
                        startY = horizonY * 0.55f,
                        endY = horizonY
                    )
                )

                // Snow caps on far peaks
                val snowCaps = Path().apply {
                    moveTo(width * 0.24f, horizonY * 0.65f)
                    lineTo(width * 0.28f, horizonY * 0.60f)
                    lineTo(width * 0.32f, horizonY * 0.65f)
                    close()

                    moveTo(width * 0.70f, horizonY * 0.64f)
                    lineTo(width * 0.75f, horizonY * 0.58f)
                    lineTo(width * 0.80f, horizonY * 0.64f)
                    close()

                    moveTo(width * 0.05f, horizonY * 0.70f)
                    lineTo(width * 0.08f, horizonY * 0.66f)
                    lineTo(width * 0.12f, horizonY * 0.71f)
                    close()
                }
                drawPath(snowCaps, color = Color.White.copy(alpha = 0.92f))

                // Left dramatic alpine mountain flank
                val leftFlank = Path().apply {
                    moveTo(0f, horizonY)
                    lineTo(0f, horizonY * 0.52f)
                    lineTo(width * 0.14f, horizonY * 0.68f)
                    lineTo(width * 0.25f, horizonY * 0.82f)
                    lineTo(width * 0.38f, horizonY)
                    close()
                }
                drawPath(
                    path = leftFlank,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
                        startY = horizonY * 0.50f,
                        endY = horizonY
                    )
                )
                // Left flank snow cover
                val leftSnow = Path().apply {
                    moveTo(0f, horizonY * 0.52f)
                    lineTo(width * 0.06f, horizonY * 0.58f)
                    lineTo(width * 0.12f, horizonY * 0.69f)
                    lineTo(width * 0.08f, horizonY * 0.72f)
                    lineTo(0f, horizonY * 0.65f)
                    close()
                }
                drawPath(leftSnow, color = Color.White.copy(alpha = 0.85f))

                // Right dramatic alpine mountain flank
                val rightFlank = Path().apply {
                    moveTo(width, horizonY)
                    lineTo(width, horizonY * 0.48f)
                    lineTo(width * 0.88f, horizonY * 0.62f)
                    lineTo(width * 0.76f, horizonY * 0.78f)
                    lineTo(width * 0.62f, horizonY)
                    close()
                }
                drawPath(
                    path = rightFlank,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
                        startY = horizonY * 0.45f,
                        endY = horizonY
                    )
                )
                // Right flank snow cover
                val rightSnow = Path().apply {
                    moveTo(width, horizonY * 0.48f)
                    lineTo(width * 0.92f, horizonY * 0.56f)
                    lineTo(width * 0.85f, horizonY * 0.68f)
                    lineTo(width * 0.90f, horizonY * 0.72f)
                    lineTo(width, horizonY * 0.60f)
                    close()
                }
                drawPath(rightSnow, color = Color.White.copy(alpha = 0.85f))

                // 5. Alpine Lake (Reflective Mirror Water from Horizon to Bottom)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7DD3FC), // Sky reflection at horizon
                            Color(0xFF0284C7), // Azure deep lake
                            Color(0xFF0369A1), // Alpine blue
                            Color(0xFF0F172A)  // Deep lake base near taskbar
                        ),
                        startY = horizonY,
                        endY = height
                    ),
                    topLeft = Offset(0f, horizonY),
                    size = androidx.compose.ui.geometry.Size(width, height - horizonY)
                )

                // Golden Sunrise Shimmer Column down the lake center
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color(0xFFFEF08A).copy(alpha = 0.7f),
                            Color(0xFFFDBA74).copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.5f, horizonY + (height - horizonY) * 0.35f),
                        radius = (height - horizonY) * 0.7f
                    ),
                    topLeft = Offset(width * 0.35f, horizonY),
                    size = androidx.compose.ui.geometry.Size(width * 0.30f, height - horizonY)
                )

                // Water ripple texture lines
                for (i in 1..8) {
                    val rippleY = horizonY + (height - horizonY) * (i * 0.11f)
                    val rippleWidth = width * (0.25f + i * 0.08f)
                    val rippleStart = (width - rippleWidth) / 2f
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f - (i * 0.02f)),
                        start = Offset(rippleStart, rippleY),
                        end = Offset(rippleStart + rippleWidth, rippleY),
                        strokeWidth = 1.2f
                    )
                }

                // 6. Foreground Rocky Shorelines & Evergreen Pine Forest Silhouettes
                // Left Shore & Pines
                val leftShore = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, horizonY + (height - horizonY) * 0.15f)
                    lineTo(width * 0.18f, horizonY + (height - horizonY) * 0.35f)
                    lineTo(width * 0.24f, horizonY + (height - horizonY) * 0.65f)
                    lineTo(width * 0.12f, height)
                    close()
                }
                drawPath(
                    path = leftShore,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF064E3B))
                    )
                )

                // Left pine trees
                listOf(
                    Pair(width * 0.04f, horizonY + (height - horizonY) * 0.12f),
                    Pair(width * 0.09f, horizonY + (height - horizonY) * 0.20f),
                    Pair(width * 0.14f, horizonY + (height - horizonY) * 0.28f),
                    Pair(width * 0.19f, horizonY + (height - horizonY) * 0.42f)
                ).forEach { (px, py) ->
                    val treePath = Path().apply {
                        moveTo(px, py - 32f)
                        lineTo(px + 12f, py)
                        lineTo(px - 12f, py)
                        close()

                        moveTo(px, py - 20f)
                        lineTo(px + 16f, py + 16f)
                        lineTo(px - 16f, py + 16f)
                        close()
                    }
                    drawPath(treePath, color = Color(0xFF0F172A))
                }

                // Right Shore & Pines
                val rightShore = Path().apply {
                    moveTo(width, height)
                    lineTo(width, horizonY + (height - horizonY) * 0.25f)
                    lineTo(width * 0.82f, horizonY + (height - horizonY) * 0.50f)
                    lineTo(width * 0.88f, height)
                    close()
                }
                drawPath(
                    path = rightShore,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF064E3B))
                    )
                )

                // Right pine trees
                listOf(
                    Pair(width * 0.86f, horizonY + (height - horizonY) * 0.38f),
                    Pair(width * 0.92f, horizonY + (height - horizonY) * 0.24f),
                    Pair(width * 0.96f, horizonY + (height - horizonY) * 0.18f)
                ).forEach { (px, py) ->
                    val treePath = Path().apply {
                        moveTo(px, py - 30f)
                        lineTo(px + 12f, py)
                        lineTo(px - 12f, py)
                        close()

                        moveTo(px, py - 18f)
                        lineTo(px + 15f, py + 14f)
                        lineTo(px - 15f, py + 14f)
                        close()
                    }
                    drawPath(treePath, color = Color(0xFF0F172A))
                }

                // 7. Celestial Star Compass Emblem & Glowing Flares in the Upper Sky
                val emblemCenter = Offset(width * 0.5f, height * 0.165f)

                // Soft background radial aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFF38BDF8).copy(alpha = 0.55f),
                            Color(0xFF0284C7).copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = emblemCenter,
                        radius = 110f
                    ),
                    radius = 110f,
                    center = emblemCenter
                )

                // Outer Celestial Ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.88f),
                    radius = 70f,
                    center = emblemCenter,
                    style = Stroke(width = 2.2f)
                )
                // Inner Celestial Ring
                drawCircle(
                    color = Color(0xFFBAE6FD).copy(alpha = 0.75f),
                    radius = 52f,
                    center = emblemCenter,
                    style = Stroke(width = 1.4f)
                )

                // North-South Primary Vertical Flare Diamond
                val verticalRay = Path().apply {
                    moveTo(emblemCenter.x, emblemCenter.y - 105f) // Top tip
                    lineTo(emblemCenter.x + 10f, emblemCenter.y)
                    lineTo(emblemCenter.x, emblemCenter.y + 115f) // Bottom tip
                    lineTo(emblemCenter.x - 10f, emblemCenter.y)
                    close()
                }
                drawPath(verticalRay, color = Color.White.copy(alpha = 0.95f))

                // East-West Primary Horizontal Flare Diamond
                val horizontalRay = Path().apply {
                    moveTo(emblemCenter.x - 90f, emblemCenter.y) // Left tip
                    lineTo(emblemCenter.x, emblemCenter.y - 8f)
                    lineTo(emblemCenter.x + 90f, emblemCenter.y) // Right tip
                    lineTo(emblemCenter.x, emblemCenter.y + 8f)
                    close()
                }
                drawPath(horizontalRay, color = Color.White.copy(alpha = 0.95f))

                // Diagonal Star Diamond Rays
                val diagRays = Path().apply {
                    // Top-Left to Bottom-Right
                    moveTo(emblemCenter.x - 48f, emblemCenter.y - 48f)
                    lineTo(emblemCenter.x + 5f, emblemCenter.y - 5f)
                    lineTo(emblemCenter.x + 48f, emblemCenter.y + 48f)
                    lineTo(emblemCenter.x - 5f, emblemCenter.y + 5f)
                    close()

                    // Top-Right to Bottom-Left
                    moveTo(emblemCenter.x + 48f, emblemCenter.y - 48f)
                    lineTo(emblemCenter.x + 5f, emblemCenter.y + 5f)
                    lineTo(emblemCenter.x - 48f, emblemCenter.y + 48f)
                    lineTo(emblemCenter.x - 5f, emblemCenter.y - 5f)
                    close()
                }
                drawPath(diagRays, color = Color(0xFFBAE6FD).copy(alpha = 0.9f))

                // Wing Arcs on Compass Sides
                val leftArc = Path().apply {
                    moveTo(emblemCenter.x - 85f, emblemCenter.y)
                    quadraticBezierTo(
                        emblemCenter.x - 40f, emblemCenter.y + 35f,
                        emblemCenter.x, emblemCenter.y + 75f
                    )
                }
                drawPath(leftArc, color = Color.White.copy(alpha = 0.85f), style = Stroke(width = 2f))

                val rightArc = Path().apply {
                    moveTo(emblemCenter.x + 85f, emblemCenter.y)
                    quadraticBezierTo(
                        emblemCenter.x + 40f, emblemCenter.y + 35f,
                        emblemCenter.x, emblemCenter.y + 75f
                    )
                }
                drawPath(rightArc, color = Color.White.copy(alpha = 0.85f), style = Stroke(width = 2f))

                // Radiant Center Star Core
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = emblemCenter
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFF38BDF8), Color.Transparent),
                        center = emblemCenter,
                        radius = 24f
                    ),
                    radius = 24f,
                    center = emblemCenter
                )
            }

            // 8. Wallpaper Typography Overlay (Pure Elyzareth Identity)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 188.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "E L Y Z A R E T H",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    letterSpacing = 7.sp,
                    textAlign = TextAlign.Center
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.dp)
                            .background(Color(0xFF0284C7).copy(alpha = 0.6f))
                    )
                    Text(
                        text = "OS",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0284C7),
                        letterSpacing = 3.sp
                    )
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.dp)
                            .background(Color(0xFF0284C7).copy(alpha = 0.6f))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "CREATE  •  CURATE  •  INTEGRATE  •  GOVERN  •  ARCHIVE",
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B).copy(alpha = 0.88f),
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Dark / Ambient Wallpapers
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
}
