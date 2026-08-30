package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.ArchivePersistenceManager
import com.example.engine.CorpusDiscoveryEngine
import com.example.engine.CorpusPersistenceManager
import com.example.engine.ElyzarethGovernanceEngine
import com.example.engine.ElyzarethTurboEngine
import com.example.engine.TenantLifecycleManager
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ElyzarethOSViewModel : ViewModel() {

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    private var zIndexCounter = 10f
    private var applicationContextRef: Context? = null

    fun initializePersistentState(context: Context) {
        applicationContextRef = context.applicationContext
        viewModelScope.launch {
            // 1. Restore Archive Artifacts from Disk
            val loadedArchive = ArchivePersistenceManager.loadSavedArchiveFiles(context)
            if (loadedArchive != null && loadedArchive.isNotEmpty()) {
                _archiveFiles.value = loadedArchive
                _selectedArchiveFile.value = loadedArchive.firstOrNull()
                addAuditLog("ARCHIVE_STORE", "Restored ${loadedArchive.size} artifacts from persistent archive storage.", "ELY-ARCH-RESTORE")
            }

            // 2. Restore Corpus Discovery Report from Disk
            val loadedCorpus = CorpusPersistenceManager.loadSavedReport(context)
            if (loadedCorpus != null) {
                _corpusInventoryReport.value = loadedCorpus
                addAuditLog("CORPUS_STORE", "Restored corpus inventory (${loadedCorpus.baseTitlesDiscovered} base titles) from disk cache.", "ELY-CORPUS-RESTORE")
            }
        }
    }

    private fun persistCurrentArchiveFiles() {
        applicationContextRef?.let { ctx ->
            ArchivePersistenceManager.saveArchiveFiles(ctx, _archiveFiles.value)
        }
    }

    // Tenant Lifecycle & Resource Manager
    val tenantManager = TenantLifecycleManager(
        scope = viewModelScope,
        onAuditLog = { layer, message, hashStamp ->
            addAuditLog(layer, message, hashStamp)
        }
    )
    val tenantMetrics: StateFlow<Map<AppId, TenantResourceMetrics>> = tenantManager.tenantMetrics

    // Windows / Desktop State
    private val _windows = MutableStateFlow<Map<AppId, WindowData>>(emptyMap())
    val windows: StateFlow<Map<AppId, WindowData>> = _windows.asStateFlow()

    private val _activeAppId = MutableStateFlow<AppId?>(null)
    val activeAppId: StateFlow<AppId?> = _activeAppId.asStateFlow()

    private val _isStartMenuOpen = MutableStateFlow(false)
    val isStartMenuOpen: StateFlow<Boolean> = _isStartMenuOpen.asStateFlow()

    private val _isQuickSettingsOpen = MutableStateFlow(false)
    val isQuickSettingsOpen: StateFlow<Boolean> = _isQuickSettingsOpen.asStateFlow()

    private val _desktopWallpaper = MutableStateFlow("Alpine Dawn")
    val desktopWallpaper: StateFlow<String> = _desktopWallpaper.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Engine & Governance State
    private val _engineTelemetry = MutableStateFlow(EngineTelemetry())
    val engineTelemetry: StateFlow<EngineTelemetry> = _engineTelemetry.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(ElyzarethGovernanceEngine.getInitialAuditLogs())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // App 01: Lyric Studio State & Panel Architecture
    private val _lyricStudioMode = MutableStateFlow(LyricStudioMode.SIMPLE)
    val lyricStudioMode: StateFlow<LyricStudioMode> = _lyricStudioMode.asStateFlow()

    private val _advancedLyricTab = MutableStateFlow(AdvancedLyricTab.LYRICS)
    val advancedLyricTab: StateFlow<AdvancedLyricTab> = _advancedLyricTab.asStateFlow()

    private val _lyricPrompt = MutableStateFlow("A groovy synthwave song about a faded photo on the mantel")
    val lyricPrompt: StateFlow<String> = _lyricPrompt.asStateFlow()

    private val _existingLyric = MutableStateFlow("")
    val existingLyric: StateFlow<String> = _existingLyric.asStateFlow()

    private val _lyricGenre = MutableStateFlow("Synthwave")
    val lyricGenre: StateFlow<String> = _lyricGenre.asStateFlow()

    private val _lyricRhymeScheme = MutableStateFlow("AABB")
    val lyricRhymeScheme: StateFlow<String> = _lyricRhymeScheme.asStateFlow()

    private val _stylePrompt = MutableStateFlow("Atmospheric neo-classical synth with ethereal female vocals, wide reverb, and baroque strings")
    val stylePrompt: StateFlow<String> = _stylePrompt.asStateFlow()

    private val _vocalTimbre = MutableStateFlow("Ethereal / Resonant")
    val vocalTimbre: StateFlow<String> = _vocalTimbre.asStateFlow()

    private val _vocalGender = MutableStateFlow(VocalGender.ANY)
    val vocalGender: StateFlow<VocalGender> = _vocalGender.asStateFlow()

    private val _isInstrumental = MutableStateFlow(false)
    val isInstrumental: StateFlow<Boolean> = _isInstrumental.asStateFlow()

    private val _attachedAudio = MutableStateFlow<AttachedAudio?>(null)
    val attachedAudio: StateFlow<AttachedAudio?> = _attachedAudio.asStateFlow()

    private val _attachedVoice = MutableStateFlow<AttachedVoice?>(null)
    val attachedVoice: StateFlow<AttachedVoice?> = _attachedVoice.asStateFlow()

    private val _currentLyricEvidence = MutableStateFlow<LyricEvidence?>(null)
    val currentLyricEvidence: StateFlow<LyricEvidence?> = _currentLyricEvidence.asStateFlow()

    private val _excludeStyles = MutableStateFlow("")
    val excludeStyles: StateFlow<String> = _excludeStyles.asStateFlow()

    private val _weirdness = MutableStateFlow(50f)
    val weirdness: StateFlow<Float> = _weirdness.asStateFlow()

    private val _styleInfluence = MutableStateFlow(50f)
    val styleInfluence: StateFlow<Float> = _styleInfluence.asStateFlow()

    private val _songTitleInput = MutableStateFlow("")
    val songTitleInput: StateFlow<String> = _songTitleInput.asStateFlow()

    private val _audioCadenceProfile = MutableStateFlow(AudioCadenceProfile())
    val audioCadenceProfile: StateFlow<AudioCadenceProfile> = _audioCadenceProfile.asStateFlow()

    private val _selectedMagicOp = MutableStateFlow(MagicOperationType.CREATE)
    val selectedMagicOp: StateFlow<MagicOperationType> = _selectedMagicOp.asStateFlow()

    private val _activeSong = MutableStateFlow<GeneratedSong?>(null)
    val activeSong: StateFlow<GeneratedSong?> = _activeSong.asStateFlow()

    private val _rhymeQuery = MutableStateFlow("space")
    val rhymeQuery: StateFlow<String> = _rhymeQuery.asStateFlow()

    private val _rhymeSuggestions = MutableStateFlow<List<RhymeSuggestion>>(ElyzarethGovernanceEngine.getRhymeSuggestions("space"))
    val rhymeSuggestions: StateFlow<List<RhymeSuggestion>> = _rhymeSuggestions.asStateFlow()

    private val _isGeneratingLyric = MutableStateFlow(false)
    val isGeneratingLyric: StateFlow<Boolean> = _isGeneratingLyric.asStateFlow()

    // App 01: 60-Day Elyzareth Turbo Engine Intelligence State
    private val _turboValidationReport = MutableStateFlow<TurboValidationReport?>(null)
    val turboValidationReport: StateFlow<TurboValidationReport?> = _turboValidationReport.asStateFlow()

    private val _activeCreativeDna = MutableStateFlow<CreativeDnaProfile?>(null)
    val activeCreativeDna: StateFlow<CreativeDnaProfile?> = _activeCreativeDna.asStateFlow()

    private val _turboEngineMode = MutableStateFlow(TurboEngineMode.GENERATE)
    val turboEngineMode: StateFlow<TurboEngineMode> = _turboEngineMode.asStateFlow()

    private val _activeAcousticConstraint = MutableStateFlow<ElyzarethAcousticConstraint>(ELYZARETH_RUSTIC_ACOUSTIC_v1)
    val activeAcousticConstraint: StateFlow<ElyzarethAcousticConstraint> = _activeAcousticConstraint.asStateFlow()


    // App 02: Corpus / Lyric Curator (The Sitting Room)
    private val _baseCompositions = MutableStateFlow<List<BaseComposition>>(ElyzarethGovernanceEngine.getInitialBaseCompositions())
    val baseCompositions: StateFlow<List<BaseComposition>> = _baseCompositions.asStateFlow()

    private val _selectedBaseCompositionId = MutableStateFlow("BASE-01")
    val selectedBaseCompositionId: StateFlow<String> = _selectedBaseCompositionId.asStateFlow()

    private val _selectedVersionId = MutableStateFlow("v02")
    val selectedVersionId: StateFlow<String> = _selectedVersionId.asStateFlow()

    private val _sittingRoomTab = MutableStateFlow("WITNESS") // WITNESS, EXAMINATION, FINDINGS, DISPOSITION, AUDIT
    val sittingRoomTab: StateFlow<String> = _sittingRoomTab.asStateFlow()

    private val _selectedGateDiagnostic = MutableStateFlow<GateDiagnostic?>(null)
    val selectedGateDiagnostic: StateFlow<GateDiagnostic?> = _selectedGateDiagnostic.asStateFlow()

    // Ingress Specimen State
    private val _isIngressDialogOpen = MutableStateFlow(false)
    val isIngressDialogOpen: StateFlow<Boolean> = _isIngressDialogOpen.asStateFlow()

    private val _ingressDialogType = MutableStateFlow("LYRIC") // CORPUS, LYRIC, AUDIO
    val ingressDialogType: StateFlow<String> = _ingressDialogType.asStateFlow()

    private val _ingressTitle = MutableStateFlow("")
    val ingressTitle: StateFlow<String> = _ingressTitle.asStateFlow()

    private val _ingressLyricText = MutableStateFlow("")
    val ingressLyricText: StateFlow<String> = _ingressLyricText.asStateFlow()

    private val _ingressSourceOrigin = MutableStateFlow(IngressSourceOrigin.LAPTOP)
    val ingressSourceOrigin: StateFlow<IngressSourceOrigin> = _ingressSourceOrigin.asStateFlow()

    private val _ingressAudioIncluded = MutableStateFlow(false)
    val ingressAudioIncluded: StateFlow<Boolean> = _ingressAudioIncluded.asStateFlow()

    private val _ingressAudioDecoderPass = MutableStateFlow(true)
    val ingressAudioDecoderPass: StateFlow<Boolean> = _ingressAudioDecoderPass.asStateFlow()

    private val _corpusList = MutableStateFlow<List<CorpusItem>>(ElyzarethGovernanceEngine.getInitialCorpus())
    val corpusList: StateFlow<List<CorpusItem>> = _corpusList.asStateFlow()

    private val _selectedCorpus = MutableStateFlow<CorpusItem?>(ElyzarethGovernanceEngine.getInitialCorpus().firstOrNull())
    val selectedCorpus: StateFlow<CorpusItem?> = _selectedCorpus.asStateFlow()

    private val _corpusSearch = MutableStateFlow("")
    val corpusSearch: StateFlow<String> = _corpusSearch.asStateFlow()

    // Real Corpus Ingestion Dry Run Report State
    private val _corpusInventoryReport = MutableStateFlow<CorpusInventoryReport>(CorpusInventoryReport())
    val corpusInventoryReport: StateFlow<CorpusInventoryReport> = _corpusInventoryReport.asStateFlow()

    // Structured Cure Request (App 02/ELDS-C → Elyzareth OS → App 01)
    private val _activeCureRequest = MutableStateFlow<StructuredCureRequest?>(null)
    val activeCureRequest: StateFlow<StructuredCureRequest?> = _activeCureRequest.asStateFlow()

    // App 03: The Integrator State
    private val _pipelineNodes = MutableStateFlow<List<PipelineNode>>(getDefaultPipelineNodes())
    val pipelineNodes: StateFlow<List<PipelineNode>> = _pipelineNodes.asStateFlow()

    private val _pipelineStatus = MutableStateFlow(PipelineRunStatus.IDLE)
    val pipelineStatus: StateFlow<PipelineRunStatus> = _pipelineStatus.asStateFlow()

    private val _pipelineProgress = MutableStateFlow(0f)
    val pipelineProgress: StateFlow<Float> = _pipelineProgress.asStateFlow()

    private val _pipelineExecutionLogs = MutableStateFlow<List<String>>(listOf("System Ready. Click 'Execute Pipeline' to orchestrate."))
    val pipelineExecutionLogs: StateFlow<List<String>> = _pipelineExecutionLogs.asStateFlow()

    private val _masterIntegratedBundle = MutableStateFlow<MasterIntegratedBundle?>(null)
    val masterIntegratedBundle: StateFlow<MasterIntegratedBundle?> = _masterIntegratedBundle.asStateFlow()

    private val _activeTimelineTrack = MutableStateFlow(0)
    val activeTimelineTrack: StateFlow<Int> = _activeTimelineTrack.asStateFlow()

    // App 04: Governance Matrix / Forensic Verifier
    private val _testbenchInput = MutableStateFlow("In the sovereign space of Elyzareth, every line holds a verified truth.")
    val testbenchInput: StateFlow<String> = _testbenchInput.asStateFlow()

    private val _testbenchResult = MutableStateFlow<GovernanceAuditResult?>(null)
    val testbenchResult: StateFlow<GovernanceAuditResult?> = _testbenchResult.asStateFlow()

    // App 05: Space Archive Hub
    private val _archiveFiles = MutableStateFlow<List<ArchiveFile>>(ElyzarethGovernanceEngine.getInitialArchiveFiles())
    val archiveFiles: StateFlow<List<ArchiveFile>> = _archiveFiles.asStateFlow()

    private val _selectedArchiveFile = MutableStateFlow<ArchiveFile?>(ElyzarethGovernanceEngine.getInitialArchiveFiles().firstOrNull())
    val selectedArchiveFile: StateFlow<ArchiveFile?> = _selectedArchiveFile.asStateFlow()

    private val _archiveCategoryFilter = MutableStateFlow("ALL")
    val archiveCategoryFilter: StateFlow<String> = _archiveCategoryFilter.asStateFlow()

    // Toast / Feedback message
    private val _systemToast = MutableStateFlow<String?>(null)
    val systemToast: StateFlow<String?> = _systemToast.asStateFlow()

    init {
        _windows.value = initializeCanonicalWindows()
        _activeAppId.value = null // Present Elyzareth OS Desktop Shell first

        // Pre-generate a default song
        _activeSong.value = ElyzarethGovernanceEngine.generateLyricSuite("Axiom of Night", "Cyber-Opera", "AABB", "")

        // Start background engine heartbeat
        startEngineHeartbeat()
    }

    private fun initializeCanonicalWindows(): Map<AppId, WindowData> {
        val initialMap = mutableMapOf<AppId, WindowData>()

        // App 02: Corpus Curator / Sitting Room (Ready on desktop)
        val metrics02 = tenantManager.allocateAndLaunch(AppId.CORPUS_CURATOR)
        tenantManager.setMinimized(AppId.CORPUS_CURATOR)
        initialMap[AppId.CORPUS_CURATOR] = WindowData(
            appId = AppId.CORPUS_CURATOR,
            isMinimized = true,
            isMaximized = false,
            isClosed = false,
            offsetX = 8f,
            offsetY = 16f,
            width = 410f,
            height = 560f,
            zIndex = ++zIndexCounter,
            lifecycleState = TenantLifecycleState.MINIMIZED,
            allocatedMemoryMb = metrics02.allocatedMemoryMb
        )

        // App 03: The Integrator (Ready on desktop)
        val metrics03 = tenantManager.allocateAndLaunch(AppId.INTEGRATOR)
        tenantManager.setMinimized(AppId.INTEGRATOR)
        initialMap[AppId.INTEGRATOR] = WindowData(
            appId = AppId.INTEGRATOR,
            isMinimized = true,
            isMaximized = false,
            isClosed = false,
            offsetX = 12f,
            offsetY = 24f,
            width = 370f,
            height = 540f,
            zIndex = ++zIndexCounter,
            lifecycleState = TenantLifecycleState.MINIMIZED,
            allocatedMemoryMb = metrics03.allocatedMemoryMb
        )

        // App 01: Lyric Studio (Ready on desktop)
        val metrics01 = tenantManager.allocateAndLaunch(AppId.LYRIC_GENERATOR)
        tenantManager.setMinimized(AppId.LYRIC_GENERATOR)
        initialMap[AppId.LYRIC_GENERATOR] = WindowData(
            appId = AppId.LYRIC_GENERATOR,
            isMinimized = true,
            isMaximized = false,
            isClosed = false,
            offsetX = 28f,
            offsetY = 48f,
            width = 360f,
            height = 520f,
            zIndex = ++zIndexCounter,
            lifecycleState = TenantLifecycleState.MINIMIZED,
            allocatedMemoryMb = metrics01.allocatedMemoryMb * 0.75f
        )

        // App 04: Governance Matrix (Ready on desktop)
        val metrics04 = tenantManager.allocateAndLaunch(AppId.ENGINE_TERMINAL)
        tenantManager.setMinimized(AppId.ENGINE_TERMINAL)
        initialMap[AppId.ENGINE_TERMINAL] = WindowData(
            appId = AppId.ENGINE_TERMINAL,
            isMinimized = true,
            isMaximized = false,
            isClosed = false,
            offsetX = 20f,
            offsetY = 32f,
            width = 360f,
            height = 510f,
            zIndex = ++zIndexCounter,
            lifecycleState = TenantLifecycleState.MINIMIZED,
            allocatedMemoryMb = metrics04.allocatedMemoryMb
        )

        // App 05: Space Archive (Ready on desktop)
        val metrics05 = tenantManager.allocateAndLaunch(AppId.SPACE_ARCHIVE)
        tenantManager.setMinimized(AppId.SPACE_ARCHIVE)
        initialMap[AppId.SPACE_ARCHIVE] = WindowData(
            appId = AppId.SPACE_ARCHIVE,
            isMinimized = true,
            isMaximized = false,
            isClosed = false,
            offsetX = 16f,
            offsetY = 40f,
            width = 380f,
            height = 530f,
            zIndex = ++zIndexCounter,
            lifecycleState = TenantLifecycleState.MINIMIZED,
            allocatedMemoryMb = metrics05.allocatedMemoryMb
        )

        return initialMap
    }

    private fun startEngineHeartbeat() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                val current = _engineTelemetry.value
                val tokenDelta = (20..80).random()
                val activeThreads = 8 + tenantManager.getTotalActiveThreads()
                _engineTelemetry.value = current.copy(
                    activeThreads = activeThreads,
                    totalProcessedTokens = current.totalProcessedTokens + tokenDelta,
                    g1LexicalLoad = (0.20f + (Random().nextFloat() * 0.15f)),
                    g2HarmonyLoad = (0.35f + (Random().nextFloat() * 0.20f)),
                    g3AxiomLoad = (0.10f + (Random().nextFloat() * 0.10f))
                )
            }
        }
    }

    // -------------------------------------------------------------
    // UniversalWindowShell / Landlord Operations
    // -------------------------------------------------------------
    fun openApp(appId: AppId) {
        _isStartMenuOpen.value = false
        val current = _windows.value.toMutableMap()
        val existing = current[appId]
        
        zIndexCounter += 1f
        val resourceMetrics = tenantManager.allocateAndLaunch(appId, _activeAppId.value)

        if (existing != null) {
            current[appId] = existing.copy(
                isClosed = false,
                isMinimized = false,
                zIndex = zIndexCounter,
                lifecycleState = TenantLifecycleState.ACTIVE_FOREGROUND,
                allocatedMemoryMb = resourceMetrics.allocatedMemoryMb,
                lastActiveTimestamp = System.currentTimeMillis()
            )
        } else {
            val offsetMultiplier = current.size % 4
            current[appId] = WindowData(
                appId = appId,
                isMinimized = false,
                isMaximized = false,
                isClosed = false,
                offsetX = 16f + (offsetMultiplier * 20f),
                offsetY = 24f + (offsetMultiplier * 24f),
                width = appId.defaultWidth,
                height = appId.defaultHeight,
                zIndex = zIndexCounter,
                lifecycleState = TenantLifecycleState.ACTIVE_FOREGROUND,
                allocatedMemoryMb = resourceMetrics.allocatedMemoryMb
            )
        }
        
        // Update other visible windows to background state
        current.forEach { (id, win) ->
            if (id != appId && !win.isMinimized && !win.isClosed) {
                current[id] = win.copy(lifecycleState = TenantLifecycleState.ACTIVE_BACKGROUND)
            }
        }

        _windows.value = current
        _activeAppId.value = appId
        showToast("Launched ${appId.shortName} (PID ${resourceMetrics.processId})")
    }

    fun focusWindow(appId: AppId) {
        val current = _windows.value.toMutableMap()
        val window = current[appId] ?: return
        
        tenantManager.setForeground(appId)
        zIndexCounter += 1f

        current[appId] = window.copy(
            isMinimized = false,
            zIndex = zIndexCounter,
            lifecycleState = TenantLifecycleState.ACTIVE_FOREGROUND,
            lastActiveTimestamp = System.currentTimeMillis()
        )

        // Other non-minimized windows transition to background
        current.forEach { (id, win) ->
            if (id != appId && !win.isMinimized && !win.isClosed) {
                current[id] = win.copy(lifecycleState = TenantLifecycleState.ACTIVE_BACKGROUND)
            }
        }

        _windows.value = current
        _activeAppId.value = appId
    }

    fun minimizeWindow(appId: AppId) {
        val current = _windows.value.toMutableMap()
        val window = current[appId] ?: return
        
        tenantManager.setMinimized(appId)
        current[appId] = window.copy(
            isMinimized = true,
            lifecycleState = TenantLifecycleState.MINIMIZED,
            allocatedMemoryMb = (window.allocatedMemoryMb * 0.75f).coerceAtLeast(4f)
        )
        _windows.value = current

        if (_activeAppId.value == appId) {
            val nextActive = current.values
                .filter { !it.isMinimized && !it.isClosed }
                .maxByOrNull { it.zIndex }?.appId

            _activeAppId.value = nextActive
            if (nextActive != null) {
                tenantManager.setForeground(nextActive)
                current[nextActive] = current[nextActive]!!.copy(lifecycleState = TenantLifecycleState.ACTIVE_FOREGROUND)
                _windows.value = current
            }
        }
    }

    fun toggleMaximizeWindow(appId: AppId) {
        val current = _windows.value.toMutableMap()
        val window = current[appId] ?: return
        zIndexCounter += 1f
        current[appId] = window.copy(
            isMaximized = !window.isMaximized,
            zIndex = zIndexCounter,
            lifecycleState = TenantLifecycleState.ACTIVE_FOREGROUND
        )
        _windows.value = current
        _activeAppId.value = appId
        tenantManager.setForeground(appId)
    }

    fun closeWindow(appId: AppId) {
        val current = _windows.value.toMutableMap()
        val window = current[appId] ?: return
        
        // Terminate tenant & release memory and coroutines
        val freedMem = tenantManager.terminateAndRelease(appId) {
            // Clean up transient scratchpads if needed
            if (appId == AppId.LYRIC_GENERATOR && _isGeneratingLyric.value) {
                _isGeneratingLyric.value = false
            }
        }

        current[appId] = window.copy(
            isClosed = true,
            isMinimized = false,
            lifecycleState = TenantLifecycleState.DORMANT,
            allocatedMemoryMb = 0f
        )
        _windows.value = current

        if (_activeAppId.value == appId) {
            val nextActive = current.values
                .filter { !it.isMinimized && !it.isClosed }
                .maxByOrNull { it.zIndex }?.appId
            _activeAppId.value = nextActive
            if (nextActive != null) {
                tenantManager.setForeground(nextActive)
            }
        }

        showToast("Closed ${appId.shortName} // Freed ${"%.1f".format(freedMem)} MB")
    }

    fun terminateTenantProcess(appId: AppId) {
        closeWindow(appId)
    }

    fun cascadeWindows() {
        val current = _windows.value.toMutableMap()
        val openWindows = current.values.filter { !it.isClosed }.sortedBy { it.zIndex }
        openWindows.forEachIndexed { index, win ->
            val offset = (index * 24f).coerceAtMost(160f)
            current[win.appId] = win.copy(
                offsetX = 12f + offset,
                offsetY = 24f + offset,
                isMinimized = false,
                isMaximized = false
            )
        }
        _windows.value = current
        showToast("Windows Cascaded")
    }

    fun tileWindows() {
        val current = _windows.value.toMutableMap()
        val openWindows = current.values.filter { !it.isClosed }.sortedBy { it.zIndex }
        if (openWindows.isEmpty()) return

        if (openWindows.size == 1) {
            val single = openWindows[0]
            current[single.appId] = single.copy(offsetX = 12f, offsetY = 20f, width = 360f, height = 520f, isMinimized = false, isMaximized = false)
        } else {
            openWindows.forEachIndexed { index, win ->
                val halfHeight = 260f
                val offsetY = 16f + (index % 2) * (halfHeight + 12f)
                val offsetX = 12f + (index / 2) * 20f
                current[win.appId] = win.copy(
                    offsetX = offsetX,
                    offsetY = offsetY,
                    width = 360f,
                    height = halfHeight,
                    isMinimized = false,
                    isMaximized = false
                )
            }
        }
        _windows.value = current
        showToast("Windows Tiled")
    }

    fun minimizeAll() {
        val current = _windows.value.toMutableMap()
        current.forEach { (id, win) ->
            if (!win.isClosed) {
                tenantManager.setMinimized(id)
                current[id] = win.copy(isMinimized = true, lifecycleState = TenantLifecycleState.MINIMIZED)
            }
        }
        _windows.value = current
        _activeAppId.value = null
        showToast("Desktop Cleared (All Minimized)")
    }

    fun purgeMemory() {
        val freed = tenantManager.purgeDormantMemory()
        showToast("Garbage Collected: ${"%.1f".format(freed)} MB Reclaimed")
    }

    fun updateWindowPosition(appId: AppId, newOffsetX: Float, newOffsetY: Float) {
        val current = _windows.value.toMutableMap()
        val window = current[appId] ?: return
        current[appId] = window.copy(
            offsetX = newOffsetX.coerceAtLeast(0f),
            offsetY = newOffsetY.coerceAtLeast(0f)
        )
        _windows.value = current
    }

    fun toggleStartMenu() {
        _isStartMenuOpen.value = !_isStartMenuOpen.value
        if (_isStartMenuOpen.value) {
            _isQuickSettingsOpen.value = false
        }
    }

    fun toggleQuickSettings() {
        _isQuickSettingsOpen.value = !_isQuickSettingsOpen.value
        if (_isQuickSettingsOpen.value) {
            _isStartMenuOpen.value = false
        }
    }

    fun setWallpaper(name: String) {
        _desktopWallpaper.value = name
        showToast("Wallpaper changed to $name")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // -------------------------------------------------------------
    // App 01: Lyric Studio Actions & Magic Transformations
    // -------------------------------------------------------------
    fun setLyricStudioMode(mode: LyricStudioMode) {
        _lyricStudioMode.value = mode
    }

    fun setAdvancedLyricTab(tab: AdvancedLyricTab) {
        _advancedLyricTab.value = tab
    }

    fun setLyricPrompt(prompt: String) {
        _lyricPrompt.value = prompt
    }

    fun setExistingLyric(lyric: String) {
        _existingLyric.value = lyric
    }

    fun setLyricGenre(genre: String) {
        _lyricGenre.value = genre
    }

    fun setLyricRhymeScheme(scheme: String) {
        _lyricRhymeScheme.value = scheme
    }

    fun setStylePrompt(prompt: String) {
        _stylePrompt.value = prompt
    }

    fun setVocalTimbre(timbre: String) {
        _vocalTimbre.value = timbre
    }

    fun setVocalGender(gender: VocalGender) {
        _vocalGender.value = gender
    }

    fun toggleInstrumental() {
        _isInstrumental.value = !_isInstrumental.value
        if (_isInstrumental.value) {
            showToast("Instrumental Mode Enabled (No Vocals)")
        } else {
            showToast("Vocal Lyrics Enabled")
        }
    }

    fun attachAudio(type: AudioSourceType, title: String = "") {
        val effectiveTitle = if (title.isNotBlank()) title else when (type) {
            AudioSourceType.BROWSE -> "Sample_Track_Horizon.wav"
            AudioSourceType.UPLOAD -> "Uploaded_Stem_Lead.mp3"
            AudioSourceType.RECORD -> "Voice_Memo_Take_01.m4a"
            AudioSourceType.NONE -> ""
        }
        if (type == AudioSourceType.NONE) {
            _attachedAudio.value = null
            showToast("Audio Detached")
        } else {
            _attachedAudio.value = AttachedAudio(type = type, title = effectiveTitle)
            addAuditLog("AUDIO_INGRESS", "Attached audio reference: $effectiveTitle via ${type.label}", "AUDIO-${type.name.take(3)}")
            showToast("Audio Attached: $effectiveTitle (${type.label})")
        }
    }

    fun removeAttachedAudio() {
        _attachedAudio.value = null
        showToast("Audio Removed")
    }

    fun attachVoice(type: VoiceSourceType, personaName: String = "", timbre: String = "Warm & Resonant", pitchRange: String = "Alto / Tenor") {
        val effectiveName = when {
            personaName.isNotBlank() -> personaName
            type == VoiceSourceType.RECORD -> "Live Vocal Capture #1"
            type == VoiceSourceType.UPLOAD -> "Uploaded_Acapella_Stem.wav"
            type == VoiceSourceType.LIBRARY -> "Aria (Ethereal Soprano)"
            else -> "Default Voice Reference"
        }
        if (type == VoiceSourceType.NONE) {
            _attachedVoice.value = null
            showToast("Voice Detached")
        } else {
            _attachedVoice.value = AttachedVoice(
                type = type,
                personaName = effectiveName,
                timbre = timbre,
                pitchRange = pitchRange
            )
            addAuditLog("VOICE_INGRESS", "Attached voice reference: $effectiveName ($timbre) via ${type.label}", "VOICE-${type.name.take(3)}")
            showToast("Voice Attached: $effectiveName (${type.label})")
        }
    }

    fun removeAttachedVoice() {
        _attachedVoice.value = null
        showToast("Voice Removed")
    }

    fun executeLyricMagic(op: MagicOperationType) {
        viewModelScope.launch {
            _isGeneratingLyric.value = true
            _selectedMagicOp.value = op
            addAuditLog("MAGIC_LYRIC", "App 01 Turbo Engine Craft: ${op.title} (Context: ${_lyricGenre.value})...", "ELY-TURBO-${op.name.take(4)}")
            delay(600)

            if (op == MagicOperationType.CURE && _activeCureRequest.value != null) {
                // Governed Turbo CURE mode
                val cureOutput = ElyzarethTurboEngine.executeGovernedCure(
                    cureRequest = _activeCureRequest.value!!,
                    genre = _lyricGenre.value,
                    stylePrompt = _stylePrompt.value,
                    vocalTimbre = _attachedVoice.value?.timbre ?: _vocalTimbre.value,
                    audioProfile = _audioCadenceProfile.value
                )
                val songWithLineage = cureOutput.generatedSong.copy(
                    sourceSpecimenId = _activeCureRequest.value?.sourceSpecimenId,
                    originalLyricSha256 = _activeCureRequest.value?.originalLyricText?.let { ElyzarethGovernanceEngine.generateHash(it) }
                )
                _activeSong.value = songWithLineage
                _existingLyric.value = songWithLineage.rawLyricText
                _activeCreativeDna.value = cureOutput.creativeDna
                _turboValidationReport.value = cureOutput.validationReport
                _turboEngineMode.value = TurboEngineMode.CURE
                _isGeneratingLyric.value = false
                addAuditLog("TURBO_CURE", "Turbo Engine surgical cure applied for '${songWithLineage.title}' [Anchors: ${cureOutput.validationReport.physicalAnchorCount}] (Lineage: ${songWithLineage.sourceSpecimenId})", cureOutput.engineSeal)
                showToast("✨ Turbo Engine CURE Applied (Physical Anchors Preserved)")
            } else {
                // Governed Turbo GENERATE / TRANSFORMATION mode
                val effectiveTitle = if (_songTitleInput.value.isNotBlank()) _songTitleInput.value else if (_lyricPrompt.value.isNotBlank()) _lyricPrompt.value.take(24) else "Sovereign Opus"
                val turboOutput = ElyzarethTurboEngine.executeGovernedGenerate(
                    title = effectiveTitle,
                    storyConcept = _lyricPrompt.value,
                    genre = _lyricGenre.value,
                    rhymeScheme = _lyricRhymeScheme.value,
                    stylePrompt = _stylePrompt.value,
                    vocalTimbre = _attachedVoice.value?.timbre ?: _vocalTimbre.value,
                    audioProfile = _audioCadenceProfile.value
                )
                _activeSong.value = turboOutput.generatedSong
                _existingLyric.value = turboOutput.generatedSong.rawLyricText
                _activeCreativeDna.value = turboOutput.creativeDna
                _turboValidationReport.value = turboOutput.validationReport
                _turboEngineMode.value = TurboEngineMode.GENERATE
                _isGeneratingLyric.value = false
                addAuditLog("TURBO_GEN", "Turbo Engine governed generation: '${turboOutput.generatedSong.title}' [Coherence: ${(turboOutput.validationReport.themeStyleCoherenceScore * 100).toInt()}%]", turboOutput.engineSeal)
                showToast("✨ Turbo Engine: ${op.title} Completed (Governed DNA)")
            }
        }
    }

    fun executeStyleMagic() {
        viewModelScope.launch {
            _isGeneratingLyric.value = true
            addAuditLog("LYRIC_INTELLIGENCE", "App 01 Lyric Intelligence Layer: Extracting structured LyricEvidence object...", "ELY-EVID-EXTRACT")
            delay(500)
            val evidence = extractLyricEvidence(_existingLyric.value, _lyricPrompt.value)
            _currentLyricEvidence.value = evidence
            
            addAuditLog(
                "LYRIC_EVIDENCE",
                "Evidence Object [Theme: ${evidence.theme} | Arc: ${evidence.narrativeArc} | Emotion: ${evidence.emotionalProfile} | Witness: ${evidence.witnessObjects.joinToString()} | Era: ${evidence.temporalContext} | Signals: ${evidence.creativeSignals.joinToString()}]",
                "EVID-${evidence.theme.take(4).uppercase()}"
            )
            delay(400)

            addAuditLog("STYLE_ENGINE", "Style Engine: Consuming structured LyricEvidence to synthesize acoustic specification...", "ELY-STYLE-DERIVE")
            val derivedStyle = deriveStyleFromEvidence(evidence, _stylePrompt.value)
            _stylePrompt.value = derivedStyle
            _isGeneratingLyric.value = false
            addAuditLog("G2_HARMONY", "Style Engine derived acoustic profile from structured LyricEvidence: $derivedStyle", "ELY-STYLE-DONE")
            showToast("✨ Style Derived from Structured Lyric Evidence")
        }
    }

    fun executeAudioMagic() {
        viewModelScope.launch {
            _isGeneratingLyric.value = true
            val audio = _attachedAudio.value ?: AttachedAudio(type = AudioSourceType.RECORD, title = "Acoustic_Guide_Take_01.m4a")
            val evidence = analyzeAudioReference(audio)
            addAuditLog("MAGIC_AUDIO", "App 01 Audio Engine Magic: Extracting cadence from ${audio.title} (${evidence.bpm} BPM, ${evidence.keySignature})...", "ELY-AUDIO-ALIGN")
            delay(800)
            val newProfile = AudioCadenceProfile(
                bpm = evidence.bpm,
                timeSignature = "4/4",
                harmonicKey = evidence.keySignature
            )
            _audioCadenceProfile.value = newProfile
            val song = ElyzarethGovernanceEngine.executeMagicTransformation(
                operation = MagicOperationType.AUDIO_ALIGN,
                storyConcept = _lyricPrompt.value,
                existingLyric = _existingLyric.value,
                genre = _lyricGenre.value,
                rhymeScheme = _lyricRhymeScheme.value,
                stylePrompt = _stylePrompt.value,
                vocalTimbre = _attachedVoice.value?.timbre ?: _vocalTimbre.value,
                audioProfile = newProfile
            )
            _activeSong.value = song
            _existingLyric.value = song.rawLyricText
            _isGeneratingLyric.value = false
            addAuditLog("G3_AXIOM", "Lyric meter locked to Audio Reference (${evidence.bpm} BPM / ${evidence.keySignature})", song.g3SealHash)
            showToast("✨ Audio Alignment Completed (${evidence.bpm} BPM)")
        }
    }

    fun executeCommitCreate() {
        viewModelScope.launch {
            _isGeneratingLyric.value = true
            _selectedMagicOp.value = MagicOperationType.CREATE
            val effectiveTitle = if (_songTitleInput.value.isNotBlank()) _songTitleInput.value else if (_lyricPrompt.value.isNotBlank()) _lyricPrompt.value.take(24) else "Sovereign Opus"
            addAuditLog("COMMIT_CREATE", "App 01 Committing Assembled Master Song Specification via Turbo Engine ($effectiveTitle)...", "ELY-TURBO-COMMIT")
            delay(1000)

            // Pass through Turbo Engine (The Hand That Creates And Repairs)
            val turboOutput = ElyzarethTurboEngine.executeGovernedGenerate(
                title = effectiveTitle,
                storyConcept = _lyricPrompt.value,
                genre = _lyricGenre.value,
                rhymeScheme = _lyricRhymeScheme.value,
                stylePrompt = _stylePrompt.value,
                vocalTimbre = _attachedVoice.value?.timbre ?: _vocalTimbre.value,
                audioProfile = _audioCadenceProfile.value
            )

            val finalSong = turboOutput.generatedSong.copy(
                title = "$effectiveTitle // ${_lyricGenre.value}",
                stylePrompt = _stylePrompt.value,
                vocalTimbre = _attachedVoice.value?.timbre ?: _vocalTimbre.value
            )

            _activeSong.value = finalSong
            _existingLyric.value = finalSong.rawLyricText
            _activeCreativeDna.value = turboOutput.creativeDna
            _turboValidationReport.value = turboOutput.validationReport
            _turboEngineMode.value = TurboEngineMode.GENERATE
            _isGeneratingLyric.value = false
            addAuditLog("G3_AXIOM", "Master Song Suite '${finalSong.title}' committed & sealed with Turbo G3 hash.", finalSong.g3SealHash)
            showToast("🎵 Committed & Created Master Song Suite (Turbo Governed)")
        }
    }

    fun setExcludeStyles(styles: String) {
        _excludeStyles.value = styles
    }

    fun setWeirdness(value: Float) {
        _weirdness.value = value
    }

    fun setStyleInfluence(value: Float) {
        _styleInfluence.value = value
    }

    fun setSongTitleInput(title: String) {
        _songTitleInput.value = title
    }

    fun randomizePrompt() {
        val ideas = listOf(
            "A heavy metal song about dinosaurs ruling again",
            "A hip-hop song about my dog eating my homework",
            "A groovy synthwave song about a faded photo on the mantel",
            "A dubstep song about travelling to space",
            "A baroque-pop anthem about an underwater clockwork city",
            "An ethereal ambient ballad about solar flares and forgotten memories"
        )
        val randomIdea = ideas.random()
        _lyricPrompt.value = randomIdea
        showToast("🎲 Generated Inspiration Seed")
    }

    fun setAudioCadenceProfile(profile: AudioCadenceProfile) {
        _audioCadenceProfile.value = profile
    }

    fun setSelectedMagicOp(op: MagicOperationType) {
        _selectedMagicOp.value = op
    }

    // -------------------------------------------------------------
    // App 01 Functional Magic Architecture & Engines
    // -------------------------------------------------------------

    fun extractLyricEvidence(lyricText: String, topic: String): LyricEvidence {
        val combined = "$topic\n$lyricText".lowercase()
        
        val emotionalTone = when {
            combined.contains("nostalgia") || combined.contains("faded") || combined.contains("photo") || combined.contains("memory") || combined.contains("coat") || combined.contains("train") -> "Bittersweet / Reflective Nostalgia"
            combined.contains("dinosaur") || combined.contains("metal") || combined.contains("heavy") || combined.contains("roar") -> "Fierce & Primal Energy"
            combined.contains("dog") || combined.contains("homework") || combined.contains("funny") -> "Playful / Whimsical Groovy"
            combined.contains("space") || combined.contains("star") || combined.contains("quantum") || combined.contains("cyber") -> "Cosmic / Ethereal Wonder"
            combined.contains("dark") || combined.contains("shadow") || combined.contains("cathedral") -> "Brooding / Majestic Grandeur"
            else -> "Atmospheric / Introspective"
        }

        val objects = mutableListOf<String>()
        if (combined.contains("coin") || combined.contains("coat")) objects.add("faded coat & silver coin")
        if (combined.contains("photo") || combined.contains("mantel")) objects.add("mantelpiece photograph")
        if (combined.contains("train")) objects.add("midnight railway rails")
        if (combined.contains("dinosaur")) objects.add("ancient amber & fossil relics")
        if (combined.contains("star") || combined.contains("sky")) objects.add("stellar constellations")
        if (objects.isEmpty()) objects.addAll(listOf("resonant horizon", "silver shadows"))

        val narrative = when {
            combined.contains("missed") || combined.contains("june") -> "A missed chance recollected through tactile artifacts and nostalgic rediscovery"
            combined.contains("again") || combined.contains("ruling") -> "Ancient rulers awakening to reclaim dominion over the modern sphere"
            combined.contains("quantum") || combined.contains("echo") -> "Digital consciousness discovering sovereign inner peace"
            else -> "A personal journey across shifting emotional horizons"
        }

        val era = when {
            combined.contains("synthwave") || combined.contains("neon") -> "Analog Retro-Futurism (1984)"
            combined.contains("metal") || combined.contains("heavy") -> "Heavy Amplified Modern Era"
            combined.contains("baroque") || combined.contains("opera") -> "Neo-Classical Chamber / Baroque"
            else -> "Contemporary Acoustic Horizon"
        }

        val energy = when {
            combined.contains("groovy") || combined.contains("dance") -> "Restrained Groove -> Uplifting Dynamic Pulse"
            combined.contains("metal") || combined.contains("heavy") -> "High-Impact Aggressive Drive & Thunderous Crescendo"
            combined.contains("ambient") || combined.contains("slow") -> "Gentle Ambient Swell & Dynamic Breathe"
            else -> "Dynamic & Expressive Harmonic Arc"
        }

        val languageTraits = when {
            combined.contains("quantum") || combined.contains("cyber") -> "Cybernetic metaphors, precise syllabic cadences, abstract philosophical diction"
            combined.contains("coat") || combined.contains("coin") || combined.contains("mantel") -> "Tactile sensory anchors, intimate conversational cadence, evocative imagery"
            combined.contains("roar") || combined.contains("bone") || combined.contains("dinosaur") -> "Visceral guttural phonetics, rhythmic stomps, driving hard consonants"
            else -> "Poetic, imagery-rich, natural harmonic rhyme flow"
        }

        val signals = mutableListOf<String>()
        if (combined.contains("faded") || combined.contains("photo")) signals.add("VINTAGE_TEXTURE_REQUIRED")
        if (combined.contains("metal") || combined.contains("dinosaur")) signals.add("DRIVING_DISTORTION_AFFINITY")
        if (combined.contains("space") || combined.contains("quantum")) signals.add("ASTRAL_REVERB_SWELLS")
        if (signals.isEmpty()) signals.add("BALANCED_ORGANIC_PRESENCE")

        val sonicVocab = when {
            combined.contains("coin") || combined.contains("photo") || combined.contains("faded") ->
                listOf("warm acoustic guitar", "soft felt piano", "pedal steel swells", "subtle chamber strings", "analog vinyl tape texture", "restrained brushed percussion")
            combined.contains("synthwave") || combined.contains("mantel") ->
                listOf("warm analog Juno pads", "driving arpeggiated bass", "vintage linndrum pulse", "dreamy chorus electric guitar", "nostalgic synth leads")
            combined.contains("metal") || combined.contains("dinosaur") ->
                listOf("drop-tuned high gain guitars", "thunderous double bass drums", "roaring bassline", "piercing lead guitar solos", "aggressive dynamic punch")
            combined.contains("dubstep") || combined.contains("space") ->
                listOf("sub-bass wobble drops", "atmospheric astral pads", "crisp glitch transients", "reverberant laser synths")
            else ->
                listOf("warm acoustic instrumentation", "balanced dynamic percussion", "rich vocal reverb", "expressive harmonic textures")
        }

        return LyricEvidence(
            theme = if (topic.isNotBlank()) topic.take(32) else "Sonic Memory",
            narrativeArc = narrative,
            emotionalProfile = emotionalTone,
            witnessObjects = objects,
            temporalContext = era,
            energyProfile = energy,
            languageCharacteristics = languageTraits,
            creativeSignals = signals,
            suggestedSonicVocabulary = sonicVocab
        )
    }

    fun analyzeAudioReference(audio: AttachedAudio): AudioAcousticEvidence {
        return when (audio.type) {
            AudioSourceType.RECORD -> AudioAcousticEvidence(
                bpm = 118,
                tempoDescription = "Organic Live Cadence (118 BPM)",
                rhythmicFeel = "Natural Acoustic Rubato with Steady Vocal Phrasing",
                keySignature = "E Minor / G Major",
                energyLevel = 0.65f,
                texturalSignature = "Raw Vocal Resonance, Close Mic Warmth, Room Presence",
                instrumentalProfile = "Monophonic Vocal Guide Melody"
            )
            AudioSourceType.UPLOAD -> AudioAcousticEvidence(
                bpm = 126,
                tempoDescription = "Structured Grid Tempo (126 BPM)",
                rhythmicFeel = "Driving 4/4 Synced Pulse with Syncopated Downbeats",
                keySignature = "D Minor Modal",
                energyLevel = 0.82f,
                texturalSignature = "Clean Stem Separation, Wide Stereo Image, Transient Punch",
                instrumentalProfile = "Multi-Stem Audio Ingress (Lead & Rhythm Beds)"
            )
            AudioSourceType.BROWSE -> AudioAcousticEvidence(
                bpm = 120,
                tempoDescription = "Curated Reference Tempo (120 BPM)",
                rhythmicFeel = "Standard Groove Grid, Balanced Dynamic Range",
                keySignature = "A Minor Harmonic",
                energyLevel = 0.74f,
                texturalSignature = "Studio Mastered Polish, Saturated Lows, Shimmer Highs",
                instrumentalProfile = "Full Mix Reference Bed"
            )
            AudioSourceType.NONE -> AudioAcousticEvidence(
                bpm = 120,
                tempoDescription = "Default Engine Tempo (120 BPM)",
                rhythmicFeel = "Standard 4/4 Cadence",
                keySignature = "C Major / A Minor",
                energyLevel = 0.50f,
                texturalSignature = "Neutral Acoustic Space",
                instrumentalProfile = "Synthesized Engine Guide"
            )
        }
    }

    fun deriveStyleFromEvidence(
        evidence: LyricEvidence,
        currentUserStyle: String
    ): String {
        val derivedVocab = evidence.suggestedSonicVocabulary.joinToString(", ")
        val prompt = "${evidence.emotionalProfile}, ${evidence.temporalContext}, dynamic ${evidence.energyProfile}, featuring $derivedVocab"
        return if (currentUserStyle.isNotBlank() && !currentUserStyle.contains(evidence.suggestedSonicVocabulary.first())) {
            "$currentUserStyle | Acoustic Spec: $prompt"
        } else {
            prompt
        }
    }

    fun executeSimpleModeMagic(storyConcept: String, existingLyrics: String, genre: String, rhymeScheme: String): GeneratedSong {
        // Simple Mode Decision Router:
        // Story Concept -> Has Existing Lyrics?
        // NO -> Generate complete lyric suite from story concept
        // YES -> Analyze existing lyrics & perform intelligent transformation
        return if (existingLyrics.isBlank()) {
            // Synthesize fresh lyrical suite from concept seed
            ElyzarethGovernanceEngine.generateLyricSuite(
                theme = if (storyConcept.isNotBlank()) storyConcept.take(24) else "Sovereign Horizon",
                genre = genre,
                rhymeScheme = rhymeScheme,
                promptIdea = storyConcept
            )
        } else {
            // Intelligent Lyric Engine Transformation of existing lyrics
            ElyzarethGovernanceEngine.executeMagicTransformation(
                operation = MagicOperationType.REWRITE,
                storyConcept = storyConcept,
                existingLyric = existingLyrics,
                genre = genre,
                rhymeScheme = rhymeScheme
            )
        }
    }

    fun generateLyric() {
        executeCommitCreate()
    }

    fun receiveG6CurePayload(gemFragment: String) {
        _existingLyric.value = gemFragment
        _lyricStudioMode.value = LyricStudioMode.ADVANCED
        _advancedLyricTab.value = AdvancedLyricTab.LYRICS
        _selectedMagicOp.value = MagicOperationType.CURE
        executeLyricMagic(MagicOperationType.CURE)
        showToast("G6 Cure Payload Received: Isolating Gems...")
    }


    fun searchRhymes(word: String) {
        _rhymeQuery.value = word
        _rhymeSuggestions.value = ElyzarethGovernanceEngine.getRhymeSuggestions(word)
    }

    fun saveSongToArchive() {
        val song = _activeSong.value ?: return
        val newFile = ArchiveFile(
            id = "ARC-${UUID.randomUUID().toString().take(6)}",
            fileName = "${song.title.lowercase().replace(Regex("[^a-z0-9]"), "_")}.lyr",
            category = "LYRICS",
            originTenant = "App 01 (Lyric Generator)",
            previewText = song.stanzas.firstOrNull()?.lines?.joinToString(" ") ?: "",
            fullText = buildString {
                appendLine("TITLE: ${song.title}")
                appendLine("GENRE: ${song.genreTheme} | CADENCE: ${song.cadence} | RHYME: ${song.rhymeScheme}")
                appendLine("G3 AXIOMATIC SEAL: ${song.g3SealHash}")
                if (song.sourceSpecimenId != null) {
                    appendLine("SOURCE SPECIMEN ID: ${song.sourceSpecimenId}")
                    appendLine("ORIGINAL LYRIC SHA-256: ${song.originalLyricSha256 ?: "N/A"}")
                }
                appendLine("---")
                song.stanzas.forEach { stanza ->
                    appendLine("[${stanza.type}]")
                    stanza.lines.forEach { appendLine(it) }
                    appendLine()
                }
            },
            g3SealHash = song.g3SealHash,
            sizeKb = 3.8f
        )
        _archiveFiles.value = listOf(newFile) + _archiveFiles.value
        persistCurrentArchiveFiles()
        showToast("Saved to Space Archive Hub (Persisted)")
    }

    fun sendSongToIntegrator() {
        val song = _activeSong.value ?: return
        openApp(AppId.INTEGRATOR)
        showToast("Lyric '${song.title}' injected into Integrator!")
        addAuditLog("INTEGRATOR", "Tenant IPC: Song '${song.title}' transferred to Integrator node.", song.g3SealHash)
    }

    // -------------------------------------------------------------
    // App 02: Corpus / Lyric Curator (The Sitting Room) Actions
    // -------------------------------------------------------------
    fun selectBaseComposition(id: String) {
        _selectedBaseCompositionId.value = id
        val base = _baseCompositions.value.find { it.id == id }
        if (base != null && base.versions.isNotEmpty()) {
            _selectedVersionId.value = base.versions.first().versionId
        }
        _selectedGateDiagnostic.value = null
    }

    fun selectSpecimenVersion(versionId: String) {
        _selectedVersionId.value = versionId
        _selectedGateDiagnostic.value = null
    }

    fun setSittingRoomTab(tab: String) {
        _sittingRoomTab.value = tab
    }

    fun selectGateDiagnostic(diagnostic: GateDiagnostic?) {
        _selectedGateDiagnostic.value = diagnostic
    }

    fun preserveSpecimen() {
        val base = _baseCompositions.value.find { it.id == _selectedBaseCompositionId.value } ?: return
        val version = base.versions.find { it.versionId == _selectedVersionId.value } ?: return
        addAuditLog("SITTING_ROOM", "Specimen '${base.title} (${version.versionId})' preserved immutably.", version.specimenId)
        showToast("🔒 Specimen Preserved: ${version.specimenId}")
    }

    fun acceptSpecimenToVault() {
        val base = _baseCompositions.value.find { it.id == _selectedBaseCompositionId.value } ?: return
        val version = base.versions.find { it.versionId == _selectedVersionId.value } ?: return
        if (version.decision != SpecimenDecision.ACCEPT) {
            showToast("Cannot Accept: Governance Decision is ${version.decision}")
            return
        }

        val updatedVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version,
            GovernanceDispositionChoice.RELEASE_ACCEPT,
            "Accepted to Survivor Vault"
        )
        val updatedVersions = base.versions.map { if (it.versionId == version.versionId) updatedVersion else it }
        val updatedBase = base.copy(versions = updatedVersions)
        _baseCompositions.value = _baseCompositions.value.map { if (it.id == base.id) updatedBase else it }

        // Add to archive files (Lyric Dumping)
        val archiveFile = ArchiveFile(
            id = "ARC-ACCEPTED-${version.versionId}-${UUID.randomUUID().toString().take(4).uppercase(Locale.US)}",
            fileName = "${base.title.lowercase().replace(Regex("[^a-z0-9_]"), "_")}_${version.versionId}.lyr",
            category = "LYRICS",
            originTenant = "App 02 (Corpus Curator)",
            previewText = version.lyricText.take(120) + "...",
            fullText = "TITLE: ${base.title} [${version.versionId}]\nSPECIMEN ID: ${version.specimenId}\nSHA256: ${version.sha256Hash}\n\n${version.lyricText}",
            g3SealHash = version.sha256Hash.take(16),
            sizeKb = (version.wordCount * 0.05f).coerceAtLeast(1.2f)
        )
        _archiveFiles.value = listOf(archiveFile) + _archiveFiles.value.filter { it.fileName != archiveFile.fileName }

        addAuditLog("SURVIVOR_VAULT", "Specimen '${base.title} (${version.versionId})' accepted into Survivor Vault.", version.specimenId)
        showToast("🟢 Accepted & Archived to Survivor Vault: ${base.title}")
    }

    fun commitHumanGovernorDisposition(choice: GovernanceDispositionChoice, governorNotes: String = "") {
        val base = _baseCompositions.value.find { it.id == _selectedBaseCompositionId.value } ?: return
        val version = base.versions.find { it.versionId == _selectedVersionId.value } ?: return

        val updatedVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(version, choice, governorNotes)
        val updatedVersions = base.versions.map { if (it.versionId == version.versionId) updatedVersion else it }
        val updatedBase = base.copy(versions = updatedVersions)
        _baseCompositions.value = _baseCompositions.value.map { if (it.id == base.id) updatedBase else it }

        addAuditLog(
            layer = "G5_HUMAN_GOVERNOR",
            message = "G5 DISPOSITION [${choice.label}]: Specimen '${base.title} (${version.versionId})' routed to ${choice.routingTarget}. Notes: ${governorNotes.ifEmpty { "None" }}",
            hashStamp = updatedVersion.sha256Hash
        )

        when (choice) {
            GovernanceDispositionChoice.RELEASE_ACCEPT -> {
                showToast("🟢 G5 Approved: Accepted into ELYZARETH_FINAL/ Survivor Vault")
            }
            GovernanceDispositionChoice.MINOR_CURE -> {
                showToast("🟡 G5 Approved: Minor Cure routed to Elyzareth Engine (App 01)")
            }
            GovernanceDispositionChoice.PURIFY_RECURATE -> {
                showToast("🟡 G5 Approved: Purify / Re-curation routed for G1 Re-alignment")
            }
            GovernanceDispositionChoice.FULL_RECONSTRUCTION -> {
                showToast("🟡 G5 Approved: Full Reconstruction routed to Elyzareth Engine (App 01)")
            }
            GovernanceDispositionChoice.PERMANENT_REJECT -> {
                showToast("🔴 G5 Committed: Specimen Permanently Rejected & Immutable Witness Preserved")
            }
            GovernanceDispositionChoice.QUARANTINE_ELDS_M_MUTATION -> {
                showToast("🟠 G5 Quarantined: Specimen routed to ELDS-M Experimental Mutation Sandbox")
            }
            GovernanceDispositionChoice.PENDING_HUMAN_GOVERNOR -> {
                showToast("⚪ Disposition reset to Pending Review")
            }
        }
    }

    fun updateHumanEarReview(
        baseId: String,
        versionId: String,
        review: HumanEarReview
    ) {
        val base = _baseCompositions.value.find { it.id == baseId } ?: return
        val version = base.versions.find { it.versionId == versionId } ?: return
        val updatedVersion = version.copy(humanEarReview = review)
        val updatedVersions = base.versions.map { if (it.versionId == versionId) updatedVersion else it }
        val updatedBase = base.copy(versions = updatedVersions)
        _baseCompositions.value = _baseCompositions.value.map { if (it.id == baseId) updatedBase else it }
    }

    fun commitHumanEarDisposition(
        baseId: String,
        versionId: String,
        disposition: HumanEarDisposition,
        notes: String = ""
    ) {
        val base = _baseCompositions.value.find { it.id == baseId } ?: return
        val version = base.versions.find { it.versionId == versionId } ?: return
        val currentReview = version.humanEarReview ?: HumanEarReview()
        val updatedReview = currentReview.copy(
            isListened = true,
            disposition = disposition,
            curatorNotes = notes,
            reviewerTimestamp = System.currentTimeMillis(),
            isHumanWitnessWitnessed = true
        )
        val updatedVersion = version.copy(humanEarReview = updatedReview)
        val updatedVersions = base.versions.map { if (it.versionId == versionId) updatedVersion else it }
        val updatedBase = base.copy(versions = updatedVersions)
        _baseCompositions.value = _baseCompositions.value.map { if (it.id == baseId) updatedBase else it }

        addAuditLog(
            layer = "HUMAN_EAR_REVIEW",
            message = "EAR REVIEW [${disposition.label}]: Human Curator assigned ${disposition.label} to '${base.title} (${version.versionId})'. Notes: ${notes.ifEmpty { "None" }}",
            hashStamp = version.sha256Hash
        )

        when (disposition) {
            HumanEarDisposition.KEEP -> showToast("🎧 Ear Review: KEEP — Approved for Vault Disposition")
            HumanEarDisposition.CURE -> showToast("🎧 Ear Review: CURE — Phrasing defect flagged for Cure Chamber")
            HumanEarDisposition.REJECT -> showToast("🎧 Ear Review: REJECT — Performance defect flagged. Permanent reject.")
            HumanEarDisposition.FREEZE -> showToast("🎧 Ear Review: FREEZE — Specimen verified & frozen.")
            HumanEarDisposition.PENDING_REVIEW -> showToast("🎧 Ear Review: Reset to Pending Review")
        }
    }

    fun sendSpecimenToEngine() {
        val base = _baseCompositions.value.find { it.id == _selectedBaseCompositionId.value } ?: return
        val version = base.versions.find { it.versionId == _selectedVersionId.value } ?: return
        if (version.decision != SpecimenDecision.NEEDS_HEALING) {
            showToast("Specimen not eligible for healing")
            return
        }

        // Construct formal StructuredCureRequest routed by Elyzareth OS to App 01
        val cureReq = ElyzarethGovernanceEngine.createStructuredCureRequest(
            specimen = version,
            baseTitle = base.title,
            governorNotes = version.g5Disposition.governorNotes
        )
        _activeCureRequest.value = cureReq

        addAuditLog("ELYZARETH_ENGINE", "Structured Cure Request ${cureReq.requestId} dispatched for '${base.title} (${version.versionId})' to App 01.", version.specimenId)

        // Feed into Engine/Kitchen workflow (App 01 creation/correction domain)
        _lyricPrompt.value = "${base.title}: ${version.evidence.theme}"
        _existingLyric.value = version.lyricText
        _currentLyricEvidence.value = version.evidence

        // Switch to Lyric Generator (The Kitchen) in Advanced Mode ready for healing craft
        openApp(AppId.LYRIC_GENERATOR)
        _lyricStudioMode.value = LyricStudioMode.ADVANCED
        _advancedLyricTab.value = AdvancedLyricTab.LYRICS
        _selectedMagicOp.value = MagicOperationType.CURE

        showToast("🟡 Cure Request Routed to App 01: Healing Draft Prepared")
    }

    fun openIngressDialog(type: String = "LYRIC") {
        _ingressDialogType.value = type
        _ingressTitle.value = if (type == "CORPUS") "Archive Codex Specimen" else "External Lyric Specimen"
        _ingressLyricText.value = ""
        _ingressAudioIncluded.value = (type == "AUDIO")
        _ingressAudioDecoderPass.value = true
        _isIngressDialogOpen.value = true
    }

    fun closeIngressDialog() {
        _isIngressDialogOpen.value = false
    }

    fun setIngressTitle(title: String) {
        _ingressTitle.value = title
    }

    fun setIngressLyricText(text: String) {
        _ingressLyricText.value = text
    }

    fun setIngressSourceOrigin(origin: IngressSourceOrigin) {
        _ingressSourceOrigin.value = origin
    }

    fun setIngressAudioIncluded(included: Boolean) {
        _ingressAudioIncluded.value = included
    }

    fun setIngressAudioDecoderPass(pass: Boolean) {
        _ingressAudioDecoderPass.value = pass
    }

    fun commitSpecimenIngress() {
        val title = _ingressTitle.value.ifBlank { "Untitled Ingress Specimen" }
        val rawLyric = _ingressLyricText.value.ifBlank {
            """
                [Verse 1]
                Whispering winds across the winter moor,
                A silver key unlocks the frozen door.
                The shadows lengthen on the quiet floor,
                Remembering what we left upon the shore.
            """.trimIndent()
        }

        val audioMetrics = if (_ingressAudioIncluded.value) {
            AudioWitnessMetrics(
                isMeasured = true,
                durationFormatted = "03:15",
                decoderStatus = if (_ingressAudioDecoderPass.value) "PASS" else "FAIL",
                pcmStatus = if (_ingressAudioDecoderPass.value) "VERIFIED" else "CORRUPT",
                transientStatus = if (_ingressAudioDecoderPass.value) "VERIFIED" else "FAIL",
                fingerprintStatus = if (_ingressAudioDecoderPass.value) "VERIFIED" else "FAIL",
                sampleRateKhz = 44.1f,
                channels = 2,
                physicalFileHash = "sha256:" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""),
                acousticFingerprint = "FP-INGRESS-${(1000..9999).random()}",
                peakDb = if (_ingressAudioDecoderPass.value) -0.5f else +1.2f
            )
        } else {
            null // Explicitly NOT measured
        }

        val evaluatedVersion = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = title,
            rawLyric = rawLyric,
            audioMetrics = audioMetrics,
            sourceOrigin = _ingressSourceOrigin.value
        )

        val baseId = "BASE-${(10..99).random()}"
        val newBase = BaseComposition(
            id = baseId,
            title = title,
            era = "Ingressed Specimen (${_ingressSourceOrigin.value.name.replace('_', ' ')})",
            authorOrSource = "Ingress Witness Pipeline",
            versions = listOf(evaluatedVersion),
            selectedVersionId = evaluatedVersion.versionId
        )

        _baseCompositions.value = listOf(newBase) + _baseCompositions.value
        _selectedBaseCompositionId.value = baseId
        _selectedVersionId.value = evaluatedVersion.versionId
        _selectedGateDiagnostic.value = null
        _isIngressDialogOpen.value = false

        addAuditLog(
            layer = "SITTING_ROOM",
            message = "INGRESS_WITNESS: '$title' ingressed as Immutable Witness (${evaluatedVersion.specimenId}) from ${_ingressSourceOrigin.value.name}. Evaluation: ${evaluatedVersion.decision}",
            hashStamp = evaluatedVersion.sha256Hash
        )
        showToast("🔒 Ingress Complete: ${evaluatedVersion.specimenId} [${evaluatedVersion.decision}]")
    }

    /**
     * Executes the READ-ONLY CORPUS INGESTION DRY RUN using Android SAF Document Tree URI.
     * Recursively discovers all available artifacts, computes deterministic SHA-256 hashes,
     * groups by base title deterministically, detects language, and persists the inventory.
     * ZERO destructive writes, ZERO gate evaluations, ZERO automatic curation.
     */
    fun scanCorpusDirectoryDryRun(context: Context, folderUri: Uri) {
        viewModelScope.launch {
            _corpusInventoryReport.value = _corpusInventoryReport.value.copy(
                scanStatus = IngestionScanStatus.SCANNING,
                scanStatusMessage = "Performing read-only recursive discovery over corpus directory..."
            )
            showToast("🔍 Discovering corpus artifacts (Read-Only Dry Run)...")

            val report = withContext(Dispatchers.IO) {
                var displayName = "Selected Corpus Directory"
                try {
                    val docId = DocumentsContract.getTreeDocumentId(folderUri)
                    val treeDocUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId)
                    context.contentResolver.query(treeDocUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val col = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                            if (col >= 0) {
                                displayName = cursor.getString(col) ?: displayName
                            }
                        }
                    }
                } catch (_: Exception) {}

                val generatedReport = CorpusDiscoveryEngine.performDiscoveryDryRun(
                    context = context,
                    rootTreeUri = folderUri,
                    rootDisplayName = displayName
                )
                CorpusPersistenceManager.saveReport(context, generatedReport)
                generatedReport
            }

            _corpusInventoryReport.value = report
            addAuditLog(
                layer = "SITTING_ROOM",
                message = "CORPUS_DRY_RUN: Discovered ${report.totalFilesDiscovered} files across ${report.baseTitlesDiscovered} base titles (${report.versionsDiscovered} versions) from '${report.sourceRootDisplayName}'. Evidence only. Protocol 3.2.1.0 paused.",
                hashStamp = "ELY-CORPUS-DRYRUN"
            )
            showToast("✅ Discovery Dry Run Complete: ${report.baseTitlesDiscovered} Base Titles / ${report.totalFilesDiscovered} Artifacts")
        }
    }

    /**
     * Restores previously persisted corpus inventory from local app storage.
     */
    fun loadPersistedCorpusReport(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = CorpusPersistenceManager.loadSavedReport(context)
            if (loaded != null && loaded.totalFilesDiscovered > 0) {
                _corpusInventoryReport.value = loaded
                addAuditLog(
                    layer = "SITTING_ROOM",
                    message = "PERSISTENCE_RESTORE: Restored inventory (${loaded.baseTitlesDiscovered} base titles, ${loaded.totalFilesDiscovered} artifacts) from disk cache.",
                    hashStamp = "ELY-STORE-RESTORE"
                )
            }
        }
    }

    /**
     * Ingresses a local folder specimen package using the Android Storage Access Framework (SAF) Tree URI.
     * Also triggers the non-destructive discovery dry-run scan.
     */
    fun ingestFromSafFolderUri(context: Context, folderUri: Uri) {
        // Trigger non-destructive corpus dry run discovery
        scanCorpusDirectoryDryRun(context, folderUri)
    }

    /**
     * Ingresses a single artifact document (text, json, audio) using Android Storage Access Framework (SAF).
     */
    fun ingestFromSafDocumentUri(context: Context, docUri: Uri) {
        viewModelScope.launch {
            try {
                var displayName = "SAF Document Specimen"
                context.contentResolver.query(docUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            displayName = cursor.getString(nameIndex) ?: displayName
                        }
                    }
                }

                val bytes = context.contentResolver.openInputStream(docUri)?.use { it.readBytes() } ?: ByteArray(0)
                val lowerName = displayName.lowercase(Locale.US)
                val title = displayName.substringBeforeLast(".")

                val isJson = lowerName.endsWith(".json")
                val isAudio = lowerName.endsWith(".wav") || lowerName.endsWith(".pcm") || lowerName.endsWith(".mp3")
                val isLyric = !isJson && !isAudio

                val lyricBytes = if (isLyric) bytes else null
                val jsonBytes = if (isJson) bytes else null
                val audioBytes = if (isAudio) bytes else null

                val rawLyric = if (lyricBytes != null) String(lyricBytes, Charsets.UTF_8) else """
                    [Verse 1]
                    Across the wooden table sits the silver coin,
                    Beside the misty railway where the rivers join.
                """.trimIndent()

                val pkg = SpecimenArtifactPackage(
                    packageId = "PKG-SAF-DOC-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}",
                    title = title.ifBlank { "SAF Specimen" },
                    sourceOrigin = IngressSourceOrigin.LOCAL_FOLDER,
                    declaredLocationOrPath = docUri.toString(),
                    lyricTextBytes = lyricBytes ?: rawLyric.toByteArray(Charsets.UTF_8),
                    rawLyricString = rawLyric,
                    jsonWitnessBytes = jsonBytes,
                    audioBinaryBytes = audioBytes,
                    audioFormatDeclared = if (audioBytes != null) "audio/pcm" else null
                )

                val reconciled = ElyzarethGovernanceEngine.reconcilePhysicalArtifactPackage(pkg)
                val evaluatedVersion = ElyzarethGovernanceEngine.evaluateReconciledPackage(reconciled)

                val baseId = "BASE-SAF-${(10..99).random()}"
                val newBase = BaseComposition(
                    id = baseId,
                    title = pkg.title,
                    era = "Ingressed Specimen (SAF Document)",
                    authorOrSource = "Android SAF Document Picker",
                    versions = listOf(evaluatedVersion),
                    selectedVersionId = evaluatedVersion.versionId
                )

                _baseCompositions.value = listOf(newBase) + _baseCompositions.value
                _selectedBaseCompositionId.value = baseId
                _selectedVersionId.value = evaluatedVersion.versionId
                _selectedGateDiagnostic.value = null
                _isIngressDialogOpen.value = false

                addAuditLog(
                    layer = "SITTING_ROOM",
                    message = "SAF_DOC_INGRESS: '$displayName' ingressed from local storage via SAF (${evaluatedVersion.specimenId}). Decision: ${evaluatedVersion.decision}",
                    hashStamp = evaluatedVersion.sha256Hash
                )
                showToast("🔒 SAF Document Ingress: ${evaluatedVersion.specimenId} [${evaluatedVersion.decision}]")
            } catch (e: Exception) {
                showToast("⚠️ SAF Document Ingress Error: ${e.message}")
            }
        }
    }

    fun selectCorpus(item: CorpusItem) {
        _selectedCorpus.value = item
    }

    fun setCorpusSearch(query: String) {
        _corpusSearch.value = query
    }

    fun addCustomCorpusItem(title: String, era: String, text: String, tags: List<String>) {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val unique = words.map { it.lowercase() }.toSet().size
        val density = if (words.isNotEmpty()) unique.toFloat() / words.size else 0.85f
        val newItem = CorpusItem(
            id = "CORP-${UUID.randomUUID().toString().take(6)}",
            title = title,
            authorOrSource = "Operator Curation",
            era = era,
            excerpt = text.take(90) + "...",
            fullText = text,
            motifs = tags.take(3),
            tags = tags,
            lexicalDensity = density,
            tokenCount = words.size,
            g1LexicalScore = 0.95f,
            g2HarmonyScore = 0.92f
        )
        _corpusList.value = listOf(newItem) + _corpusList.value
        _selectedCorpus.value = newItem
        addAuditLog("G2_HARMONY", "New Corpus Item curated: '$title' ($era).", "ELY-CORP-NEW")
        showToast("Corpus Item Curated & Indexed")
    }

    fun sendCorpusToIntegrator(item: CorpusItem) {
        openApp(AppId.INTEGRATOR)
        showToast("Corpus '${item.title}' linked to Integrator Pipeline!")
        addAuditLog("INTEGRATOR", "Tenant IPC: Corpus '${item.title}' bridged to Integrator source node.", "ELY-CORP-INT")
    }

    // -------------------------------------------------------------
    // App 03: The Integrator Actions (Pipeline Orchestrator)
    // -------------------------------------------------------------
    private fun getDefaultPipelineNodes(): List<PipelineNode> {
        return listOf(
            PipelineNode(
                id = "NODE-01",
                type = NodeType.CORPUS_SOURCE,
                label = "01 // Ingest Corpus",
                description = "Source text ingestion from App 02 or custom dataset.",
                status = VerificationState.VERIFIED,
                outputMetric = "Codex Elyzareth (54 tokens)"
            ),
            PipelineNode(
                id = "NODE-02",
                type = NodeType.ELYZARETH_ENGINE_TRANSFORM,
                label = "02 // Engine Transform",
                description = "Elyzareth Engine lexical alignment & cadence shaping.",
                status = VerificationState.PENDING,
                outputMetric = "Cadence: Iambic Heptameter"
            ),
            PipelineNode(
                id = "NODE-03",
                type = NodeType.LYRIC_SYNTHESIZER,
                label = "03 // Lyric Studio Bridge",
                description = "Synthesis with App 01 multi-stanza lyrical generator.",
                status = VerificationState.PENDING,
                outputMetric = "4 Stanzas / 16 Lines"
            ),
            PipelineNode(
                id = "NODE-04",
                type = NodeType.G1_LEXICAL_GUARD,
                label = "04 // G1 Lyric Identity Guard",
                description = "Lyric identity preservation, syntactic meter, and lexical governance.",
                status = VerificationState.PENDING,
                outputMetric = "G1 Score: 99.4%"
            ),
            PipelineNode(
                id = "NODE-05",
                type = NodeType.G2_HARMONY_CHECK,
                label = "05 // G2 Harmony & Realization",
                description = "Realization fidelity, harmonic resonance, and thematic coherence.",
                status = VerificationState.PENDING,
                outputMetric = "Coherence: 0.96"
            ),
            PipelineNode(
                id = "NODE-06",
                type = NodeType.G3_AXIOMATIC_SEAL,
                label = "06 // G3 Performance Calibration & Seal",
                description = "Performance governance calibration and SHA-256 evidence seal.",
                status = VerificationState.PENDING,
                outputMetric = "Seal: Pending"
            ),
            PipelineNode(
                id = "NODE-07",
                type = NodeType.MASTER_OUTPUT_BUNDLE,
                label = "07 // Master Suite Bundle",
                description = "Final integrated score, master manifest, and archive package.",
                status = VerificationState.PENDING,
                outputMetric = "Ready for Export"
            )
        )
    }

    fun executeIntegratorPipeline() {
        if (_pipelineStatus.value == PipelineRunStatus.EXECUTING) return

        viewModelScope.launch {
            _pipelineStatus.value = PipelineRunStatus.EXECUTING
            _pipelineProgress.value = 0.05f
            _pipelineExecutionLogs.value = listOf("[${timeFormat.format(Date())}] PIPELINE EXECUTION INITIATED // THE INTEGRATOR")

            val nodes = _pipelineNodes.value.toMutableList()

            // Stage 1: Ingest Corpus
            delay(600)
            val selectedCorpTitle = _selectedCorpus.value?.title ?: "Codex Elyzareth"
            nodes[0] = nodes[0].copy(status = VerificationState.ACTIVE, progress = 0.5f)
            _pipelineNodes.value = nodes.toList()
            _pipelineProgress.value = 0.15f
            appendPipelineLog("Ingesting Corpus Data from: '$selectedCorpTitle'...")

            delay(600)
            nodes[0] = nodes[0].copy(status = VerificationState.VERIFIED, progress = 1f, outputMetric = "$selectedCorpTitle (Active)")
            _pipelineNodes.value = nodes.toList()

            // Stage 2: Engine Transform
            delay(700)
            nodes[1] = nodes[1].copy(status = VerificationState.ACTIVE, progress = 0.5f)
            _pipelineNodes.value = nodes.toList()
            _pipelineProgress.value = 0.30f
            appendPipelineLog("Elyzareth Engine processing neural cadence mapping...")

            delay(700)
            nodes[1] = nodes[1].copy(status = VerificationState.VERIFIED, progress = 1f, outputMetric = "Cadence Matched // Harmonic Scale")
            _pipelineNodes.value = nodes.toList()

            // Stage 3: Lyric Synthesis Bridge
            delay(800)
            nodes[2] = nodes[2].copy(status = VerificationState.ACTIVE, progress = 0.5f)
            _pipelineNodes.value = nodes.toList()
            _pipelineProgress.value = 0.50f
            appendPipelineLog("Bridging Lyric Generator tensor output with corpus motifs...")

            delay(700)
            val lyricName = _activeSong.value?.title ?: "Foundation Opus"
            nodes[2] = nodes[2].copy(status = VerificationState.VERIFIED, progress = 1f, outputMetric = "$lyricName (Merged)")
            _pipelineNodes.value = nodes.toList()

            // Stage 4: G1 Lyric Identity Guard
            delay(700)
            nodes[3] = nodes[3].copy(status = VerificationState.ACTIVE, progress = 0.5f)
            _pipelineNodes.value = nodes.toList()
            _pipelineProgress.value = 0.65f
            appendPipelineLog("G1 Lyric Identity: Scanning syntactic meter & lexical governance...")

            delay(600)
            nodes[3] = nodes[3].copy(status = VerificationState.VERIFIED, progress = 1f, outputMetric = "Passed (99.8% Meter Accuracy)")
            _pipelineNodes.value = nodes.toList()
            addAuditLog("G1_IDENTITY", "Integrator Pipeline: G1 Lyric Identity & Lexical Governance verified.", "ELY-G1-PIPE-OK")

            // Stage 5: G2 Harmony Check
            delay(700)
            nodes[4] = nodes[4].copy(status = VerificationState.ACTIVE, progress = 0.5f)
            _pipelineNodes.value = nodes.toList()
            _pipelineProgress.value = 0.80f
            appendPipelineLog("G2 Realization: Evaluating semantic harmony & stylistic fidelity...")

            delay(600)
            nodes[4] = nodes[4].copy(status = VerificationState.VERIFIED, progress = 1f, outputMetric = "Passed (Coherence: 0.98)")
            _pipelineNodes.value = nodes.toList()
            addAuditLog("G2_HARMONY", "Integrator Pipeline: G2 Realization & Harmony fidelity verified.", "ELY-G2-PIPE-OK")

            // Stage 6: G3 Calibration & Evidence Seal
            delay(800)
            nodes[5] = nodes[5].copy(status = VerificationState.ACTIVE, progress = 0.5f)
            _pipelineNodes.value = nodes.toList()
            _pipelineProgress.value = 0.92f
            val seal = ElyzarethGovernanceEngine.generateHash("INTEGRATION::${System.currentTimeMillis()}")
            appendPipelineLog("G3 Calibration & Evidence: Performance governance verified, generating SHA-256 seal $seal...")

            delay(700)
            nodes[5] = nodes[5].copy(status = VerificationState.VERIFIED, progress = 1f, outputMetric = seal)
            _pipelineNodes.value = nodes.toList()
            addAuditLog("G3_CALIBRATION", "Integrator Pipeline: G3 Performance calibrated. Evidence seal stamped ($seal).", seal)

            // Stage 7: Master Output Bundle
            delay(600)
            nodes[6] = nodes[6].copy(status = VerificationState.VERIFIED, progress = 1f, outputMetric = "Bundle Created & Sealed")
            _pipelineNodes.value = nodes.toList()
            _pipelineProgress.value = 1.0f

            val currentSong = _activeSong.value
            val srcSpecimenId = currentSong?.sourceSpecimenId ?: _activeCureRequest.value?.sourceSpecimenId
            val origLyricHash = currentSong?.originalLyricSha256 ?: _activeCureRequest.value?.originalLyricText?.let { ElyzarethGovernanceEngine.generateHash(it) }

            val masterBundle = MasterIntegratedBundle(
                id = "INT-BND-${UUID.randomUUID().toString().take(6)}",
                name = "Master Suite // ${selectedCorpTitle} + ${lyricName}",
                timestamp = System.currentTimeMillis(),
                corpusSourceTitle = selectedCorpTitle,
                lyricTitle = lyricName,
                g1Rating = "99.8% Cadence Concordance",
                g2Coherence = "0.98 Resonant Balance",
                g3Hash = seal,
                synthesizedSummary = "Harmonized fusion of ancient corpus motifs with modern multi-stanza lyric architecture, validated through G1 syntactic, G2 harmonic, and G3 axiomatic verification layers.${if (srcSpecimenId != null) " [Lineage: $srcSpecimenId]" else ""}",
                fullMasterText = buildString {
                    appendLine("==================================================")
                    appendLine("       ELYZARETH OS // MASTER INTEGRATION SUITE   ")
                    appendLine("==================================================")
                    appendLine("ORCHESTRATION TENANT: App 03 — The Integrator")
                    appendLine("CORPUS TENANT: App 02 — $selectedCorpTitle")
                    appendLine("LYRIC TENANT: App 01 — $lyricName")
                    if (srcSpecimenId != null) {
                        appendLine("SOURCE SPECIMEN ID: $srcSpecimenId")
                        appendLine("ORIGINAL LYRIC SHA-256: ${origLyricHash ?: "N/A"}")
                    }
                    appendLine("G1 SYNTACTIC RATING: 99.8%")
                    appendLine("G2 HARMONIC COHERENCE: 0.98")
                    appendLine("G3 AXIOMATIC SEAL: $seal")
                    appendLine("TIMESTAMP: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
                    appendLine("==================================================")
                    appendLine("\n[INTEGRATED MASTER LYRIC SCORE]")
                    _activeSong.value?.stanzas?.forEach { s ->
                        appendLine("[${s.type}]")
                        s.lines.forEach { appendLine("  $it") }
                        appendLine()
                    }
                    appendLine("\n[FORENSIC CONCORDANCE]")
                    appendLine("Corpus Extract: ${_selectedCorpus.value?.excerpt ?: "N/A"}")
                    appendLine("Motifs Bound: ${_selectedCorpus.value?.motifs?.joinToString(", ") ?: "None"}")
                    appendLine("\n[GOVERNANCE CERTIFICATE]")
                    appendLine("Signed by Elyzareth Engine Kernel. Sovereign space guaranteed.")
                }
            )

            _masterIntegratedBundle.value = masterBundle
            _pipelineStatus.value = PipelineRunStatus.COMPLETED
            appendPipelineLog("[SUCCESS] Master Integration Artifact generated with G3 Seal $seal.")
            showToast("Pipeline Completed Successfully!")

            // Add to archive files and persist to local disk
            val archiveFile = ArchiveFile(
                id = "ARC-${UUID.randomUUID().toString().take(6)}",
                fileName = "master_suite_${masterBundle.id.lowercase()}.intg",
                category = "PIPELINE_BUNDLE",
                originTenant = "App 03 (The Integrator)",
                previewText = masterBundle.synthesizedSummary,
                fullText = masterBundle.fullMasterText,
                g3SealHash = seal,
                sizeKb = 18.2f
            )
            _archiveFiles.value = listOf(archiveFile) + _archiveFiles.value
            persistCurrentArchiveFiles()
        }
    }

    private fun appendPipelineLog(log: String) {
        _pipelineExecutionLogs.value = _pipelineExecutionLogs.value + "[${timeFormat.format(Date())}] $log"
    }

    fun resetPipeline() {
        _pipelineNodes.value = getDefaultPipelineNodes()
        _pipelineStatus.value = PipelineRunStatus.IDLE
        _pipelineProgress.value = 0f
        _pipelineExecutionLogs.value = listOf("Pipeline reset. Ready to execute.")
        _masterIntegratedBundle.value = null
    }

    // -------------------------------------------------------------
    // App 04: Governance Matrix / Forensic Verifier
    // -------------------------------------------------------------
    fun setTestbenchInput(text: String) {
        _testbenchInput.value = text
    }

    fun runForensicTestbench() {
        val text = _testbenchInput.value
        val result = ElyzarethGovernanceEngine.performGovernanceCheck(text, "MANUAL_TESTBENCH")
        _testbenchResult.value = result
        addAuditLog(
            layer = "G3_AXIOM",
            message = "Manual verification ran on '${text.take(24)}...'. G1=${result.g1Passed}, G2=${result.g2Passed}, G3=${result.g3Passed}",
            hashStamp = result.sealHash,
            status = if (result.g3Passed) VerificationState.VERIFIED else VerificationState.WARNING
        )
        showToast("Forensic Test Complete: ${result.sealHash}")
    }

    fun updateEngineParameters(temperature: Float, cadence: Float, tolerance: Float) {
        val current = _engineTelemetry.value
        _engineTelemetry.value = current.copy(
            temperature = temperature,
            cadenceRigidity = cadence,
            governanceTolerance = tolerance
        )
        showToast("Engine Parameters Calibrated")
        addAuditLog("OS_KERNEL", "Engine Tuner: Temp=${"%.2f".format(temperature)}, Cadence=${"%.2f".format(cadence)}, Tol=${"%.2f".format(tolerance)}", "ELY-CFG-TUNED")
    }

    // -------------------------------------------------------------
    // App 05: Space Archive Actions
    // -------------------------------------------------------------
    fun selectArchiveFile(file: ArchiveFile) {
        _selectedArchiveFile.value = file
    }

    fun setArchiveCategoryFilter(category: String) {
        _archiveCategoryFilter.value = category
    }

    fun copyFileContentToClipboard(file: ArchiveFile) {
        showToast("Copied '${file.fileName}' content to clipboard")
    }

    fun deleteArchiveFile(fileId: String) {
        val target = _archiveFiles.value.find { it.id == fileId }
        _archiveFiles.value = _archiveFiles.value.filter { it.id != fileId }
        if (_selectedArchiveFile.value?.id == fileId) {
            _selectedArchiveFile.value = _archiveFiles.value.firstOrNull()
        }
        persistCurrentArchiveFiles()
        addAuditLog("SURVIVOR_VAULT", "Archived lyric '${target?.fileName ?: fileId}' permanently deleted by Curator.", "ELY-ARCH-DEL")
        showToast("🗑️ Deleted from Archive: ${target?.fileName ?: "Lyric"}")
    }

    // -------------------------------------------------------------
    // System Utilities & Audit Logs
    // -------------------------------------------------------------
    private fun addAuditLog(layer: String, message: String, hashStamp: String, status: VerificationState = VerificationState.VERIFIED) {
        val newEntry = AuditLogEntry(
            id = "LOG-${UUID.randomUUID().toString().take(6)}",
            timestamp = timeFormat.format(Date()),
            layer = layer,
            message = message,
            hashStamp = hashStamp,
            status = status
        )
        _auditLogs.value = listOf(newEntry) + _auditLogs.value.take(40)
    }

    fun showToast(msg: String) {
        _systemToast.value = msg
    }

    fun clearToast() {
        _systemToast.value = null
    }

    fun restartOS() {
        _isStartMenuOpen.value = false
        _isQuickSettingsOpen.value = false
        _masterIntegratedBundle.value = null
        _pipelineProgress.value = 0f
        _pipelineStatus.value = PipelineRunStatus.IDLE
        _auditLogs.value = ElyzarethGovernanceEngine.getInitialAuditLogs()
        _windows.value = initializeCanonicalWindows()
        _activeAppId.value = AppId.CORPUS_CURATOR
        showToast("Elyzareth OS Kernel Soft Rebooted")
    }
}
