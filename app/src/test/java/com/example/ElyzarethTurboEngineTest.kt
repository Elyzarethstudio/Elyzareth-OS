package com.example

import com.example.engine.ElyzarethGovernanceEngine
import com.example.engine.ElyzarethTurboEngine
import com.example.model.*
import com.example.viewmodel.ElyzarethOSViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Elyzareth 60-Day Turbo Engine Integration Tests
 *
 * Verifies:
 * 1. Turbo Engine operates inside Tenant 1 (App 01) without being an external tenant.
 * 2. Generator never generates "blind" — all paths pass through Creative DNA & Physical Anchor checks.
 * 3. C16 Creative Intelligence & Physical Anchor Principle enforcement.
 * 4. Cliché / Prohibited Trope zero-tolerance suppression.
 * 5. Theme ↔ Emotional Profile ↔ Style Coherence Evaluator.
 * 6. App 02 → App 01 Structured Cure Request -> Turbo Engine surgical single-variable cure -> Re-examination pipeline.
 * 7. Information Survival Hierarchy (Witness Objects & Narrative Meaning preserved over generic rhyme).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ElyzarethTurboEngineTest {

    private lateinit var viewModel: ElyzarethOSViewModel

    @Before
    fun setUp() {
        viewModel = ElyzarethOSViewModel()
    }

    @Test
    fun test01_turboEngineInternalToTenant1NotExternalTenant() {
        // App 01 is Tenant 1 (Lyric Generator)
        assertEquals("App 01 must be Lyric Generator", AppId.LYRIC_GENERATOR, AppId.values().first { it.tenantNumber == "TENANT 01" })
        
        // Ensure Turbo Engine is an internal engine layer, not a 6th OS tenant
        assertEquals("Elyzareth OS must strictly maintain five canonical tenants", 5, AppId.values().size)
    }

    @Test
    fun test02_governedGenerateEnforcesPhysicalAnchorsAndSuppressesTropes() {
        val output = ElyzarethTurboEngine.executeGovernedGenerate(
            title = "Axiom of the Cedar River",
            storyConcept = "Memories kept upon the wooden table beside the railway",
            genre = "Acoustic Dark Folk",
            rhymeScheme = "AABB",
            stylePrompt = "warm acoustic guitar, felt piano",
            vocalTimbre = "Resonant Alto",
            audioProfile = AudioCadenceProfile(bpm = 64, timeSignature = "4/4")
        )

        assertNotNull("Generated song suite must not be null", output.generatedSong)
        assertNotNull("Creative DNA profile must be established before craft", output.creativeDna)
        assertNotNull("Turbo validation report must be generated", output.validationReport)

        // Verify Physical Anchor Principle
        assertTrue("Must contain physical witness anchors", output.validationReport.physicalAnchorCount >= 2)
        assertTrue(
            "Anchors list must contain tactile items",
            output.validationReport.physicalAnchorsFound.any { it.contains("table") || it.contains("coin") || it.contains("railway") || it.contains("key") }
        )

        // Verify Cliché Suppression (Zero Prohibited Tropes)
        assertTrue("Prohibited generic tropes must be strictly purged", output.validationReport.prohibitedTropesDetected.isEmpty())
        assertTrue("Song must be verified governed", output.validationReport.isGoverned)
        assertTrue("Information survival score must be >= 0.90", output.validationReport.informationSurvivalScore >= 0.90f)
    }

    @Test
    fun test03_themeStyleCoherenceEvaluation() {
        // Test Intimate Lyrical + Extreme Death Metal Contradiction
        val (scoreLow, msgLow) = ElyzarethTurboEngine.evaluateThemeStyleCoherence(
            theme = "Lullaby for a sleeping child",
            emotionalProfile = "Intimate whisper and gentle heartbreak",
            genre = "Death Metal",
            stylePrompt = "blistering distortion and speed blast beats"
        )
        assertTrue("Contradictory theme/style pairing must be flagged", scoreLow < 0.70f)
        assertTrue("Flag message must warn of theme-style mismatch", msgLow.contains("Surprising pairing flagged"))

        // Test Harmonious Dark Folk pairing
        val (scoreHigh, msgHigh) = ElyzarethTurboEngine.evaluateThemeStyleCoherence(
            theme = "Autumn railway crossing",
            emotionalProfile = "Bittersweet Nostalgia & Physical Longing",
            genre = "Acoustic Folk",
            stylePrompt = "warm steel-string guitar, dry room vocal"
        )
        assertTrue("Harmonized pairing must score >= 0.95", scoreHigh >= 0.95f)
        assertTrue("Harmonized pairing must report harmonized", msgHigh.contains("Harmonized"))
    }

    @Test
    fun test04_app01ViewModelExecuteCommitCreateRunsThroughTurboEngine() {
        viewModel.openApp(AppId.LYRIC_GENERATOR)
        viewModel.setSongTitleInput("The Silver Key of Elyzareth")
        viewModel.setLyricPrompt("A wooden table by the railway where an old coat hangs")
        viewModel.setLyricGenre("Acoustic Dark Folk")

        // Execute Commit Create
        viewModel.executeCommitCreate()
        org.robolectric.shadows.ShadowLooper.idleMainLooper(1500, java.util.concurrent.TimeUnit.MILLISECONDS)

        // Verify active song and Turbo report
        val activeSong = viewModel.activeSong.value
        assertNotNull("Active song must be populated", activeSong)
        assertEquals("Turbo Engine mode must be GENERATE", TurboEngineMode.GENERATE, viewModel.turboEngineMode.value)

        val report = viewModel.turboValidationReport.value
        assertNotNull("Turbo validation report must be generated on commit create", report)
        assertTrue("Physical anchors must be present", report!!.physicalAnchorCount >= 1)
        assertTrue("Prohibited clichés must be zero", report.prohibitedTropesDetected.isEmpty())

        val dna = viewModel.activeCreativeDna.value
        assertNotNull("Creative DNA profile must be bound to active song", dna)
        assertTrue("DNA must have provenance hash", dna!!.provenanceHash.isNotBlank())
    }

    @Test
    fun test05_app02ToApp01TurboEngineCurePipeline() {
        // 1. App 02: Prepare contaminated specimen needing healing
        val specimenNeedingCure = ElyzarethGovernanceEngine.getInitialBaseCompositions().first().versions.first()
        val cureRequest = ElyzarethGovernanceEngine.createStructuredCureRequest(
            specimen = specimenNeedingCure,
            baseTitle = "Silver Coin and Dusty Railway",
            governorNotes = "Remove neon tapestry and ensure physical table anchor is reinforced"
        )

        // 2. Route Cure Request to App 01 via Turbo Engine
        val cureOutput = ElyzarethTurboEngine.executeGovernedCure(
            cureRequest = cureRequest,
            genre = "Acoustic Dark Folk",
            stylePrompt = "warm acoustic guitar, dry room vocal"
        )

        assertNotNull("Cure output must not be null", cureOutput)
        assertTrue("Must be marked as cure output", cureOutput.isCureOutput)
        assertTrue("Physical anchors must be preserved in cured output", cureOutput.validationReport.physicalAnchorCount >= 2)
        assertFalse("Neon tapestry or prohibited tropes must be purged", cureOutput.generatedSong.rawLyricText.contains("neon tapestry", ignoreCase = true))
        assertTrue("Seal must indicate TURBO_CURE", cureOutput.engineSeal.isNotBlank())
    }

    @Test
    fun test06_physicalAnchorDiagnosticEvaluatorDeterministicBehavior() {
        // 1. Anchored lyric -> PASS
        val anchoredLyric = """
            Across the wooden table sits the silver coin,
            Beside the railway where the rivers join.
            The iron latch clicks shut at three,
            Beneath the branches of the maple tree.
        """.trimIndent()

        val passDiag = ElyzarethTurboEngine.evaluatePhysicalAnchors(anchoredLyric)
        assertEquals("PASS", passDiag.status)
        assertTrue("Anchor objects must contain table and coin", passDiag.anchorObjects.contains("table") && passDiag.anchorObjects.contains("coin"))
        assertNull("PASS diagnostic must have null failReason", passDiag.failReason)
        assertFalse("Anchored lyric must not trigger unanchored emotion", passDiag.abstractEmotionUnanchoredDetected)

        // 2. Purely abstract ungrounded emotional lyric -> FAIL with explainable reason
        val unanchoredLyric = """
            My sorrow is deep and my heartbreak is wide,
            Lost in the feelings that I cannot hide.
            The agony of destiny, the passion and the pain,
            Wishing for the longing to subside again.
        """.trimIndent()

        val failDiag = ElyzarethTurboEngine.evaluatePhysicalAnchors(unanchoredLyric)
        assertEquals("FAIL", failDiag.status)
        assertTrue("Anchor objects must be empty for abstract lyric", failDiag.anchorObjects.isEmpty())
        assertTrue("Abstract unanchored emotion must be flagged", failDiag.abstractEmotionUnanchoredDetected)
        assertNotNull("FAIL diagnostic must contain explainable failReason", failDiag.failReason)
        assertTrue("Fail reason must explain lack of concrete witness objects", failDiag.failReason!!.contains("Zero concrete physical witness objects"))

        // 3. Metaphor Soup lyric -> flags metaphorSoupDetected
        val metaphorSoupLyric = """
            A symphony of stars and an ocean of tears,
            Tapestry of dreams through the passage of years.
        """.trimIndent()

        val soupDiag = ElyzarethTurboEngine.evaluatePhysicalAnchors(metaphorSoupLyric)
        assertTrue("Metaphor soup must be flagged", soupDiag.metaphorSoupDetected)
    }

    @Test
    fun test07_elyzarethRusticAcousticConstraintBlueprint() {
        val constraint = ELYZARETH_RUSTIC_ACOUSTIC_v1

        assertEquals("ELYZARETH_RUSTIC_ACOUSTIC_v1", constraint.constraintId)
        assertEquals("1.0.0", constraint.schemaVersion)
        assertEquals("Room 05 / Rustic / Dry Parlor", constraint.roomProfile)
        assertEquals(0.4f, constraint.maxT60Seconds, 0.001f)
        assertEquals(15f, constraint.maxWetRatioPercent, 0.001f)
        assertEquals("close-mic male baritone", constraint.vocalSpec)
        assertEquals("steel-string acoustic fingerpicking", constraint.instrumentationSpec)
        assertTrue(constraint.prohibitedElements.containsAll(listOf("drums", "percussion", "synth pads", "strings")))
        assertEquals(0f, constraint.arrangementDriftTargetPercent, 0.001f)

        // Explicit architectural invariant: Declared constraint != Measured audio evidence
        assertFalse("Lyric Engine constraint must NOT claim to be measured PCM evidence", constraint.isMeasuredAudioEvidence)
    }

    @Test
    fun test08_elyzarethSparseArrangementConstraintsLockedArtifact() {
        val sparse = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0

        // Schema & ID
        assertEquals("ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1.0", sparse.constraintId)
        assertEquals("1.0.0", sparse.schemaVersion)

        // 1. Complete Percussive Suppression
        val expectedPercussion = listOf("kicks", "snares", "hi-hats", "shakers", "tambourines", "hand percussion", "percussive loops")
        assertTrue("Must suppress all percussive elements", sparse.percussiveSuppression.containsAll(expectedPercussion))

        // 2. Synthetic & Orchestral Pad Suppression
        val expectedAtmospherics = listOf("synth pads", "bowed string sections", "drones", "organ fills", "atmospheric synth washes")
        assertTrue("Must suppress synthetic/orchestral pads and drones", sparse.syntheticAndOrchestralPadSuppression.containsAll(expectedAtmospherics))

        // 3. Acoustic Core & Thumb-Bass Isolation
        assertEquals("steel-string acoustic fingerpicking + subtle thumb-plucked bass movement", sparse.coreAcousticRealization)
        val expectedExcludedInst = listOf("electric guitars", "dense chordal strumming blocks", "sub-bass", "synth bass")
        assertTrue("Must exclude heavy/electric/synth instrumentation", sparse.excludedInstrumentation.containsAll(expectedExcludedInst))

        // 4. Vocal-Layer Sectional Expansion
        assertTrue("Expansion must be through vocal-layer density", sparse.sectionalExpansionRule.contains("vocal-layer density"))

        // 5. Dynamic Restraint & Swell Prohibition
        val expectedProhibitedDynamics = listOf("cinematic swells", "crescendo risers", "reverse cymbals", "expanding artificial reverb tails")
        assertTrue("Must prohibit swells and risers", sparse.prohibitedDynamics.containsAll(expectedProhibitedDynamics))
        assertTrue("Restrained dynamics profile", sparse.dynamicRestraintProfile.contains("restrained dynamics"))

        // 6. Zero Arrangement Drift
        assertTrue("Zero arrangement drift must be true", sparse.zeroArrangementDrift)
        assertEquals(0.0f, sparse.arrangementDriftTargetPercent, 0.001f)

        // Invariant Boundary
        assertFalse("Declared production constraints must not claim to be measured PCM evidence", sparse.isMeasuredAudioEvidence)
    }
}
