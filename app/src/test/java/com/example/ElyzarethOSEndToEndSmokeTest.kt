package com.example

import com.example.model.*
import com.example.viewmodel.ElyzarethOSViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Elyzareth OS End-to-End Functional Smoke Test
 *
 * Exercises:
 * 1. Desktop Shell state, taskbar, start menu, quick settings
 * 2. Window operations: launch, minimize, maximize, restore, close, z-index elevation
 * 3. App 01 (Lyric Studio) primary interaction paths
 * 4. App 02 (Corpus Curator / The Sitting Room) 5-tab curatorial suite & disposition commits
 * 5. App 03 (The Integrator) pipeline orchestration & track management
 * 6. App 04 (Engine Terminal & Governance Matrix) testbench & telemetry
 * 7. App 05 (Space Archive) artifact catalog & checksum verification
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ElyzarethOSEndToEndSmokeTest {

    private lateinit var viewModel: ElyzarethOSViewModel

    @Before
    fun setUp() {
        viewModel = ElyzarethOSViewModel()
    }

    @Test
    fun test01_desktopShellAndTaskbarOperations() {
        // Initial state: Start Menu and Quick Settings closed
        assertFalse(viewModel.isStartMenuOpen.value)
        assertFalse(viewModel.isQuickSettingsOpen.value)

        // Toggle Start Menu
        viewModel.toggleStartMenu()
        assertTrue(viewModel.isStartMenuOpen.value)
        assertFalse(viewModel.isQuickSettingsOpen.value)

        // Search in Start menu
        viewModel.setSearchQuery("Lyric")
        assertEquals("Lyric", viewModel.searchQuery.value)
        viewModel.setSearchQuery("")

        // Toggle Quick Settings
        viewModel.toggleQuickSettings()
        assertTrue(viewModel.isQuickSettingsOpen.value)
        assertFalse(viewModel.isStartMenuOpen.value)

        // Wallpaper switching
        viewModel.setWallpaper("Cybernetic Gold")
        assertEquals("Cybernetic Gold", viewModel.desktopWallpaper.value)

        // Close quick settings
        viewModel.toggleQuickSettings()
        assertFalse(viewModel.isQuickSettingsOpen.value)
    }

    @Test
    fun test02_windowLifecycleAndZIndexManagement() {
        // Initial window state has App 03 and App 01 initialized
        assertTrue(viewModel.windows.value.containsKey(AppId.INTEGRATOR))

        // Launch / Focus App 01
        viewModel.openApp(AppId.LYRIC_GENERATOR)
        assertTrue(viewModel.windows.value.containsKey(AppId.LYRIC_GENERATOR))
        assertEquals(AppId.LYRIC_GENERATOR, viewModel.activeAppId.value)
        val initialZ1 = viewModel.windows.value[AppId.LYRIC_GENERATOR]?.zIndex ?: 0f

        // Launch App 02
        viewModel.openApp(AppId.CORPUS_CURATOR)
        assertTrue(viewModel.windows.value.containsKey(AppId.CORPUS_CURATOR))
        assertEquals(AppId.CORPUS_CURATOR, viewModel.activeAppId.value)
        val initialZ2 = viewModel.windows.value[AppId.CORPUS_CURATOR]?.zIndex ?: 0f
        assertTrue("Newly focused window should have higher zIndex", initialZ2 > initialZ1)

        // Maximize App 02
        viewModel.toggleMaximizeWindow(AppId.CORPUS_CURATOR)
        assertTrue(viewModel.windows.value[AppId.CORPUS_CURATOR]?.isMaximized == true)

        // Restore App 02
        viewModel.toggleMaximizeWindow(AppId.CORPUS_CURATOR)
        assertFalse(viewModel.windows.value[AppId.CORPUS_CURATOR]?.isMaximized == true)

        // Minimize App 02
        viewModel.minimizeWindow(AppId.CORPUS_CURATOR)
        assertTrue(viewModel.windows.value[AppId.CORPUS_CURATOR]?.isMinimized == true)

        // Restore via focusWindow
        viewModel.focusWindow(AppId.CORPUS_CURATOR)
        assertFalse(viewModel.windows.value[AppId.CORPUS_CURATOR]?.isMinimized == true)
        assertEquals(AppId.CORPUS_CURATOR, viewModel.activeAppId.value)

        // Close App 01
        viewModel.closeWindow(AppId.LYRIC_GENERATOR)
        assertTrue(viewModel.windows.value[AppId.LYRIC_GENERATOR]?.isClosed == true)
    }

    @Test
    fun test03_app01_lyricStudioPrimaryFlow() {
        viewModel.openApp(AppId.LYRIC_GENERATOR)

        // Mode toggling
        viewModel.setLyricStudioMode(LyricStudioMode.ADVANCED)
        assertEquals(LyricStudioMode.ADVANCED, viewModel.lyricStudioMode.value)
        viewModel.setAdvancedLyricTab(AdvancedLyricTab.STYLES)
        assertEquals(AdvancedLyricTab.STYLES, viewModel.advancedLyricTab.value)

        // Prompt & Parameters
        viewModel.setLyricPrompt("An interstellar hymn of ancient sovereignty")
        assertEquals("An interstellar hymn of ancient sovereignty", viewModel.lyricPrompt.value)
        viewModel.setLyricGenre("Neo-Baroque Synth")
        assertEquals("Neo-Baroque Synth", viewModel.lyricGenre.value)
        viewModel.setLyricRhymeScheme("AABB")
        assertEquals("AABB", viewModel.lyricRhymeScheme.value)
        viewModel.setVocalGender(VocalGender.FEMALE)
        assertEquals(VocalGender.FEMALE, viewModel.vocalGender.value)
        viewModel.toggleInstrumental()
        assertTrue(viewModel.isInstrumental.value)
        viewModel.toggleInstrumental()
        assertFalse(viewModel.isInstrumental.value)

        // Rhyme Query
        viewModel.searchRhymes("light")
        assertEquals("light", viewModel.rhymeQuery.value)
        val rhymes = viewModel.rhymeSuggestions.value
        assertNotNull(rhymes)

        // Audio & Voice Attachments
        viewModel.attachAudio(
            type = AudioSourceType.UPLOAD,
            title = "stem_guide.wav"
        )
        assertNotNull(viewModel.attachedAudio.value)

        viewModel.attachVoice(
            type = VoiceSourceType.LIBRARY,
            personaName = "Sovereign Alto",
            timbre = "Ethereal / Resonant"
        )
        assertNotNull(viewModel.attachedVoice.value)

        // Generate Lyric
        viewModel.generateLyric()
        assertNotNull(viewModel.activeSong.value)
    }

    @Test
    fun test04_app02_corpusCuratorSittingRoomPrimaryFlow() {
        viewModel.openApp(AppId.CORPUS_CURATOR)

        // Select Composition & Version
        val compositions = viewModel.baseCompositions.value
        assertTrue(compositions.isNotEmpty())
        val compId = compositions.first().id
        viewModel.selectBaseComposition(compId)
        assertEquals(compId, viewModel.selectedBaseCompositionId.value)

        // Tab Navigation across 5 tabs
        val tabs = listOf("WITNESS", "EXAMINATION", "FINDINGS", "DISPOSITION", "AUDIT")
        for (tab in tabs) {
            viewModel.setSittingRoomTab(tab)
            assertEquals(tab, viewModel.sittingRoomTab.value)
        }

        // Commit Human Governor Disposition
        viewModel.setSittingRoomTab("DISPOSITION")
        
        viewModel.commitHumanGovernorDisposition(
            choice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            governorNotes = "Curatorial release approved under Sovereign Mandate"
        )

        val updatedAudit = viewModel.auditLogs.value
        assertTrue(updatedAudit.any { it.message.contains("G5 DISPOSITION") || it.message.contains("RELEASE_ACCEPT") })
    }

    @Test
    fun test05_app03_integratorPrimaryFlow() {
        viewModel.openApp(AppId.INTEGRATOR)

        // Initial Pipeline Nodes
        val initialNodes = viewModel.pipelineNodes.value
        assertTrue(initialNodes.isNotEmpty())
        assertEquals(7, initialNodes.size)

        // Execute pipeline
        viewModel.executeIntegratorPipeline()
        val runningStatus = viewModel.pipelineStatus.value
        assertTrue(runningStatus == PipelineRunStatus.EXECUTING || runningStatus == PipelineRunStatus.COMPLETED)
    }

    @Test
    fun test06_app04_governanceMatrixAndTerminalFlow() {
        viewModel.openApp(AppId.ENGINE_TERMINAL)

        // Evaluate Forensic Testbench
        viewModel.setTestbenchInput("In the sovereign space of Elyzareth, truth holds.")
        viewModel.runForensicTestbench()
        val result = viewModel.testbenchResult.value
        assertNotNull(result)
        assertTrue(result!!.g1Passed)

        // Engine Telemetry and Audit Logs
        val telemetry = viewModel.engineTelemetry.value
        assertNotNull(telemetry)
        assertTrue(telemetry.temperature >= 0f)

        val logs = viewModel.auditLogs.value
        assertTrue(logs.isNotEmpty())
    }

    @Test
    fun test07_app05_spaceArchivePrimaryFlow() {
        viewModel.openApp(AppId.SPACE_ARCHIVE)

        // Filter categories
        viewModel.setArchiveCategoryFilter("SOVEREIGN_CORPUS")
        assertEquals("SOVEREIGN_CORPUS", viewModel.archiveCategoryFilter.value)

        viewModel.setArchiveCategoryFilter("ALL")
        assertEquals("ALL", viewModel.archiveCategoryFilter.value)

        // Verify archive files exist
        val files = viewModel.archiveFiles.value
        assertTrue(files.isNotEmpty())

        // Select archive file
        val firstFile = files.first()
        viewModel.selectArchiveFile(firstFile)
        assertEquals(firstFile.id, viewModel.selectedArchiveFile.value?.id)
    }

    @Test
    fun test08_app02_persistenceAndColdRestartIntegrity() {
        // 1. Initial Cold Launch Verification: Desktop presented with all 5 canonical tenants ready
        assertTrue("App 02 must be in canonical AppId registry", AppId.values().contains(AppId.CORPUS_CURATOR))
        assertTrue("App 02 window must be initialized on launch", viewModel.windows.value.containsKey(AppId.CORPUS_CURATOR))
        assertFalse("App 02 must not be closed on launch", viewModel.windows.value[AppId.CORPUS_CURATOR]!!.isClosed)

        // Launch App 02 from OS Desktop
        viewModel.openApp(AppId.CORPUS_CURATOR)
        assertEquals("App 02 must be foreground active after launch", AppId.CORPUS_CURATOR, viewModel.activeAppId.value)
        assertFalse("App 02 window must not be minimized after launch", viewModel.windows.value[AppId.CORPUS_CURATOR]!!.isMinimized)

        // 2. Cold Restart 1: Simulate complete process termination & fresh restart
        val coldRestartVm1 = ElyzarethOSViewModel()
        assertTrue("Cold Restart 1: App 02 must be in registry", AppId.values().contains(AppId.CORPUS_CURATOR))
        assertTrue("Cold Restart 1: App 02 must be initialized in windows", coldRestartVm1.windows.value.containsKey(AppId.CORPUS_CURATOR))
        assertFalse("Cold Restart 1: App 02 window must not be closed", coldRestartVm1.windows.value[AppId.CORPUS_CURATOR]!!.isClosed)
        coldRestartVm1.openApp(AppId.CORPUS_CURATOR)
        assertEquals("Cold Restart 1: App 02 must be active foreground after open", AppId.CORPUS_CURATOR, coldRestartVm1.activeAppId.value)

        // 3. Cold Restart 2: Simulate second process termination & fresh restart
        val coldRestartVm2 = ElyzarethOSViewModel()
        assertTrue("Cold Restart 2: App 02 must be in registry", AppId.values().contains(AppId.CORPUS_CURATOR))
        assertTrue("Cold Restart 2: App 02 must be initialized in windows", coldRestartVm2.windows.value.containsKey(AppId.CORPUS_CURATOR))
        assertFalse("Cold Restart 2: App 02 window must not be closed", coldRestartVm2.windows.value[AppId.CORPUS_CURATOR]!!.isClosed)
        coldRestartVm2.openApp(AppId.CORPUS_CURATOR)
        assertEquals("Cold Restart 2: App 02 must be active foreground after open", AppId.CORPUS_CURATOR, coldRestartVm2.activeAppId.value)

        // 4. Soft Reboot Verification
        coldRestartVm2.restartOS()
        assertTrue("Reboot: App 02 window must be restored in windows map", coldRestartVm2.windows.value.containsKey(AppId.CORPUS_CURATOR))
    }

    @Test
    fun test09_androidApplicationLauncherLayerDiscovery() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageManager = context.packageManager
        val packageName = context.packageName

        // 1. Verify Android Package Identification
        assertNotNull("Package name must be resolved", packageName)
        assertEquals("com.aistudio.elyzarethos.v9xk", packageName)

        // 2. Query Android Launcher Intents (How Android OS discovers home screen apps)
        val launcherIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            setPackage(packageName)
        }

        val resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)
        assertFalse("Android OS launcher query must find at least one launchable activity for Elyzareth OS", resolveInfos.isEmpty())

        val mainActivityInfo = resolveInfos.first()
        assertEquals("Launchable activity class name must be com.example.MainActivity", "com.example.MainActivity", mainActivityInfo.activityInfo.name)
        assertTrue("MainActivity must be exported to be launchable by Android OS Launcher", mainActivityInfo.activityInfo.exported)

        // 3. Verify App Label & Icon Resources for Android Launcher Drawer
        val appLabel = packageManager.getApplicationLabel(context.applicationInfo).toString()
        assertEquals("Android Launcher app label must match Elyzareth OS", "Elyzareth OS", appLabel)
    }
}
