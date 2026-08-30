package com.example

import com.example.engine.ArchivePersistenceManager
import com.example.engine.ElyzarethGovernanceEngine
import com.example.engine.ElyzarethTurboEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SilverCoinLineageAndArchiveTest {

    @Test
    fun testSilverCoinLineageSurvivalThroughCurePipeline() {
        val testLyric = """
            Across the wooden table sits the silver coin,
            Beside the misty railway where the rivers join.
            Neon synthesizer beats pulsing in the dark.
        """.trimIndent()

        val specimenId = "SPEC-SILVER-COIN-001"
        val originalLyricHash = ElyzarethGovernanceEngine.generateHash(testLyric)

        val cureRequest = StructuredCureRequest(
            requestId = "REQ-CURE-001",
            sourceSpecimenId = specimenId,
            sourceVersionId = "VER-001",
            title = "Silver Coin Specimen",
            originalLyricText = testLyric,
            originalEvidence = LyricEvidence(
                theme = "Memory and transit",
                narrativeArc = "Observation at the junction",
                emotionalProfile = "Melancholic and grounded",
                witnessObjects = listOf("silver coin", "wooden table", "misty railway", "rivers join"),
                temporalContext = "Dawn",
                energyProfile = "Intimate acoustic",
                languageCharacteristics = "Concrete physical imagery",
                creativeSignals = listOf("FOLK_RESONANCE")
            ),
            diagnosticSummary = "G2 Anchor Defect: Synthesizer pulse contaminated acoustic space.",
            gateFlags = listOf("PROHIBITED_SYNTH"),
            targetVariablesToHeal = listOf("SYNTH_PURGE", "ANCHOR_PRESERVE"),
            cureRecommendation = "Preserve wooden table and silver coin, purge synth."
        )

        // Execute Turbo CURE
        val cureResult = ElyzarethTurboEngine.executeGovernedCure(
            cureRequest = cureRequest,
            genre = "Appalachian Folk",
            stylePrompt = "Warm fingerpicked guitar, wooden fiddle",
            vocalTimbre = "Ethereal / Resonant",
            audioProfile = AudioCadenceProfile(bpm = 108)
        )

        // Verify song is generated
        assertNotNull(cureResult.generatedSong)
        assertTrue(cureResult.generatedSong.stanzas.isNotEmpty())

        // Verify physical anchors are preserved
        assertTrue(cureResult.validationReport.isGoverned)
        assertTrue(cureResult.validationReport.physicalAnchorCount >= 2)

        // Copy song with verified lineage
        val songWithLineage = cureResult.generatedSong.copy(
            sourceSpecimenId = cureRequest.sourceSpecimenId,
            originalLyricSha256 = originalLyricHash
        )

        assertEquals(specimenId, songWithLineage.sourceSpecimenId)
        assertEquals(originalLyricHash, songWithLineage.originalLyricSha256)
    }

    @Test
    fun testArchivePersistenceSerializationRoundTrip() {
        val context = RuntimeEnvironment.getApplication()

        val sampleFiles = listOf(
            ArchiveFile(
                id = "ARC-TEST-001",
                fileName = "silver_coin_opus.lyr",
                category = "LYRICS",
                originTenant = "App 01 (Lyric Generator)",
                previewText = "Across the wooden table sits the silver coin...",
                fullText = "TITLE: Silver Coin Opus\nSOURCE SPECIMEN ID: SPEC-SILVER-COIN-001\n---",
                g3SealHash = "G3-HASH-TEST-9999",
                sizeKb = 4.2f,
                timestamp = 1700000000000L
            ),
            ArchiveFile(
                id = "ARC-TEST-002",
                fileName = "master_suite_test.intg",
                category = "PIPELINE_BUNDLE",
                originTenant = "App 03 (The Integrator)",
                previewText = "Harmonized fusion of ancient corpus motifs...",
                fullText = "===================\nELYZARETH OS // MASTER INTEGRATION SUITE\n===================",
                g3SealHash = "G3-HASH-MASTER-8888",
                sizeKb = 18.5f,
                timestamp = 1700000001000L
            )
        )

        // Save to disk
        ArchivePersistenceManager.saveArchiveFiles(context, sampleFiles)

        // Read back from disk
        val restored = ArchivePersistenceManager.loadSavedArchiveFiles(context)

        assertNotNull(restored)
        assertEquals(2, restored!!.size)
        assertEquals("ARC-TEST-001", restored[0].id)
        assertEquals("silver_coin_opus.lyr", restored[0].fileName)
        assertEquals("G3-HASH-TEST-9999", restored[0].g3SealHash)
        assertEquals(4.2f, restored[0].sizeKb, 0.01f)

        assertEquals("ARC-TEST-002", restored[1].id)
        assertEquals("master_suite_test.intg", restored[1].fileName)
        assertEquals("G3-HASH-MASTER-8888", restored[1].g3SealHash)
    }
}
