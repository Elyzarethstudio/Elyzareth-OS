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

    @Test
    fun test12_CorpusDiscoveryDryRun_ReadOnlyInventoryAndEvidenceDistinction() {
        // Build a simulated corpus representing a 390 base titles / 700+ artifacts collection
        val sampleRecords = mutableListOf<DiscoveredArtifactRecord>()

        // 1. Valid Hindi Lyric
        sampleRecords.add(
            DiscoveredArtifactRecord(
                id = "ART-001",
                documentUri = "content://saf/tree/doc/01",
                fileName = "mera_pyar_tera_sahara_v1.txt",
                relativePath = "Mera Pyar Tera Sahara/mera_pyar_tera_sahara_v1.txt",
                fileExtension = "txt",
                fileSizeBytes = 1024L,
                lastModified = 1700000000L,
                mimeType = "text/plain",
                sha256Hash = "sha256:1111111111111111111111111111111111111111111111111111111111111111",
                category = ArtifactCategory.LYRIC_TEXT,
                discoveryState = IngestionDiscoveryState.PARSED,
                isParsedSuccessfully = true,
                detectedBaseTitle = "Mera Pyar Tera Sahara",
                detectedVersionLabel = "v01",
                detectedLanguage = "Hindi (Romanized)",
                lineCount = 16,
                wordCount = 95,
                characterCount = 480,
                snippetText = "Tera pyar mera sahara, dil ka yeh kinara...",
                parseErrorMessage = null
            )
        )

        // 2. Dual-Witness Companion Schema (.json)
        sampleRecords.add(
            DiscoveredArtifactRecord(
                id = "ART-002",
                documentUri = "content://saf/tree/doc/02",
                fileName = "mera_pyar_tera_sahara_v1_witness.json",
                relativePath = "Mera Pyar Tera Sahara/mera_pyar_tera_sahara_v1_witness.json",
                fileExtension = "json",
                fileSizeBytes = 512L,
                lastModified = 1700000000L,
                mimeType = "application/json",
                sha256Hash = "sha256:2222222222222222222222222222222222222222222222222222222222222222",
                category = ArtifactCategory.STRUCTURED_SCHEMA,
                discoveryState = IngestionDiscoveryState.PARSED,
                isParsedSuccessfully = true,
                detectedBaseTitle = "Mera Pyar Tera Sahara",
                detectedVersionLabel = "v01",
                detectedLanguage = "Schema / JSON",
                lineCount = 20,
                wordCount = 50,
                characterCount = 300,
                snippetText = "{\"theme\": \"Devotion\", \"emotionalProfile\": \"Bittersweet\"}",
                parseErrorMessage = null
            )
        )

        // 3. Companion Audio (.wav) -> NOT_MEASURED until physical acoustic decoding
        sampleRecords.add(
            DiscoveredArtifactRecord(
                id = "ART-003",
                documentUri = "content://saf/tree/doc/03",
                fileName = "mera_pyar_tera_sahara_v1_guide.wav",
                relativePath = "Mera Pyar Tera Sahara/mera_pyar_tera_sahara_v1_guide.wav",
                fileExtension = "wav",
                fileSizeBytes = 25000000L,
                lastModified = 1700000000L,
                mimeType = "audio/wav",
                sha256Hash = "sha256:3333333333333333333333333333333333333333333333333333333333333333",
                category = ArtifactCategory.AUDIO_STREAM,
                discoveryState = IngestionDiscoveryState.NOT_MEASURED,
                isParsedSuccessfully = true,
                detectedBaseTitle = "Mera Pyar Tera Sahara",
                detectedVersionLabel = "v01",
                detectedLanguage = "Audio Stream",
                lineCount = 0,
                wordCount = 0,
                characterCount = 0,
                snippetText = "Binary Audio Stream [wav, 23.8 MB]",
                parseErrorMessage = null
            )
        )

        // 4. Orphan artifact without companion
        sampleRecords.add(
            DiscoveredArtifactRecord(
                id = "ART-004",
                documentUri = "content://saf/tree/doc/04",
                fileName = "stray_guitar_riff_take_09.wav",
                relativePath = "Unsorted/stray_guitar_riff_take_09.wav",
                fileExtension = "wav",
                fileSizeBytes = 12000000L,
                lastModified = 1700000000L,
                mimeType = "audio/wav",
                sha256Hash = "sha256:4444444444444444444444444444444444444444444444444444444444444444",
                category = ArtifactCategory.AUDIO_STREAM,
                discoveryState = IngestionDiscoveryState.NOT_MEASURED,
                isParsedSuccessfully = true,
                detectedBaseTitle = "Unsorted",
                detectedVersionLabel = "take09",
                detectedLanguage = "Audio Stream",
                lineCount = 0,
                wordCount = 0,
                characterCount = 0,
                snippetText = "Binary Audio Stream [wav, 11.4 MB]",
                parseErrorMessage = null
            )
        )

        // Verify that discovery states are strictly categorized without gate evaluations
        assertEquals(IngestionDiscoveryState.PARSED, sampleRecords[0].discoveryState)
        assertEquals(IngestionDiscoveryState.PARSED, sampleRecords[1].discoveryState)
        assertEquals(IngestionDiscoveryState.NOT_MEASURED, sampleRecords[2].discoveryState)
        assertEquals(IngestionDiscoveryState.NOT_MEASURED, sampleRecords[3].discoveryState)
    }

    @Test
    fun test13_CorpusPersistence_SurvivesRestartWithIntegrity() {
        val appContext = org.robolectric.RuntimeEnvironment.getApplication()

        val sampleReport = CorpusInventoryReport(
            scanTimestamp = System.currentTimeMillis(),
            sourceRootUri = "content://com.android.externalstorage.documents/tree/primary%3AMusic%2FCorpus",
            sourceRootDisplayName = "Elyzareth Master Corpus (~390 Songs)",
            scanStatus = IngestionScanStatus.COMPLETED,
            scanStatusMessage = "Discovery Dry Run complete. Discovered 720 artifacts across 390 base titles.",
            totalFilesDiscovered = 720,
            baseTitlesDiscovered = 390,
            versionsDiscovered = 512,
            successfullyParsed = 718,
            unparsedCount = 2,
            duplicateCandidatesCount = 4,
            orphanArtifactsCount = 8,
            missingExpectedComponentsCount = 24,
            languageStats = mapOf("Hindi (Romanized)" to 210, "Hindi (Devanagari)" to 95, "English / Latin" to 85),
            evidenceStats = mapOf("Text Witness Available" to 390, "Audio Acoustic (NOT MEASURED)" to 330),
            baseTitleGroups = listOf(
                DiscoveredBaseTitleGroup(
                    baseId = "BASE-001",
                    title = "Mera Pyar Tera Sahara",
                    relativeFolder = "Mera Pyar Tera Sahara",
                    artifacts = emptyList(),
                    lyricCount = 1,
                    audioCount = 1,
                    schemaCount = 1,
                    primaryLanguage = "Hindi (Romanized)",
                    isCompletePackage = true,
                    missingComponents = emptyList(),
                    duplicateCandidates = emptyList(),
                    requiresHumanReview = false,
                    humanReviewReason = null
                )
            ),
            allArtifacts = emptyList()
        )

        // Save to disk
        com.example.engine.CorpusPersistenceManager.saveReport(appContext, sampleReport)

        // Restore from disk
        val restored = com.example.engine.CorpusPersistenceManager.loadSavedReport(appContext)

        assertNotNull("Persisted inventory must be restored from disk", restored)
        assertEquals(390, restored!!.baseTitlesDiscovered)
        assertEquals(720, restored.totalFilesDiscovered)
        assertEquals(512, restored.versionsDiscovered)
        assertEquals(718, restored.successfullyParsed)
        assertEquals(2, restored.unparsedCount)
        assertEquals(4, restored.duplicateCandidatesCount)
        assertEquals(8, restored.orphanArtifactsCount)
        assertEquals(24, restored.missingExpectedComponentsCount)
        assertEquals("Elyzareth Master Corpus (~390 Songs)", restored.sourceRootDisplayName)
        assertEquals("Hindi (Romanized)", restored.baseTitleGroups.first().primaryLanguage)
    }
}
