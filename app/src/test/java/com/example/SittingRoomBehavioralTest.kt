package com.example

import com.example.engine.ElyzarethGovernanceEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ELYZARETH OS — SITTING ROOM (APP 02) BEHAVIORAL TEST SUITE
 * Spec v1 Authoritative Verification
 *
 * 10 Automated Behavioral Tests covering:
 * - Test 01: Ingress & SHA-256 generation (Deterministic Immutable Witness)
 * - Test 02: G1 Path A (Text/Schema Path — Complete 8-field schema without Audio -> PASS)
 * - Test 03: G1 Path B (Physical Audio Path — Corrupt PCM / Decoder Failure -> G1 FAIL)
 * - Test 04: G1 Contract (Missing mandatory LyricEvidence field -> G1 FAIL, Pipeline Halt)
 * - Test 05: G2 Band 1: COMPLIANT (90-100 score)
 * - Test 06: G2 Band 2: TOLERANT (80-89 score, minor repair)
 * - Test 07: G2 Band 4: FAILURE (<70 score)
 * - Test 08: G3-P01 Performance Calibration (Structural cadence & section balance)
 * - Test 09: G4 Informational & G6 Deferred Preservation
 * - Test 10: G5 Human Governor Protocol (3.2.1.0) & 4 Disposition Commit Pathways
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SittingRoomBehavioralTest {

    private val fullValidLyric = """
        [Verse 1]
        Across the old wooden table sits the faded coat and silver coin,
        Where memory and time in quiet currents join.
        The mantelpiece photograph watches through the rain,
        As ancient echoes whisper down the railway train.

        [Chorus]
        Hold the boundary in the quiet light,
        Sovereign memory standing in the night.
        Every metric syllable in iron grace,
        A sacred witness in the eternal space.

        [Verse 2]
        The stellar constellations turn across the sky,
        While silent watchers let the fleeting hours go by.
        With tactile truth and golden cadence preserved whole,
        The music anchors deep within the living soul.
    """.trimIndent()

    private val validAudio = AudioWitnessMetrics(
        isMeasured = true,
        durationSeconds = 192,
        durationFormatted = "03:12",
        sampleRateKhz = 44.1f,
        channels = 2,
        peakDb = -0.3f,
        decoderStatus = "PASS",
        pcmStatus = "VERIFIED",
        transientStatus = "VERIFIED",
        fingerprintStatus = "VERIFIED",
        physicalFileHash = "0x9F4C2A1E88B",
        acousticFingerprint = "FP-44100-STEREO-OK"
    )

    @Test
    fun test01_IngressAndDeterministicSha256WitnessGeneration() {
        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Midnight Railway & Silver Coin",
            rawLyric = fullValidLyric,
            audioMetrics = validAudio,
            sourceOrigin = IngressSourceOrigin.LAPTOP
        )

        assertNotNull(specimen.specimenId)
        assertTrue(specimen.specimenId.startsWith("WITNESS-"))
        assertTrue("Hash must be deterministic SHA-256", specimen.sha256Hash.startsWith("sha256:"))
        assertEquals(64 + 7, specimen.sha256Hash.length) // "sha256:" + 64 hex chars
        assertEquals(IngressSourceOrigin.LAPTOP, specimen.sourceOrigin)
        assertEquals(fullValidLyric, specimen.lyricText)
        assertTrue(specimen.historyTrail.isNotEmpty())
    }

    @Test
    fun test02_G1DualPath_TextSchemaPath_NoAudio_PassesWithAudioPending() {
        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Text-Only Codex",
            rawLyric = fullValidLyric,
            audioMetrics = null,
            sourceOrigin = IngressSourceOrigin.GOOGLE_DRIVE
        )

        val g1 = specimen.gates.find { it.gateId == "G1" }
        assertNotNull(g1)
        assertEquals(GateStatus.PASS, g1!!.status)
        assertTrue(g1.detailedEvidence.any { it.contains("Audio: NOT_APPLICABLE_PENDING_AUDIO_RENDER") })

        // 8-field schema verified
        assertEquals("Bittersweet / Reflective Nostalgia", specimen.evidence.emotionalProfile)
        assertTrue(specimen.evidence.witnessObjects.isNotEmpty())
    }

    @Test
    fun test03_G1DualPath_PhysicalAudioPath_DecoderFailure_FailsG1() {
        val brokenAudio = validAudio.copy(
            decoderStatus = "FAIL",
            pcmStatus = "CORRUPT"
        )

        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Corrupt Audio Specimen",
            rawLyric = fullValidLyric,
            audioMetrics = brokenAudio,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT
        )

        val g1 = specimen.gates.find { it.gateId == "G1" }
        assertNotNull(g1)
        assertEquals(GateStatus.FAIL, g1!!.status)
        assertEquals(SpecimenDecision.NOT_ELIGIBLE, specimen.decision)
    }

    @Test
    fun test04_G1Contract_IncompleteLyricEvidenceSchema_FailsG1AndHaltsPipeline() {
        // Create an invalid/incomplete LyricEvidence missing mandatory schema field 'theme' and 'narrativeArc'
        val incompleteEvidence = LyricEvidence(
            theme = "", // MISSING MANDATORY FIELD
            narrativeArc = "", // MISSING MANDATORY FIELD
            emotionalProfile = "Solemn",
            witnessObjects = listOf("silver coin"),
            temporalContext = "Midnight",
            energyProfile = "Low",
            languageCharacteristics = "Poetic",
            creativeSignals = listOf("Signal-1")
        )

        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Incomplete Schema Specimen",
            rawLyric = fullValidLyric,
            audioMetrics = null,
            sourceOrigin = IngressSourceOrigin.LOCAL_FOLDER,
            preSuppliedEvidence = incompleteEvidence
        )

        val g1 = specimen.gates.find { it.gateId == "G1" }
        assertNotNull(g1)
        assertEquals(GateStatus.FAIL, g1!!.status)
        assertEquals(SpecimenDecision.NOT_ELIGIBLE, specimen.decision)
        assertTrue(specimen.decisionReason.contains("Pipeline halted"))
        // Verify immutable witness & audit trail is preserved despite G1 failure
        assertTrue(specimen.sha256Hash.startsWith("sha256:"))
        assertTrue(specimen.historyTrail.isNotEmpty())
        assertTrue(g1.detailedEvidence.any { it.contains("Mandatory schema field 'theme' is missing/blank") })
    }

    @Test
    fun test05_G2DiagnosticEvaluator_CompliantBand_90To100Score() {
        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Pristine Witness Specimen",
            rawLyric = fullValidLyric,
            audioMetrics = validAudio,
            sourceOrigin = IngressSourceOrigin.LAPTOP
        )

        val g2 = specimen.gates.find { it.gateId == "G2" }
        assertNotNull(g2)
        assertEquals(GateStatus.PASS, g2!!.status)
        assertTrue("G2 score must be >= 0.90 for Compliant", g2.score >= 0.90f)
        assertTrue(g2.detailedEvidence.any { it.contains("BAND: 90–100 COMPLIANT") || it.contains("G2-COMPLIANT") })
    }

    @Test
    fun test06_G2DiagnosticEvaluator_TolerantBand_80To89Score_NeedsMinorRepair() {
        // Specimen with moderate physical anchors (3 anchors -> 65 + 21 = 86 pts)
        val tolerantLyric = """
            [Verse 1]
            I left my silver coin upon the wooden chair,
            A quiet whisper drifting through the midnight air.
            We watch the steady candle flicker in the dark,
            While distant whispers fade across the quiet park.
            
            [Verse 2]
            The quiet evening settles down across the floor,
            Before the traveler walks along the grassy path.
        """.trimIndent()

        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Tolerant Structure",
            rawLyric = tolerantLyric,
            audioMetrics = null,
            sourceOrigin = IngressSourceOrigin.LOCAL_FOLDER
        )

        val g2 = specimen.gates.find { it.gateId == "G2" }
        assertNotNull(g2)
        assertTrue("G2 score should be in 0.80..0.89 range (was ${g2!!.score})", g2.score in 0.80f..0.89f)
        assertTrue(g2.detailedEvidence.any { it.contains("G2-TOLERANT") })
    }

    @Test
    fun test07_G2DiagnosticEvaluator_FailureBand_Under70Score() {
        // Lyric containing prohibited generic cliché tropes
        val failureLyric = """
            [Verse 1]
            A neon tapestry of dreams across the endless sky,
            In a symphony of stars we watch the shadows fly.
            A labyrinth of thoughts lost in the deep abyss.
        """.trimIndent()

        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Drift/Failure Specimen",
            rawLyric = failureLyric,
            audioMetrics = null,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT
        )

        val g2 = specimen.gates.find { it.gateId == "G2" }
        assertNotNull(g2)
        assertTrue("G2 score must be < 0.70 for Failure (was ${g2!!.score})", g2.score < 0.70f)
        assertTrue(g2.detailedEvidence.any { it.contains("G2-FAILURE") })
    }

    @Test
    fun test08_G3PerformanceCalibration_StructuralCadence() {
        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Performance Calibrated Piece",
            rawLyric = fullValidLyric,
            audioMetrics = validAudio,
            sourceOrigin = IngressSourceOrigin.LAPTOP
        )

        val g3 = specimen.gates.find { it.gateId == "G3" }
        assertNotNull(g3)
        assertEquals(GateStatus.PASS, g3!!.status)
        assertTrue(g3.detailedEvidence.any { it.contains("Baseline Profile") })
    }

    @Test
    fun test09_G4InformationalAndG6DeferredPreservation() {
        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Informational Verification",
            rawLyric = fullValidLyric,
            audioMetrics = validAudio,
            sourceOrigin = IngressSourceOrigin.LAPTOP
        )

        val g4 = specimen.gates.find { it.gateId == "G4" }
        assertNotNull(g4)
        assertTrue("G4 is informational only", g4!!.detailedEvidence.any { it.contains("No numeric blocking thresholds") })

        val g6 = specimen.gates.find { it.gateId == "G6" }
        assertNotNull(g6)
        assertEquals(GateStatus.UNEXAMINED, g6!!.status)
        assertTrue(g6.detailedEvidence.any { it.contains("DEFERRED / NOT SPECIFIED") || it.contains("Excluded") })
    }

    @Test
    fun test10_G5HumanGovernorProtocol_AndFourDispositionPathways() {
        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "Governor Triage Specimen",
            rawLyric = fullValidLyric,
            audioMetrics = validAudio,
            sourceOrigin = IngressSourceOrigin.LAPTOP
        )

        // Pathway 1: Release / Master Accept
        val acceptVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version = specimen,
            choice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            governorNotes = "Certified survivor quality; all G1-G3 passed."
        )
        assertEquals(SpecimenDecision.ACCEPT, acceptVersion.decision)
        assertEquals(GovernanceDispositionChoice.RELEASE_ACCEPT, acceptVersion.g5Disposition.chosenDisposition)
        assertTrue(acceptVersion.g5Disposition.chosenDisposition.routingTarget.contains("ELYZARETH_FINAL/"))
        assertFalse(acceptVersion.canHeal)
        assertTrue(acceptVersion.historyTrail.any { it.action == "G5_HUMAN_DISPOSITION" })

        // Pathway 2: Minor Cure -> App 01 Engine
        val minorCureVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version = specimen,
            choice = GovernanceDispositionChoice.MINOR_CURE,
            governorNotes = "Minor line polish required."
        )
        assertEquals(SpecimenDecision.NEEDS_HEALING, minorCureVersion.decision)
        assertEquals(GovernanceDispositionChoice.MINOR_CURE, minorCureVersion.g5Disposition.chosenDisposition)
        assertTrue(minorCureVersion.g5Disposition.chosenDisposition.routingTarget.contains("Elyzareth Engine"))
        assertTrue(minorCureVersion.canHeal)

        // Pathway 3: Full Reconstruction -> App 01 Engine
        val rebuildVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version = specimen,
            choice = GovernanceDispositionChoice.FULL_RECONSTRUCTION,
            governorNotes = "Structural rewrite permitted under human supervision."
        )
        assertEquals(SpecimenDecision.NEEDS_HEALING, rebuildVersion.decision)
        assertEquals(GovernanceDispositionChoice.FULL_RECONSTRUCTION, rebuildVersion.g5Disposition.chosenDisposition)
        assertTrue(rebuildVersion.g5Disposition.chosenDisposition.routingTarget.contains("Elyzareth Engine"))
        assertTrue(rebuildVersion.canHeal)

        // Pathway 4: Permanent Reject -> Witness Preserved & Sealed
        val rejectVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version = specimen,
            choice = GovernanceDispositionChoice.PERMANENT_REJECT,
            governorNotes = "Specimen violates canon fidelity."
        )
        assertEquals(SpecimenDecision.NOT_ELIGIBLE, rejectVersion.decision)
        assertEquals(GovernanceDispositionChoice.PERMANENT_REJECT, rejectVersion.g5Disposition.chosenDisposition)
        assertTrue(rejectVersion.g5Disposition.chosenDisposition.routingTarget.contains("Halted & Sealed"))
        assertFalse(rejectVersion.canHeal)
        // Immutable witness remains preserved and hashed
        assertEquals(specimen.sha256Hash, rejectVersion.sha256Hash)
    }

    @Test
    fun test11_TextOnlySpecimen_G3AndG4NotMeasured_NoFabricatedMetrics_G5AwaitingGovernor() {
        val textOnlySpecimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = "The Silver Pocket Watch & Railway Platform",
            rawLyric = fullValidLyric,
            audioMetrics = null,
            sourceOrigin = IngressSourceOrigin.LAPTOP
        )

        // A. Text-only specimen -> G1 PASS
        val g1 = textOnlySpecimen.gates.find { it.gateId == "G1" }
        assertNotNull(g1)
        assertEquals(GateStatus.PASS, g1!!.status)
        assertEquals(AudioRegistrationStatus.NOT_APPLICABLE_PENDING_AUDIO_RENDER, textOnlySpecimen.g1Witness?.audioStatus)

        // B. Text-only specimen -> G2 measured
        val g2 = textOnlySpecimen.gates.find { it.gateId == "G2" }
        assertNotNull(g2)
        assertEquals(GateStatus.PASS, g2!!.status)
        assertNotNull(textOnlySpecimen.g2Diagnostic)
        assertEquals(G2DiagnosticBand.COMPLIANT, textOnlySpecimen.g2Diagnostic?.band)
        assertTrue(textOnlySpecimen.g2Diagnostic!!.physicalAnchorCount >= 3)

        // C. Text-only specimen -> G3 NOT MEASURED
        val g3 = textOnlySpecimen.gates.find { it.gateId == "G3" }
        assertNotNull(g3)
        assertEquals("G3 must be UNEXAMINED / NOT MEASURED when audio is null", GateStatus.UNEXAMINED, g3!!.status)
        assertFalse(textOnlySpecimen.g3Performance!!.isAudioMeasured)
        assertNull("Vocal naturalness score must NOT exist when audio is absent", textOnlySpecimen.g3Performance?.vocalNaturalnessScore)
        assertNull("Formant stability must NOT exist when audio is absent", textOnlySpecimen.g3Performance?.formantStability)
        assertNull("Pitch measurement must NOT exist when audio is absent", textOnlySpecimen.g3Performance?.pitchTranspositionInterval)
        assertNull("Cadence measurement must NOT exist when audio is absent", textOnlySpecimen.g3Performance?.cadenceNaturalness)
        assertTrue(g3.summary.contains("NOT MEASURED") && g3.summary.contains("Physical audio/vocal evidence unavailable"))

        // D. Text-only specimen -> G4 NOT MEASURED
        val g4 = textOnlySpecimen.gates.find { it.gateId == "G4" }
        assertNotNull(g4)
        assertEquals("G4 must be UNEXAMINED / NOT MEASURED when audio is null", GateStatus.UNEXAMINED, g4!!.status)
        assertFalse(textOnlySpecimen.g4Acoustic!!.isAudioMeasured)
        assertNull("Dry room observation must NOT be fabricated from lyric", textOnlySpecimen.g4Acoustic?.dryRoomCharacter)
        assertNull("T60 trend must NOT be fabricated from lyric", textOnlySpecimen.g4Acoustic?.t60QualitativeTrend)
        assertNull("Negative space observation must NOT be fabricated from lyric", textOnlySpecimen.g4Acoustic?.negativeSpaceObservation)
        assertTrue(g4.summary.contains("NOT MEASURED") && g4.summary.contains("Physical acoustic evidence unavailable"))

        // E. No audio-derived numeric values exist
        assertEquals(0.0f, g3.score, 0.001f)
        assertEquals(0.0f, g4.score, 0.001f)

        // F. G5 remains awaiting Human Governor
        val g5 = textOnlySpecimen.gates.find { it.gateId == "G5" }
        assertNotNull(g5)
        assertEquals(GateStatus.UNEXAMINED, g5!!.status)
        assertEquals(SpecimenDecision.NOT_YET_EXAMINED, textOnlySpecimen.decision)
        assertEquals(GovernanceDispositionChoice.PENDING_HUMAN_GOVERNOR, textOnlySpecimen.g5Disposition.chosenDisposition)
        assertFalse(textOnlySpecimen.g5Disposition.isAutomatedAI)
        assertFalse(textOnlySpecimen.g5Disposition.isHumanGovernorAuthorized)
    }
}
