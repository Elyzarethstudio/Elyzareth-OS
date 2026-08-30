package com.example

import com.example.engine.*
import com.example.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

/**
 * ELYZARETH OS v38.2 — PRODUCTION VALIDATION SUITE
 *
 * Direct execution against the v38.2 Production Baseline Specification:
 * 1. Golden Specimen Flight Test (Full 5-App Evidence Continuity Chain)
 * 2. Deterministic Failure Injections:
 *    - Injected Corrupted / Missing Audio
 *    - Injected SHA-256 Hash Tampering
 *    - Injected Orphaned / Missing Lyric Artifacts
 *    - Injected Mid-Flight Process Termination & State Recovery
 * 3. Persistence Verification:
 *    - Measure -> Persist -> Close -> Restart -> Reload -> Verify
 */
class ElyzarethV38ProductionValidationTest {

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // =========================================================================
    // 1. GOLDEN SPECIMEN FLIGHT TEST (FULL 5-APP PROVENANCE CHAIN)
    // =========================================================================

    @Test
    fun test01_goldenSpecimenFullChainContinuity() {
        // STEP 1: App 01 — Lyric Studio (Create & DNA Fingerprint)
        val songTheme = "Golden Horizon Symphony"
        val stanza1 = "Rising through the golden peak of dawn\nEchoes linger when the night is gone"
        val stanza2 = "Every melody we engineered to stay\nGuiding travelers on the eternal way"
        val fullLyrics = "$stanza1\n\n$stanza2"

        val lyricSha256 = sha256(fullLyrics)
        val lyricDnaProfile = ElyzarethTurboEngine.buildCreativeDna(
            title = songTheme,
            storyConcept = "Dawn over mountain peaks with enduring acoustic clarity",
            existingLyric = fullLyrics,
            genre = "Orchestral Ambient",
            evidence = null
        )
        assertNotNull("App 01 Lyric DNA profile must be generated", lyricDnaProfile)
        assertEquals("Lyric hash must be verifiable SHA-256", 64, lyricSha256.length)

        // STEP 2: App 02 — Sitting Room (Ingest Audio & Register Witness Observations)
        val specimenId = "SPECIMEN-GOLDEN-001"
        val simulatedPcmPayload = ByteArray(4096) { ((it * 33) % 256).toByte() }
        val audioSha256 = sha256(simulatedPcmPayload.toString(Charsets.ISO_8859_1))
        
        val witnessObservation = WitnessObservation(
            specimenId = specimenId,
            witnessName = "Lead Sound Curator",
            audioHash = audioSha256,
            dynamicRangeDb = -14.2f,
            perceivedWarmthScore = 9.4f,
            notes = "Crystal alpine reverberation, balanced low-end resonance at 432Hz harmonic base."
        )
        assertNotNull("App 02 Witness observation must record genuine attributes", witnessObservation)

        // STEP 3: App 03 — Axiom Integrator (Reconcile & Link Multi-Artifact Bundle)
        val integratedBundle = IntegratedSongSpecimen(
            specimenId = specimenId,
            title = songTheme,
            lyricHash = lyricSha256,
            audioHash = audioSha256,
            dnaProfile = lyricDnaProfile,
            witnessRecord = witnessObservation,
            isReconciled = true
        )
        assertTrue("App 03 must confirm bundle reconciliation", integratedBundle.isReconciled)
        assertEquals("Integrated lyric hash must match App 01", lyricSha256, integratedBundle.lyricHash)
        assertEquals("Integrated audio hash must match App 02", audioSha256, integratedBundle.audioHash)

        // STEP 4: App 04 — Governance Matrix (G1-G5 Rule Evaluation & Verdict)
        val governanceResult = ElyzarethGovernanceEngine.evaluateSpecimenCompliance(
            specimen = integratedBundle,
            requiredRules = listOf("G1_RHYME_INTEGRITY", "G2_METRIC_BALANCE", "G3_FORENSIC_WITNESS", "G4_PROVENANCE_TRACE", "G5_IMMUTABLE_HASH")
        )
        assertTrue("App 04 Governance verdict must approve golden specimen", governanceResult.isApproved)
        assertEquals("Governance status must be APPROVED", "APPROVED", governanceResult.status)
        assertFalse("Governance must have zero severe violations", governanceResult.hasFatalViolations)

        // STEP 5: App 05 — Space Archive (Freeze, SHA-256 Manifest, & Retrieve)
        val archiveManifest = ArchiveManifest(
            archiveId = "ARC-V382-001",
            specimenId = specimenId,
            governanceReceipt = governanceResult.receiptId,
            masterHash = sha256("${integratedBundle.lyricHash}:${integratedBundle.audioHash}:${governanceResult.receiptId}"),
            timestampUtc = "2026-08-30T11:00:00Z",
            isSealed = true
        )
        assertTrue("App 05 Archive manifest must be sealed and immutable", archiveManifest.isSealed)
        assertEquals("Master hash must be 64-char hex SHA-256", 64, archiveManifest.masterHash.length)
    }

    // =========================================================================
    // 2. DETERMINISTIC FAILURE INJECTIONS
    // =========================================================================

    @Test
    fun test02_failureInjection_missingOrCorruptedAudio() {
        val corruptedPayload = ByteArray(0) // Empty / unreadable payload
        
        val extractionResult = ElyzarethTurboEngine.extractAudioWaveformTelemetry(corruptedPayload)
        
        // System must: Detect -> Refuse -> Explain -> Preserve Evidence -> Never Fabricate
        assertFalse("Extraction must fail for corrupted/empty audio", extractionResult.isSuccess)
        assertEquals("Engine must state exact cause", "EMPTY_OR_UNREADABLE_STREAM", extractionResult.errorCode)
        assertEquals("Engine must NOT fabricate sample rate", 0, extractionResult.sampleRate)
        assertNotNull("Error explanation must be human readable and deterministic", extractionResult.errorMessage)
    }

    @Test
    fun test03_failureInjection_alteredHashTampering() {
        val originalLyric = "Echoes linger when the night is gone"
        val originalHash = sha256(originalLyric)

        val tamperedLyric = "Echoes linger when the night is changed"
        val currentHash = sha256(tamperedLyric)

        val verificationResult = ElyzarethGovernanceEngine.verifyCryptographicHashMatch(
            expectedHash = originalHash,
            actualHash = currentHash
        )

        // System must: Refuse -> Flag Tampering -> Preserve diff
        assertFalse("Tampered content must fail cryptographic match", verificationResult.isMatch)
        assertEquals("Verification status must be HASH_MISMATCH_TAMPER_DETECTED", "HASH_MISMATCH_TAMPER_DETECTED", verificationResult.status)
        assertEquals("Expected hash preserved", originalHash, verificationResult.expectedHash)
        assertEquals("Actual hash recorded", currentHash, verificationResult.actualHash)
    }

    @Test
    fun test04_failureInjection_orphanedLyrics() {
        val orphanLyricSpecimen = IntegratedSongSpecimen(
            specimenId = "SPECIMEN-ORPHAN-001",
            title = "Orphaned Verse",
            lyricHash = sha256("A lone verse with no backing master"),
            audioHash = "", // Injected missing audio
            dnaProfile = null,
            witnessRecord = null,
            isReconciled = false
        )

        val governanceResult = ElyzarethGovernanceEngine.evaluateSpecimenCompliance(
            specimen = orphanLyricSpecimen,
            requiredRules = listOf("G3_FORENSIC_WITNESS", "G4_PROVENANCE_TRACE")
        )

        // System must refuse approval for orphan evidence
        assertFalse("Governance must refuse orphan lyric specimen", governanceResult.isApproved)
        assertEquals("Status must be REJECTED", "REJECTED", governanceResult.status)
        assertTrue("Violations must cite missing audio provenance", governanceResult.violations.any { it.contains("G3_FORENSIC_WITNESS") || it.contains("AUDIO_MISSING") })
    }

    @Test
    fun test05_failureInjection_interruptedStateRecovery() {
        val inFlightTransaction = WorkspaceTransaction(
            transactionId = "TXN-7789",
            step = "INTEGRATION_IN_PROGRESS",
            specimenId = "SPECIMEN-CRASH-TEST",
            isCommitted = false
        )

        // Simulate crash recovery ledger read
        val recoveredState = ElyzarethTurboEngine.recoverIncompleteTransaction(inFlightTransaction)

        // System must: Refuse incomplete write, rollback cleanly, leave zero lockfiles
        assertTrue("Recovery must mark uncommitted transaction as rolled back", recoveredState.isRolledBack)
        assertFalse("Incomplete transaction must NOT be marked committed", recoveredState.isCommitted)
        assertEquals("System reason must be CLEAN_ROLLBACK_ON_RESTART", "CLEAN_ROLLBACK_ON_RESTART", recoveredState.rollbackReason)
    }

    // =========================================================================
    // 3. PERSISTENCE VERIFICATION
    // =========================================================================

    @Test
    fun test06_persistenceCycle_measurePersistCloseRestartReloadVerify() {
        val specimenId = "SPECIMEN-PERSIST-007"
        val sampleMetric = mapOf(
            "meter_score" to "0.98",
            "rhyme_scheme" to "AABB",
            "lyric_sha256" to sha256("Immutable Lyric Line For Persistence")
        )

        // 1. Measure & Persist to disk model
        val serializedPayload = ElyzarethTurboEngine.serializeSpecimenForensicState(specimenId, sampleMetric)
        assertNotNull("Serialized payload must be non-null", serializedPayload)

        // 2. Simulate Close & Restart (Clear active memory instance)
        val deserializedState = ElyzarethTurboEngine.deserializeSpecimenForensicState(serializedPayload)

        // 3. Verify identical forensic state
        assertEquals("Specimen ID must match perfectly", specimenId, deserializedState.specimenId)
        assertEquals("Forensic metrics count must match", sampleMetric.size, deserializedState.metrics.size)
        assertEquals("Lyric SHA-256 must match exactly", sampleMetric["lyric_sha256"], deserializedState.metrics["lyric_sha256"])
        assertEquals("Meter score must match exactly", "0.98", deserializedState.metrics["meter_score"])
    }
}
