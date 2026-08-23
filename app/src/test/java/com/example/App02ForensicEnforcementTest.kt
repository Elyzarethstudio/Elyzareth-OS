package com.example

import com.example.engine.ElyzarethGovernanceEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest
import kotlin.math.sin

/**
 * App 02 Forensic Enforcement & Cryptographic Witness Gate Test
 *
 * Verifies:
 * 1. Specimen with valid JSON witness schema but NO valid PCM/binary evidence produces
 *    audio witness metrics with NOT_MEASURED and prevents Dual-Witness G1 promotion (pathType != DUAL_VERIFIED).
 * 2. Generating a valid binary PCM payload, computing its true SHA-256 hash, decoding/validating the PCM stream.
 * 3. Verifying that ONLY after BOTH witnesses (Valid JSON Schema + Valid PCM Binary) are validated
 *    can G1 unlock as DUAL_VERIFIED with valid certificates for both text and PCM.
 * 4. Mutating the binary payload, computing the new SHA-256 hash, invalidating the cached evidence,
 *    and verifying that the specimen cannot reuse the old G1 result or certificate.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class App02ForensicEnforcementTest {

    private val lyricTitle = "Sovereign Horizon Master"
    private val lyricText = """
        [Verse 1]
        Inside an old gray coat I found a silver coin,
        Beside the misty railway where the rivers join.
        The winter rain was falling on the windowpane,
        Remembering what was spoken on the midnight train.

        [Chorus]
        Hold to the ground when the sky gives way,
        Deeper than the light of the dying day.
        Stand with the mountain, breathe with the tree,
        Sovereign and rooted, unbroken and free.
    """.trimIndent()

    private val validLyricEvidence = LyricEvidence(
        theme = "Endurance & Physical Anchors",
        narrativeArc = "Discovery of tactile coin leading to sovereign ground stand",
        emotionalProfile = "Steadfast, resonant, reflective",
        witnessObjects = listOf("silver coin", "old gray coat", "railway", "windowpane", "midnight train"),
        temporalContext = "Winter era",
        energyProfile = "Restrained acoustic rise",
        languageCharacteristics = "Concrete sensory anchors, iambic symmetry",
        creativeSignals = listOf("ORGANIC_ACOUSTIC_GRAVITAS"),
        suggestedSonicVocabulary = listOf("acoustic guitar", "upright bass", "chamber strings")
    )

    private fun generateSyntheticPcmPayload(sampleCount: Int, frequency: Double, sampleRate: Int = 44100): ByteArray {
        val bytes = ByteArray(sampleCount * 2) // 16-bit mono
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleRate
            val sample = (sin(2.0 * Math.PI * frequency * t) * 16000).toInt().coerceIn(-32767, 32767)
            bytes[i * 2] = (sample and 0xFF).toByte()
            bytes[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
            }
        return bytes
    }

    private fun generateSynthetic16BitPcmSineWave(frequencyHz: Double, durationSeconds: Double, sampleRate: Int = 44100): ByteArray {
        val sampleCount = (sampleRate * durationSeconds).toInt()
        return generateSyntheticPcmPayload(sampleCount, frequencyHz, sampleRate)
    }

    private fun calculateSha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return "sha256:" + digest.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun test_app02_forensicEnforcement_dualWitnessValidationAndBinaryMutation() {
        // Validate that the JSON witness schema alone is 100% valid
        val schemaErrors = ElyzarethGovernanceEngine.validateLyricEvidenceSchema(validLyricEvidence)
        assertTrue("JSON Witness Schema must have 0 errors", schemaErrors.isEmpty())

        // -------------------------------------------------------------
        // STEP 1: Evaluate specimen with Valid JSON witness but NO PCM/binary evidence
        // -------------------------------------------------------------
        val emptyPcmBytes = ByteArray(0)
        val unmeasuredAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(emptyPcmBytes)

        assertFalse("Unmeasured audio witness must not have isMeasured=true", unmeasuredAudioMetrics.isMeasured)
        assertEquals("NOT_MEASURED", unmeasuredAudioMetrics.pcmStatus)
        assertEquals("FAIL_NO_DATA", unmeasuredAudioMetrics.decoderStatus)

        // Evaluate specimen through G1 Witness Gate with unmeasured audio
        val g1ResultWithoutAudio = ElyzarethGovernanceEngine.evaluateG1Witness(
            rawLyric = lyricText,
            title = lyricTitle,
            evidence = validLyricEvidence,
            audioMetrics = unmeasuredAudioMetrics
        )

        // Check that G1 cannot promote to DUAL_VERIFIED
        assertNotEquals("G1 must NOT promote to DUAL_VERIFIED without valid binary PCM evidence",
            G1PathType.DUAL_VERIFIED, g1ResultWithoutAudio.pathType)
        assertEquals(AudioRegistrationStatus.NOT_APPLICABLE_PENDING_AUDIO_RENDER, g1ResultWithoutAudio.audioStatus)
        assertNull("Audio certificate must be null when PCM evidence is not measured", g1ResultWithoutAudio.audioWitnessCertificate)
        assertNotNull("Text certificate is valid", g1ResultWithoutAudio.textWitnessCertificate)

        // Evaluate complete specimen pipeline
        val specimenWithoutAudio = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = lyricTitle,
            rawLyric = lyricText,
            audioMetrics = unmeasuredAudioMetrics,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT,
            preSuppliedEvidence = validLyricEvidence
        )

        // G3 and G4 must produce NOT_MEASURED in diagnostic rails when physical audio is absent
        assertFalse(specimenWithoutAudio.g3Performance?.isAudioMeasured ?: false)
        assertNull(specimenWithoutAudio.g3Performance?.vocalNaturalnessScore)
        assertTrue(specimenWithoutAudio.g3Performance?.diagnosticNotes?.contains("NOT MEASURED") == true)
        assertFalse(specimenWithoutAudio.g4Acoustic?.isAudioMeasured ?: false)
        assertTrue(specimenWithoutAudio.g4Acoustic?.statusNote?.contains("NOT MEASURED") == true)

        // -------------------------------------------------------------
        // STEP 2: Create valid binary PCM payload, compute real SHA-256, decode & validate
        // -------------------------------------------------------------
        val validPcmBytes = generateSyntheticPcmPayload(sampleCount = 44100 * 2, frequency = 440.0, sampleRate = 44100) // 2 seconds of 440Hz tone
        val calculatedSha256 = calculateSha256(validPcmBytes)
        val engineCalculatedSha256 = ElyzarethGovernanceEngine.calculateBinarySha256(validPcmBytes)

        assertEquals("Real computed SHA-256 must match Engine binary hash utility", calculatedSha256, engineCalculatedSha256)
        assertTrue("SHA-256 hash must be formatted correctly", calculatedSha256.startsWith("sha256:"))

        val measuredAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(validPcmBytes, sampleRateKhz = 44.1f, channels = 1)

        assertTrue("Measured audio must have isMeasured=true", measuredAudioMetrics.isMeasured)
        assertEquals("PASS", measuredAudioMetrics.decoderStatus)
        assertEquals("VERIFIED", measuredAudioMetrics.pcmStatus)
        assertEquals("VERIFIED", measuredAudioMetrics.transientStatus)
        assertEquals("VERIFIED", measuredAudioMetrics.fingerprintStatus)
        assertEquals(calculatedSha256, measuredAudioMetrics.physicalFileHash)
        assertTrue("Decoded peakDb must be realistic", measuredAudioMetrics.peakDb > -90.0f && measuredAudioMetrics.peakDb <= 0.0f)

        // -------------------------------------------------------------
        // STEP 3: Unlock G1 as DUAL_VERIFIED only after BOTH witnesses are valid
        // -------------------------------------------------------------
        val g1DualResult = ElyzarethGovernanceEngine.evaluateG1Witness(
            rawLyric = lyricText,
            title = lyricTitle,
            evidence = validLyricEvidence,
            audioMetrics = measuredAudioMetrics
        )

        assertTrue("G1 must be valid when both text and physical audio are verified", g1DualResult.isValid)
        assertEquals("G1 must unlock as DUAL_VERIFIED", G1PathType.DUAL_VERIFIED, g1DualResult.pathType)
        assertEquals(AudioRegistrationStatus.PHYSICAL_AUDIO_VERIFIED, g1DualResult.audioStatus)
        assertNotNull("Audio witness certificate must be generated", g1DualResult.audioWitnessCertificate)
        assertNotNull("Text witness certificate must be generated", g1DualResult.textWitnessCertificate)
        assertTrue("Audio certificate must embed hash prefix", g1DualResult.audioWitnessCertificate!!.startsWith("G1-CERT-PCM-"))

        val fullDualSpecimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = lyricTitle,
            rawLyric = lyricText,
            audioMetrics = measuredAudioMetrics,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT,
            preSuppliedEvidence = validLyricEvidence
        )

        val g1GateDiag = fullDualSpecimen.gates.find { it.gateId == "G1" }
        assertNotNull(g1GateDiag)
        assertEquals(GateStatus.PASS, g1GateDiag!!.status)
        assertTrue(g1GateDiag.summary.contains("Dual Text+Audio Witness Verified"))

        // -------------------------------------------------------------
        // STEP 4: Mutate binary payload, verify SHA-256 changes, invalidate evidence, confirm old G1 result cannot be reused
        // -------------------------------------------------------------
        val mutatedPcmBytes = validPcmBytes.clone()
        // Corrupt and mutate the audio binary bytes (e.g. introduce high entropy byte inversion and truncation)
        for (i in 1000..5000) {
            mutatedPcmBytes[i] = (mutatedPcmBytes[i].toInt() xor 0xFF).toByte()
        }

        val mutatedSha256 = ElyzarethGovernanceEngine.calculateBinarySha256(mutatedPcmBytes)
        assertNotEquals("Mutated binary MUST produce a different SHA-256 hash", calculatedSha256, mutatedSha256)

        // Decode mutated audio
        val mutatedAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(mutatedPcmBytes, sampleRateKhz = 44.1f, channels = 1)
        assertEquals(mutatedSha256, mutatedAudioMetrics.physicalFileHash)
        assertNotEquals("Old fingerprint/hash cannot be reused", measuredAudioMetrics.physicalFileHash, mutatedAudioMetrics.physicalFileHash)

        // Evaluate G1 with mutated audio
        val mutatedG1Result = ElyzarethGovernanceEngine.evaluateG1Witness(
            rawLyric = lyricText,
            title = lyricTitle,
            evidence = validLyricEvidence,
            audioMetrics = mutatedAudioMetrics
        )

        // Verify the certificate was invalidated and regenerated with the new hash
        assertNotEquals("Old audio certificate must NOT be reused by mutated specimen",
            g1DualResult.audioWitnessCertificate, mutatedG1Result.audioWitnessCertificate)
        assertEquals("New audio certificate must reflect mutated hash",
            "G1-CERT-PCM-${mutatedSha256.take(16).uppercase()}", mutatedG1Result.audioWitnessCertificate)

        // Now test corrupted stream rejection
        val corruptedBytes = ByteArray(2) { 0x00 } // truncated below minimum sample frame
        val corruptAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(corruptedBytes)
        assertEquals("FAIL", corruptAudioMetrics.decoderStatus)

        val corruptG1Result = ElyzarethGovernanceEngine.evaluateG1Witness(
            rawLyric = lyricText,
            title = lyricTitle,
            evidence = validLyricEvidence,
            audioMetrics = corruptAudioMetrics
        )

        assertFalse("G1 Static Integrity Gate MUST FAIL when audio evidence is corrupted/failed", corruptG1Result.isValid)
        assertEquals(AudioRegistrationStatus.PHYSICAL_AUDIO_FAILED, corruptG1Result.audioStatus)
        assertNull("Audio certificate must be null on failed PCM evidence", corruptG1Result.audioWitnessCertificate)
    }

    @Test
    fun test02_G4HumanGovernorAuthorizationBoundary_RejectsAIAndStaleAuthorization() {
        val validPcmBytes = generateSynthetic16BitPcmSineWave(frequencyHz = 440.0, durationSeconds = 2.0, sampleRate = 44100)
        val measuredAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(validPcmBytes, sampleRateKhz = 44.1f, channels = 1)
        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = lyricTitle,
            rawLyric = lyricText,
            audioMetrics = measuredAudioMetrics,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT,
            preSuppliedEvidence = validLyricEvidence
        )

        // 1. Authorized by Human -> VALID
        val humanAuth = ElyzarethGovernanceEngine.createHumanGovernorAuthorization(
            specimen = specimen,
            dispositionChoice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            governorNotes = "Authorized by Lead Human Curator",
            governorIdentity = "HUMAN_CURATOR_01",
            isAutomatedAI = false
        )
        val validationSuccess = ElyzarethGovernanceEngine.verifyHumanGovernorAuthorization(humanAuth, specimen)
        assertTrue("Human authorization must be valid", validationSuccess is AuthorizationValidationResult.Valid)

        // 2. Automated AI self-authorization -> REJECTED
        val aiAuth = ElyzarethGovernanceEngine.createHumanGovernorAuthorization(
            specimen = specimen,
            dispositionChoice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            governorNotes = "Autonomous AI system approval",
            governorIdentity = "AI_EVALUATION_BOT",
            isAutomatedAI = true
        )
        val aiValidation = ElyzarethGovernanceEngine.verifyHumanGovernorAuthorization(aiAuth, specimen)
        assertTrue("AI self-authorization must be rejected", aiValidation is AuthorizationValidationResult.AutomatedAiRejected)

        // 3. Missing authorization (null) -> REJECTED
        val nullValidation = ElyzarethGovernanceEngine.verifyHumanGovernorAuthorization(null, specimen)
        assertTrue("Null authorization must be rejected", nullValidation is AuthorizationValidationResult.UnauthorizedMissingGovernor)

        // 4. Stale authorization (expired or flagged stale) -> REJECTED
        val staleAuth = humanAuth.copy(isStale = true)
        val staleValidation = ElyzarethGovernanceEngine.verifyHumanGovernorAuthorization(staleAuth, specimen)
        assertTrue("Stale authorization must be rejected", staleValidation is AuthorizationValidationResult.StaleExpired)

        val expiredAuth = humanAuth.copy(timestamp = System.currentTimeMillis() - (48 * 60 * 60 * 1000L)) // 48 hours old
        val expiredValidation = ElyzarethGovernanceEngine.verifyHumanGovernorAuthorization(expiredAuth, specimen, maxAgeMs = 24 * 60 * 60 * 1000L)
        assertTrue("Expired authorization must be rejected", expiredValidation is AuthorizationValidationResult.StaleExpired)
    }

    @Test
    fun test03_G5MasterReleaseProtection_GeneratesDeterministicManifestAndBindsToFinal() {
        ElyzarethGovernanceEngine.ImmutableWitnessVault.clearForTesting()

        val validPcmBytes = generateSynthetic16BitPcmSineWave(frequencyHz = 440.0, durationSeconds = 2.0, sampleRate = 44100)
        val measuredAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(validPcmBytes, sampleRateKhz = 44.1f, channels = 1)
        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = lyricTitle,
            rawLyric = lyricText,
            audioMetrics = measuredAudioMetrics,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT,
            preSuppliedEvidence = validLyricEvidence
        )

        // Create Human Governor Authorization
        val humanAuth = ElyzarethGovernanceEngine.createHumanGovernorAuthorization(
            specimen = specimen,
            dispositionChoice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            governorNotes = "Master release approved for production catalog",
            governorIdentity = "CHIEF_HUMAN_GOVERNOR"
        )

        // Commit Master Release
        val releaseResult = ElyzarethGovernanceEngine.commitMasterRelease(specimen, humanAuth)
        assertTrue("Master release commit must succeed with valid human authorization", releaseResult is MasterReleaseResult.Success)

        val success = releaseResult as MasterReleaseResult.Success
        val manifest = success.manifest

        assertEquals("ELYZARETH_FINAL/", manifest.releaseTargetDirectory)
        assertEquals(specimen.specimenId, manifest.specimenId)
        assertEquals(specimen.versionId, manifest.versionId)
        assertTrue(manifest.releaseId.startsWith("REL-ELY-FINAL-"))
        assertNotNull(manifest.combinedManifestHash)
        assertTrue(manifest.combinedManifestHash.startsWith("sha256:"))
        assertEquals(71, manifest.combinedManifestHash.length) // "sha256:" (7) + 64 hex chars
        assertEquals(humanAuth.authorizationId, manifest.authorizationId)
        assertTrue(manifest.isSealedAndFrozen)

        // Verify committed specimen is sealed into vault
        val committedSpecimen = success.committedSpecimen
        assertEquals(SpecimenDecision.ACCEPT, committedSpecimen.decision)
        assertFalse("Committed master cannot heal", committedSpecimen.canHeal)
        assertNotNull("Specimen must have bound release manifest", committedSpecimen.releaseManifest)
        assertEquals(manifest.combinedManifestHash, committedSpecimen.releaseManifest?.combinedManifestHash)

        // Verify presence in Immutable Witness Vault
        assertTrue(ElyzarethGovernanceEngine.ImmutableWitnessVault.isSpecimenFrozen(specimen.specimenId))
        val vaultCopy = ElyzarethGovernanceEngine.ImmutableWitnessVault.getFrozenSpecimen(specimen.specimenId)
        assertNotNull(vaultCopy)
        assertEquals(committedSpecimen.sha256Hash, vaultCopy!!.sha256Hash)
    }

    @Test
    fun test04_EvidenceMutation_PreventsReuseOfOldReleaseAuthorizationOrManifest() {
        ElyzarethGovernanceEngine.ImmutableWitnessVault.clearForTesting()

        val validPcmBytes = generateSynthetic16BitPcmSineWave(frequencyHz = 440.0, durationSeconds = 2.0, sampleRate = 44100)
        val measuredAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(validPcmBytes, sampleRateKhz = 44.1f, channels = 1)
        val originalSpecimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = lyricTitle,
            rawLyric = lyricText,
            audioMetrics = measuredAudioMetrics,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT,
            preSuppliedEvidence = validLyricEvidence
        )

        // Authorize original specimen
        val originalAuth = ElyzarethGovernanceEngine.createHumanGovernorAuthorization(
            specimen = originalSpecimen,
            dispositionChoice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            governorNotes = "Authorized for original evidence"
        )

        // Mutate the lyric text of the specimen (simulating post-authorization tampering)
        val mutatedLyric = lyricText.replace("Inside an old gray coat", "Inside a bright red jacket")
        assertNotEquals("Mutated lyric must differ from original", lyricText, mutatedLyric)
        val mutatedSpecimen = originalSpecimen.copy(
            lyricText = mutatedLyric,
            sha256Hash = ElyzarethGovernanceEngine.calculateBinarySha256(mutatedLyric.toByteArray())
        )

        // Attempting to verify or commit master release with the OLD authorization MUST FAIL
        val authValidation = ElyzarethGovernanceEngine.verifyHumanGovernorAuthorization(originalAuth, mutatedSpecimen)
        assertTrue("Mutated specimen must fail authorization verification due to hash mismatch",
            authValidation is AuthorizationValidationResult.MismatchEvidenceChanged)

        val commitResult = ElyzarethGovernanceEngine.commitMasterRelease(mutatedSpecimen, originalAuth)
        assertTrue("Commit master release must reject mutated evidence", commitResult is MasterReleaseResult.Rejected)
        assertEquals("CHANGED_EVIDENCE_HASH_MISMATCH", (commitResult as MasterReleaseResult.Rejected).violationCode)

        // Also test audio binary mutation tampering
        val mutatedAudioBytes = validPcmBytes.clone()
        mutatedAudioBytes[500] = 0x7F
        val mutatedAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(mutatedAudioBytes)
        val audioMutatedSpecimen = originalSpecimen.copy(audioWitness = mutatedAudioMetrics)

        val audioCommitResult = ElyzarethGovernanceEngine.commitMasterRelease(audioMutatedSpecimen, originalAuth)
        assertTrue("Commit master release must reject audio-mutated evidence", audioCommitResult is MasterReleaseResult.Rejected)
        assertEquals("CHANGED_EVIDENCE_HASH_MISMATCH", (audioCommitResult as MasterReleaseResult.Rejected).violationCode)
    }

    @Test
    fun test05_ImmutableWitnessVault_RejectsOverwriteAndBranchesPostFreezeMutation() {
        ElyzarethGovernanceEngine.ImmutableWitnessVault.clearForTesting()

        val validPcmBytes = generateSynthetic16BitPcmSineWave(frequencyHz = 440.0, durationSeconds = 2.0, sampleRate = 44100)
        val measuredAudioMetrics = ElyzarethGovernanceEngine.decodeAndValidatePcmAudio(validPcmBytes, sampleRateKhz = 44.1f, channels = 1)
        val parentSpecimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = lyricTitle,
            rawLyric = lyricText,
            audioMetrics = measuredAudioMetrics,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT,
            preSuppliedEvidence = validLyricEvidence
        )

        val auth = ElyzarethGovernanceEngine.createHumanGovernorAuthorization(
            specimen = parentSpecimen,
            dispositionChoice = GovernanceDispositionChoice.RELEASE_ACCEPT
        )
        val releaseResult = ElyzarethGovernanceEngine.commitMasterRelease(parentSpecimen, auth)
        assertTrue(releaseResult is MasterReleaseResult.Success)

        // 1. Attempt to overwrite the frozen specimen in the vault directly
        val rogueOverwriteSpecimen = parentSpecimen.copy(lyricText = "Tampered lyric text in vault")
        val freezeOverwriteResult = ElyzarethGovernanceEngine.ImmutableWitnessVault.freezeSpecimen(rogueOverwriteSpecimen)
        assertTrue("Vault must reject overwriting an already frozen specimen", freezeOverwriteResult is FreezeResult.Rejected)
        assertEquals("ALREADY_FROZEN_IMMUTABLE", (freezeOverwriteResult as FreezeResult.Rejected).errorCode)

        // 2. Post-freeze mutation branching
        val branchedLyric = lyricText.replace("Inside an old gray coat", "Wearing an old gray coat")
        val branchedSpecimen = ElyzarethGovernanceEngine.ImmutableWitnessVault.branchMutatedSpecimen(
            parentSpecimen = parentSpecimen,
            newLyricText = branchedLyric,
            newAudioMetrics = measuredAudioMetrics,
            mutationReason = "Bridge line cadence enhancement"
        )

        // Check new specimen identity
        assertNotEquals(parentSpecimen.specimenId, branchedSpecimen.specimenId)
        assertTrue(branchedSpecimen.specimenId.endsWith("-V02"))
        assertEquals("v02", branchedSpecimen.versionId)
        assertNull("Branched specimen must NOT inherit parent authorization", branchedSpecimen.governorAuthorization)
        assertNull("Branched specimen must NOT inherit parent release manifest", branchedSpecimen.releaseManifest)
        assertEquals(SpecimenDecision.NOT_YET_EXAMINED, branchedSpecimen.decision)

        // Check that original parent in vault is completely unchanged
        val vaultParent = ElyzarethGovernanceEngine.ImmutableWitnessVault.getFrozenSpecimen(parentSpecimen.specimenId)
        assertNotNull(vaultParent)
        assertEquals(parentSpecimen.lyricText, vaultParent!!.lyricText)
        assertEquals(SpecimenDecision.ACCEPT, vaultParent.decision)
    }

    @Test
    fun test06_DispositionSeparation_EnforcesAllFourDistinctPathways() {
        ElyzarethGovernanceEngine.ImmutableWitnessVault.clearForTesting()

        val specimen = ElyzarethGovernanceEngine.evaluateIngressedSpecimen(
            title = lyricTitle,
            rawLyric = lyricText,
            audioMetrics = null,
            sourceOrigin = IngressSourceOrigin.EXTERNAL_IMPORT,
            preSuppliedEvidence = validLyricEvidence
        )

        // Pathway 1: Freeze / Master -> RELEASE_ACCEPT -> G4 Authorized -> G5 Release to ELYZARETH_FINAL/
        val masterVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version = specimen,
            choice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            governorNotes = "Human release authorization to ELYZARETH_FINAL/"
        )
        assertEquals(SpecimenDecision.ACCEPT, masterVersion.decision)
        assertTrue(masterVersion.g5Disposition.isHumanGovernorAuthorized)
        assertNotNull("Master release manifest must be generated and attached", masterVersion.releaseManifest)
        assertEquals("ELYZARETH_FINAL/", masterVersion.releaseManifest?.releaseTargetDirectory)

        // Pathway 2: Quarantine -> ELDS-M Mutation / Experimental Sandbox
        val quarantineVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version = specimen,
            choice = GovernanceDispositionChoice.QUARANTINE_ELDS_M_MUTATION,
            governorNotes = "Quarantine for non-destructive experimental evaluation in ELDS-M"
        )
        assertEquals(SpecimenDecision.NEEDS_HEALING, quarantineVersion.decision)
        assertEquals(GovernanceDispositionChoice.QUARANTINE_ELDS_M_MUTATION, quarantineVersion.g5Disposition.chosenDisposition)
        assertTrue(quarantineVersion.g5Disposition.chosenDisposition.routingTarget.contains("ELDS-M"))
        assertNull("Quarantine path must not generate a master release manifest", quarantineVersion.releaseManifest)

        // Pathway 3: Purify -> Re-curation / G1 Re-alignment
        val purifyVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version = specimen,
            choice = GovernanceDispositionChoice.PURIFY_RECURATE,
            governorNotes = "Purify & re-align G1 text witness"
        )
        assertEquals(SpecimenDecision.NEEDS_HEALING, purifyVersion.decision)
        assertEquals(GovernanceDispositionChoice.PURIFY_RECURATE, purifyVersion.g5Disposition.chosenDisposition)
        assertTrue(purifyVersion.g5Disposition.chosenDisposition.routingTarget.contains("G1 Re-examination"))
        assertNull("Purify path must not generate a master release manifest", purifyVersion.releaseManifest)

        // Pathway 4: Reject -> Permanently Excluded
        val rejectVersion = ElyzarethGovernanceEngine.applyHumanGovernorDisposition(
            version = specimen,
            choice = GovernanceDispositionChoice.PERMANENT_REJECT,
            governorNotes = "Violates core aesthetic canons"
        )
        assertEquals(SpecimenDecision.NOT_ELIGIBLE, rejectVersion.decision)
        assertEquals(GovernanceDispositionChoice.PERMANENT_REJECT, rejectVersion.g5Disposition.chosenDisposition)
        assertFalse(rejectVersion.canHeal)
        assertNull(rejectVersion.releaseManifest)
    }

    @Test
    fun test07_ArchitecturalBoundary_PreservesDistinctEldsSubsystems() {
        // Enforce the critical architectural boundary:
        // ELDS-C = CURATION
        // ELDS-M = MUTATION
        // ELYZARETH OS = GOVERNANCE / AUTHORITY
        assertEquals("ELDS-C", EldsSubsystemDomain.ELDS_C.code)
        assertEquals("Curation & Witness Domain", EldsSubsystemDomain.ELDS_C.label)
        assertTrue(EldsSubsystemDomain.ELDS_C.purpose.contains("App 02"))

        assertEquals("ELDS-M", EldsSubsystemDomain.ELDS_M.code)
        assertEquals("Mutation & Experimental Domain", EldsSubsystemDomain.ELDS_M.label)
        assertTrue(EldsSubsystemDomain.ELDS_M.purpose.contains("Non-App 02"))

        // Quarantine route strictly targets ELDS-M
        val quarantineChoice = GovernanceDispositionChoice.QUARANTINE_ELDS_M_MUTATION
        assertTrue(quarantineChoice.routingTarget.contains("ELDS-M Experimental Sandbox"))
        assertTrue(quarantineChoice.routingTarget.contains("zero overwrite"))
    }

    @Test
    fun test08_App02ArtifactPackageReconciliation_ZeroInferenceAndMultiArtifactIngress() {
        val rawPcm = generateSynthetic16BitPcmSineWave(440.0, 3.0)
        val pcmHash = calculateSha256(rawPcm)
        val witnessJson = ElyzarethGovernanceEngine.serializeLyricEvidenceToJson(validLyricEvidence)
        val witnessHash = calculateSha256(witnessJson.toByteArray(Charsets.UTF_8))
        val textBytes = lyricText.toByteArray(Charsets.UTF_8)
        val textHash = calculateSha256(textBytes)

        // 1. Ingest physical package from Google Drive with intentionally deceptive/fake filename metadata
        val gDrivePackage = SpecimenArtifactPackage(
            packageId = "PKG-GDRIVE-9901",
            title = lyricTitle,
            sourceOrigin = IngressSourceOrigin.GOOGLE_DRIVE,
            declaredLocationOrPath = "gdrive://vault/draft_v1_ignore_me.txt",
            lyricTextBytes = textBytes,
            jsonWitnessBytes = witnessJson.toByteArray(Charsets.UTF_8),
            audioBinaryBytes = rawPcm,
            audioFormatDeclared = "audio/mp3_fake_header" // metadata claim that must be ignored
        )

        val reconciled = ElyzarethGovernanceEngine.reconcilePhysicalArtifactPackage(gDrivePackage)
        assertTrue(reconciled.isAudioProvided)
        assertEquals(textHash, reconciled.textBinaryHash)
        assertEquals(witnessHash, reconciled.witnessJsonHash)
        assertEquals(pcmHash, reconciled.audioBinaryHash)
        assertEquals(IngressSourceOrigin.GOOGLE_DRIVE, reconciled.sourceOrigin)
        assertNotNull(reconciled.resolvedAudioMetrics)
        assertEquals("PASS", reconciled.resolvedAudioMetrics?.decoderStatus)
        assertEquals("VERIFIED", reconciled.resolvedAudioMetrics?.pcmStatus)

        // 2. Evaluate reconciled package through ELDS-C pipeline
        val evaluatedSpecimen = ElyzarethGovernanceEngine.evaluateReconciledPackage(reconciled)
        assertNotNull(evaluatedSpecimen.g1Witness)
        assertTrue(evaluatedSpecimen.g1Witness!!.isValid)
        assertEquals(G1PathType.DUAL_VERIFIED, evaluatedSpecimen.g1Witness!!.pathType)

        // 3. Test Local Drive package without audio (text + JSON witness only)
        val localPackage = SpecimenArtifactPackage(
            packageId = "PKG-LOCAL-8802",
            title = "Acoustic Horizon Draft",
            sourceOrigin = IngressSourceOrigin.LOCAL_FOLDER,
            declaredLocationOrPath = "/sdcard/Music/elyzareth_local/track1",
            lyricTextBytes = textBytes,
            jsonWitnessBytes = witnessJson.toByteArray(Charsets.UTF_8),
            audioBinaryBytes = null
        )
        val localReconciled = ElyzarethGovernanceEngine.reconcilePhysicalArtifactPackage(localPackage)
        assertFalse(localReconciled.isAudioProvided)
        assertNull(localReconciled.resolvedAudioMetrics)
        assertNull(localReconciled.audioBinaryHash)
        assertEquals(IngressSourceOrigin.LOCAL_FOLDER, localReconciled.sourceOrigin)
    }

    @Test
    fun test09_CureLoopBoundary_App02DiagnosesToOsStructuredCureRequestToApp01CreationToApp02ReCuration() {
        // Step 1: Ingress a drift/deficient specimen into App 02 / ELDS-C
        val driftLyric = """
            [Verse 1]
            Looking past the ceiling at the distant hill,
            Waiting for the silence to become still.
            Wondering about the moments we used to fill,
            Standing by the valley against the chill.
        """.trimIndent()

        val driftEvidence = LyricEvidence(
            theme = "Melancholy Reflection",
            narrativeArc = "Solitary reflection on lost time",
            emotionalProfile = "Wistful, restrained",
            witnessObjects = listOf("distant hill"),
            temporalContext = "Winter dusk",
            energyProfile = "Low acoustic presence",
            languageCharacteristics = "Abstract reflection with minimal tactile anchors",
            creativeSignals = listOf("ORGANIC_ACOUSTIC_RECONSTRUCTION")
        )
        val driftWitnessJson = ElyzarethGovernanceEngine.serializeLyricEvidenceToJson(driftEvidence)

        val driftPackage = SpecimenArtifactPackage(
            packageId = "PKG-DRIFT-01",
            title = "Acoustic Horizon Drift",
            sourceOrigin = IngressSourceOrigin.IMPORTED_CORPUS,
            lyricTextBytes = driftLyric.toByteArray(Charsets.UTF_8),
            jsonWitnessBytes = driftWitnessJson.toByteArray(Charsets.UTF_8)
        )
        val reconciledDrift = ElyzarethGovernanceEngine.reconcilePhysicalArtifactPackage(driftPackage)
        val diagnosedSpecimen = ElyzarethGovernanceEngine.evaluateReconciledPackage(reconciledDrift)

        // ELDS-C diagnoses deficient G2 band and missing physical anchors
        assertEquals(SpecimenDecision.NEEDS_HEALING, diagnosedSpecimen.decision)
        assertTrue(diagnosedSpecimen.canHeal)
        val g2 = diagnosedSpecimen.g2Diagnostic
        assertNotNull(g2)
        assertEquals(G2DiagnosticBand.DRIFT, g2!!.band)
        assertTrue(g2.physicalAnchorCount < 3)

        // Step 2: Elyzareth OS routes a StructuredCureRequest to App 01
        val cureRequest = ElyzarethGovernanceEngine.createStructuredCureRequest(
            specimen = diagnosedSpecimen,
            baseTitle = "Acoustic Horizon Drift",
            governorNotes = "Anchor in tactile winter imagery and increase tactile objects"
        )
        assertEquals("APP_02_CORPUS_CURATOR", cureRequest.sourceApp)
        assertEquals("APP_01_CREATION_ENGINE", cureRequest.destinationApp)
        assertEquals("ELYZARETH_OS", cureRequest.routedBy)
        assertTrue(cureRequest.gateFlags.any { it.contains("PHYSICAL_ANCHOR_DEFICIT") })

        // Step 3: App 01 creates the revised lyric/specimen artifact (creation/correction domain, NO file ingestion)
        val curedLyric = """
            [Verse 1]
            Inside an old gray coat I found a silver coin,
            Beside the misty railway where the rivers join.
            The iron lantern flickered on the wooden door,
            Remembering what was spoken on the muddy floor.
        """.trimIndent()

        val app01RevisedPackage = ElyzarethGovernanceEngine.processApp01CureDraft(
            cureRequest = cureRequest,
            revisedLyricText = curedLyric
        )
        assertNotNull(app01RevisedPackage.lyricTextBytes)
        assertNotNull(app01RevisedPackage.jsonWitnessBytes)

        // Step 4: Revised artifact package is subsequently ingested by App 02 for independent re-curation
        val reconciledCure = ElyzarethGovernanceEngine.reconcilePhysicalArtifactPackage(app01RevisedPackage)
        val reCuratedSpecimen = ElyzarethGovernanceEngine.evaluateReconciledPackage(reconciledCure)

        // Step 5: Verify that re-curation independently recognizes the cured state
        assertNotNull(reCuratedSpecimen.g2Diagnostic)
        assertEquals(0, reCuratedSpecimen.g2Diagnostic!!.prohibitedLexiconCount)
        assertTrue(reCuratedSpecimen.g2Diagnostic!!.physicalAnchorCount >= 3)
        assertEquals(G2DiagnosticBand.COMPLIANT, reCuratedSpecimen.g2Diagnostic!!.band)
    }
}


