package com.example

import com.example.engine.c16.*
import com.example.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ELYZARETH OS v38.1 — C16 ENGINE CANONICAL 12-COMPONENT ARCHITECTURE TEST
 *
 * Verifies the complete 12-component Engine specification:
 * 1. C16 Turbo / Cognitive Orchestration
 * 2. Creative Fingerprint + Collision Detection (Binary vs. Creative Identity separation)
 * 3. Physical Anchor / Witness System (5 Witness Pillars, Non-decorational, Silent Witness)
 * 4. Language / Cliché / Contamination (Corpus 205769ac identity binding, unverified algorithms as NOT FOUND/SPEC)
 * 5. Theme–Emotion–Style Coherence (Theme -> Witness -> Emotion -> Style)
 * 6. ONM / Provenance Registry (PR-001 -> PR-004, One Variable / One Observation)
 * 7. Information Survival Hierarchy (Anchors > State Markers > Numeric Series > Multi-Component Arrays)
 * 8. Arrangement Leakage Guard (Locked v1.0 constraints, Declared != Measured)
 * 9. 5-Room Acoustic Model (Environmental physics taxonomy, NOT MEASURED preserved)
 * 10. G1 / G2 / G3 Governance Rules
 * 11. Dual-Witness / Forensic Audio (NOT_MEASURED != FAILED)
 * 12. 3.2.1.0 + Human Governor (No automated AI authorization)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class C16EngineArchitectureTest {

    private lateinit var orchestrator: C16CognitiveOrchestrator
    private lateinit var generatorAdapter: StandardLyricGeneratorAdapter

    @Before
    fun setUp() {
        orchestrator = C16CognitiveOrchestrator()
        generatorAdapter = StandardLyricGeneratorAdapter()
    }

    @Test
    fun test01_c16OrchestratorComposesMasterCreativeSpecification() {
        val intent = UserCreativeIntent(
            title = "Axiom of the Cedar River",
            storyConcept = "Artifacts kept upon the wooden table beside the railway",
            genre = "Acoustic Dark Folk",
            stylePrompt = "warm acoustic fingerpicking, dry parlor vocal",
            vocalTimbre = "Restrained Baritone",
            desiredAnchors = listOf("wooden table", "silver coin", "iron key"),
            targetBpm = 64,
            timeSignature = "4/4",
            targetRoom = AcousticRoom.ROOM_05_RUSTIC
        )

        val spec = orchestrator.composeCreativeSpecification(intent)

        assertNotNull("Master specification must not be null", spec)
        assertTrue("Spec ID must be formatted", spec.specId.startsWith("SPEC-"))
        assertEquals("Creative DNA title must match intent", "Axiom of the Cedar River", spec.creativeDna.title)
        assertTrue("Witness contract must have mandatory objects", spec.witnessContract.mandatoryWitnessObjects.size >= 2)
        assertEquals("Room profile must match target", AcousticRoom.ROOM_05_RUSTIC, spec.acousticRoom.room)
        assertNotNull("Provenance record must be linked", spec.provenance)
        assertNotNull("Creative fingerprint must be generated", spec.creativeFingerprint)
    }

    @Test
    fun test02_component02_binarySha256VsCreativeFingerprintSeparation() {
        val fingerprintEngine = CreativeFingerprintEngine()
        val intent = UserCreativeIntent(
            title = "Limestone Bridge",
            storyConcept = "The river under the stone arch",
            desiredAnchors = listOf("limestone", "bridge")
        )
        val spec = orchestrator.composeCreativeSpecification(intent)
        val fingerprint = fingerprintEngine.extractFingerprint(spec)

        assertNotEquals("Binary SHA-256 must differ from Creative Token", fingerprint.binarySha256, fingerprint.creativeIdentityToken)
        assertTrue("Creative identity token must start with CFE-", fingerprint.creativeIdentityToken.startsWith("CFE-"))
        assertEquals("Vector embedding status must be SPEC_UNIMPLEMENTED", CapabilityStatus.SPEC_UNIMPLEMENTED, fingerprint.vectorEmbeddingStatus)
        assertEquals("Cosine similarity status must be SPEC_UNIMPLEMENTED", CapabilityStatus.SPEC_UNIMPLEMENTED, fingerprint.cosineSimilarityStatus)

        // Collision check against canonical baseline
        val collision = fingerprintEngine.detectCollision(
            "Across the wooden table sits the silver coin beside the misty railway where the rivers join",
            fingerprint
        )
        assertTrue("Collision must be detected for registered baseline", collision.collisionDetected)
        assertEquals("SILVER_COIN_CANONICAL", collision.matchingBaselineId)
    }

    @Test
    fun test03_component03_fiveWitnessPillarsAndNonDecorationalRequirement() {
        val witnessSystem = PhysicalWitnessSystem()
        val intent = UserCreativeIntent(
            title = "Sovereign Hearth",
            storyConcept = "The coat on the chair and the medicine bottle near the timber door",
            desiredAnchors = listOf("coat", "timber", "key")
        )
        val contract = witnessSystem.establishWitnessContract(intent)

        assertTrue("Must include DOMESTIC pillar", contract.requiredPillars.contains(WitnessPillar.DOMESTIC))
        assertTrue("Must include ENVIRONMENTAL pillar", contract.requiredPillars.contains(WitnessPillar.ENVIRONMENTAL))
        assertTrue("Silent witness principle must be true", contract.silentWitnessPrinciple)
        assertTrue("Non-decorational mandate must be true", contract.nonDecorationalMandate)

        // Evaluate passing lyric
        val passEvaluation = witnessSystem.evaluateWitnesses(
            "The coat hangs by the timber door, the key turns in the lock once more.",
            contract
        )
        assertEquals("PASS", passEvaluation.status)
        assertTrue("Pass must detect witness objects", passEvaluation.detectedWitnessObjects.isNotEmpty())

        // Evaluate failing lyric lacking physical objects
        val failEvaluation = witnessSystem.evaluateWitnesses(
            "My boundless sorrow drifts across the endless ocean of tears.",
            contract
        )
        assertEquals("FAIL", failEvaluation.status)
        assertNotNull("Fail reason must explain lack of physical objects", failEvaluation.failReason)
    }

    @Test
    fun test04_component04_languageEnvelopeAndCorpusBindingSemantics() {
        val languageGovernor = LyricLanguageGovernor()
        val intent = UserCreativeIntent(title = "Test", storyConcept = "Test")
        val envelope = languageGovernor.generateLanguageEnvelope(intent)

        // Invariant: Corpus 205769ac is identity reference, NOT closed vocabulary filter
        val corpusProfile = languageGovernor.getCorpusReferenceProfile()
        assertEquals("205769ac", corpusProfile.corpusUuid)
        assertFalse("Corpus binding must NEVER be a closed-vocabulary filter", corpusProfile.isClosedVocabularyFilter)

        // Invariant: Unverified scoring algorithms explicitly marked NOT_FOUND_UNVERIFIED
        assertEquals(CapabilityStatus.NOT_FOUND_UNVERIFIED, envelope.deltaVScoreStatus)
        assertEquals(CapabilityStatus.NOT_FOUND_UNVERIFIED, envelope.rhymeAttractorScoreStatus)
        assertEquals(CapabilityStatus.NOT_FOUND_UNVERIFIED, envelope.lexicalTensionScoreStatus)
        assertEquals(CapabilityStatus.SPEC_UNIMPLEMENTED, envelope.tfIdfDriftEngineStatus)

        // Audit clean lyric vs trope-polluted lyric
        val cleanAudit = languageGovernor.auditLanguage("The wooden table stands in the quiet room.", envelope)
        assertTrue("Clean lyric must be compliant", cleanAudit.isCompliant)

        val tropeAudit = languageGovernor.auditLanguage("A neon tapestry in a symphony of stars.", envelope)
        assertFalse("Trope lyric must NOT be compliant", tropeAudit.isCompliant)
        assertEquals(2, tropeAudit.detectedTropes.size)
    }

    @Test
    fun test05_component05_themeEmotionStyleRelationship() {
        val tesEngine = ThemeEmotionStyleEngine()
        val intent = UserCreativeIntent(
            title = "Winter Crossing",
            storyConcept = "Nostalgic memories of the railway crossing",
            genre = "Acoustic Dark Folk",
            targetBpm = 64,
            targetRoom = AcousticRoom.ROOM_05_RUSTIC
        )

        val envelope = tesEngine.composeThemeEmotionStyleEnvelope(intent)
        assertEquals(64, envelope.tempoBpm)
        assertTrue(envelope.emotionalProfile.contains("Nostalgia"))
        assertTrue(envelope.acousticAtmosphere.contains("dry timber parlor"))

        val audit = tesEngine.evaluateCoherence(
            theme = envelope.theme,
            emotion = envelope.emotionalProfile,
            style = "Acoustic Dark Folk, warm acoustic fingerpicking",
            draftText = "The railway lies cold under the winter sky."
        )
        assertTrue("Harmonized theme/style must pass qualitative binary coherence audit", audit.isHarmonized)
        assertFalse("Numerical coherence threshold must NOT be enforced in canonical evaluation", audit.isNumericalThresholdEnforced)
        assertNotNull("Diagnostic explanation must be provided", audit.diagnosticNotes)
    }

    @Test
    fun test06_component06_onmProvenanceTrackingAndBoundaries() {
        val provenanceRegistry = OnmProvenanceRegistry()
        val record1 = provenanceRegistry.createLineageRecord("SPEC-001", null, "Initial concept")

        assertEquals(ProvenanceBoundary.PR_001_CORPUS_LINEAGE, record1.boundary)
        assertNull(record1.parentProvenanceHash)
        assertTrue(record1.currentProvenanceHash.isNotBlank())

        val record2 = provenanceRegistry.createLineageRecord("SPEC-002", record1.currentProvenanceHash, "Single-variable cure")
        assertEquals(ProvenanceBoundary.PR_002_MUTATION_BOUNDARY, record2.boundary)
        assertEquals(record1.currentProvenanceHash, record2.parentProvenanceHash)

        assertTrue(provenanceRegistry.validateMutationBoundary(record1.currentProvenanceHash, record2.currentProvenanceHash, ProvenanceBoundary.PR_002_MUTATION_BOUNDARY))
    }

    @Test
    fun test07_component07_informationSurvivalHierarchy() {
        val survivalEvaluator = InformationSurvivalEvaluator()
        val intent = UserCreativeIntent(
            title = "Test Survival",
            storyConcept = "Wooden table and silver coin",
            desiredAnchors = listOf("table", "coin")
        )
        val spec = orchestrator.composeCreativeSpecification(intent)

        // Draft containing required anchors
        val draftWithAnchors = GeneratedDraft(
            draftId = "DRAFT-1",
            specId = spec.specId,
            rawLyricText = "On the table sits the coin.",
            stanzas = listOf(
                Stanza("s1", "Verse 1", listOf("On the table sits the coin."), listOf(7))
            ),
            stylePrompt = "folk",
            tempoBpm = 64,
            timeSignature = "4/4",
            vocalTimbre = "Baritone"
        )

        val reportPass = survivalEvaluator.auditInformationSurvival(spec, draftWithAnchors)
        assertTrue("Anchors must survive", reportPass.anchorsSurvived)
        assertTrue("State markers must survive", reportPass.stateMarkersSurvived)
        assertEquals(SurvivalAuditState.VERIFIED, reportPass.overallStatus)

        // Draft losing anchors
        val draftWithoutAnchors = GeneratedDraft(
            draftId = "DRAFT-2",
            specId = spec.specId,
            rawLyricText = "No physical objects anywhere in sight.",
            stanzas = listOf(
                Stanza("s2", "Verse 1", listOf("No physical objects anywhere in sight."), listOf(10))
            ),
            stylePrompt = "folk",
            tempoBpm = 64,
            timeSignature = "4/4",
            vocalTimbre = "Baritone"
        )

        val reportFail = survivalEvaluator.auditInformationSurvival(spec, draftWithoutAnchors)
        assertFalse("Anchors lost must fail survival", reportFail.anchorsSurvived)
        assertEquals(SurvivalAuditState.FAILED, reportFail.overallStatus)
    }

    @Test
    fun test08_component08_arrangementLeakageGuardPreservesLockedV1() {
        val leakageGuard = ArrangementLeakageGuard()
        val constraints = leakageGuard.getArrangementConstraints()

        assertEquals("ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1.0", constraints.constraintId)
        assertFalse("Declared constraint is NOT measured audio evidence", constraints.isMeasuredAudioEvidence)

        // Test clean acoustic style vs leaked percussive style
        val cleanAudit = leakageGuard.validateLeakageDeclaration("steel-string acoustic fingerpicking, dry parlor vocal")
        assertTrue("Clean style must be zero-leakage compliant", cleanAudit.zeroLeakageCompliant)

        val leakedAudit = leakageGuard.validateLeakageDeclaration("acoustic guitar with synth pads and kicks")
        assertFalse("Leaked style must be flagged", leakedAudit.zeroLeakageCompliant)
        assertTrue("Must flag synth pads and kicks", leakedAudit.flaggedTerms.contains("synth pads") || leakedAudit.flaggedTerms.contains("kicks"))
    }

    @Test
    fun test09_component09_fiveRoomAcousticTaxonomyAndNotMeasuredPreservation() {
        val acousticModel = FiveRoomAcousticModel()

        // ROOM_05_RUSTIC (Measured baseline)
        val rusticSpec = acousticModel.getRoomSpecification(AcousticRoom.ROOM_05_RUSTIC)
        assertTrue(rusticSpec.isT60Measured)
        assertEquals(0.40f, rusticSpec.defaultMaxT60Seconds)

        // ROOM_01_SHARED (Explicitly NOT MEASURED)
        val sharedSpec = acousticModel.getRoomSpecification(AcousticRoom.ROOM_01_SHARED)
        assertFalse("ROOM_01 T60 must not be reported as measured", sharedSpec.isT60Measured)
        assertNull("ROOM_01 T60 must remain null (NOT MEASURED)", sharedSpec.defaultMaxT60Seconds)

        // ROOM_04_MEMORY (Explicitly NOT MEASURED)
        val memorySpec = acousticModel.getRoomSpecification(AcousticRoom.ROOM_04_MEMORY)
        assertFalse("ROOM_04 T60 must not be reported as measured", memorySpec.isT60Measured)
        assertNull("ROOM_04 T60 must remain null (NOT MEASURED)", memorySpec.defaultMaxT60Seconds)
    }

    @Test
    fun test10_component10_g1G2G3GovernanceRules() {
        val governanceRules = GovernanceRulesEngine()
        val intent = UserCreativeIntent(title = "Axiom", storyConcept = "Test")
        val spec = orchestrator.composeCreativeSpecification(intent)

        val draft = GeneratedDraft(
            draftId = "DRAFT-G",
            specId = spec.specId,
            rawLyricText = "Across the wooden table sits the silver coin.\nBeside the misty railway where the rivers join.",
            stanzas = emptyList(),
            stylePrompt = "dark folk",
            tempoBpm = 64,
            timeSignature = "4/4",
            vocalTimbre = "Baritone"
        )

        val g1 = governanceRules.evaluateG1Witness(draft, spec)
        assertTrue("G1 text witness must be valid", g1.isValid)

        val g2 = governanceRules.evaluateG2Diagnostic(draft, spec)
        assertTrue("G2 band must be compliant or tolerant", g2.band == G2DiagnosticBand.COMPLIANT || g2.band == G2DiagnosticBand.TOLERANT)

        val g3 = governanceRules.evaluateG3Performance(draft)
        assertFalse("G3 performance must report unmeasured audio without failure", g3.isAudioMeasured)
    }

    @Test
    fun test11_component11_dualWitnessDistinctionAndNotMeasuredNotEqualFailed() {
        val dualWitnessBridge = DualWitnessForensicBridge()
        val intent = UserCreativeIntent(title = "Dual Witness Test", storyConcept = "Table coin")
        val spec = orchestrator.composeCreativeSpecification(intent)

        val draft = GeneratedDraft(
            draftId = "DRAFT-DW",
            specId = spec.specId,
            rawLyricText = "On the table lies the coin.",
            stanzas = emptyList(),
            stylePrompt = "folk",
            tempoBpm = 64,
            timeSignature = "4/4",
            vocalTimbre = "Baritone"
        )

        val declared = dualWitnessBridge.buildDeclaredWitness(spec, draft)
        assertEquals(spec.specId, declared.specId)
        assertTrue(declared.textHash.isNotBlank())

        // Invariant: Unmeasured audio must NOT equal FAILED
        val unmeasuredAudio = dualWitnessBridge.inspectMeasuredAudioWitness(null)
        assertFalse("Audio must not be measured", unmeasuredAudio.isMeasured)
        assertNull("Unmeasured audio must NOT produce a failure reason", unmeasuredAudio.failureReason)

        val comparison = dualWitnessBridge.compareWitnessRecords(declared, unmeasuredAudio)
        assertTrue("Comparison must be satisfied with deferred audio", comparison.isDualWitnessSatisfied)
        assertTrue(comparison.forensicStatusMessage.contains("NOT MEASURED"))
    }

    @Test
    fun test12_component12_humanGovernorProtocolEnforcesExplicitAuthorization() {
        val governorProtocol = HumanGovernorProtocol()

        // Step 0 Freeze blocked without human authorization
        val unauthFreeze = governorProtocol.evaluateProtocolStep(GovernorProtocolStep.STEP_0_FREEZE, null)
        assertFalse(unauthFreeze.isHumanGovernorAuthorized)
        assertTrue(unauthFreeze.statusMessage.contains("BLOCKED"))

        // Automated AI authorization rejected
        val aiAuth = HumanGovernorAuthorization(
            authorizationId = "AUTH-AI",
            governorIdentity = "AUTOMATED_AI_AGENT",
            specimenId = "SPEC-001",
            expectedEvidenceHash = "HASH",
            dispositionChoice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            isExplicitlyHumanAuthorized = false,
            isAutomatedAI = true
        )
        val aiFreeze = governorProtocol.evaluateProtocolStep(GovernorProtocolStep.STEP_0_FREEZE, aiAuth)
        assertFalse(aiFreeze.isHumanGovernorAuthorized)
        assertTrue(aiFreeze.statusMessage.contains("BLOCKED"))

        // Legitimate Human Governor authorization accepted
        val humanAuth = HumanGovernorAuthorization(
            authorizationId = "AUTH-HUMAN",
            governorIdentity = "PRODUCER_HUMAN_GOVERNOR",
            specimenId = "SPEC-001",
            expectedEvidenceHash = "HASH",
            dispositionChoice = GovernanceDispositionChoice.RELEASE_ACCEPT,
            isExplicitlyHumanAuthorized = true,
            isAutomatedAI = false
        )
        val humanFreeze = governorProtocol.evaluateProtocolStep(GovernorProtocolStep.STEP_0_FREEZE, humanAuth)
        assertTrue(humanFreeze.isHumanGovernorAuthorized)
        assertEquals(GovernanceDispositionChoice.RELEASE_ACCEPT, humanFreeze.currentDisposition)
        assertTrue(humanFreeze.statusMessage.contains("PRODUCER_HUMAN_GOVERNOR"))
    }

    @Test
    fun test13_endToEndPipeline_orchestratorToGeneratorAdapterToAudit() {
        // 1. User Creative Intent
        val intent = UserCreativeIntent(
            title = "Axiom of the Cedar River",
            storyConcept = "Recollections of the railway and wooden table",
            genre = "Acoustic Dark Folk",
            stylePrompt = "warm steel-string fingerpicking, dry parlor vocal",
            vocalTimbre = "Restrained Baritone",
            desiredAnchors = listOf("wooden table", "silver coin", "iron key"),
            targetBpm = 64,
            targetRoom = AcousticRoom.ROOM_05_RUSTIC
        )

        // 2. C16 Composes Creative Specification
        val spec = orchestrator.composeCreativeSpecification(intent)
        assertNotNull(spec)

        // 3. Generator Request Envelope Created
        val generatorRequest = orchestrator.createGeneratorRequest(spec)
        assertEquals(spec.creativeDna.title, generatorRequest.title)
        assertTrue(generatorRequest.requiredPhysicalAnchors.contains("wooden table"))

        // 4. Downstream Generator Adapter Proposes Expression (Non-Trusted Realization)
        val generatedDraft = generatorAdapter.generateDraft(generatorRequest)
        assertNotNull(generatedDraft)
        assertTrue(generatedDraft.rawLyricText.contains("wooden table"))
        assertTrue(generatedDraft.rawLyricText.contains("silver coin"))

        // 5. C16 Performs Post-Generation Audit across all 12 Components
        val auditReport = orchestrator.auditGeneratedDraft(generatedDraft, spec)
        assertNotNull(auditReport)
        assertTrue("Audit must pass for governed draft", auditReport.isGovernedPass)
        assertEquals("PASS", auditReport.witnessEvaluation.status)
        assertTrue("Language must be compliant", auditReport.languageAudit.isCompliant)
        assertTrue("Survival must be verified", auditReport.informationSurvivalReport.anchorsSurvived)
        assertTrue("G1 text witness must be valid", auditReport.g1Witness.isValid)
        assertTrue("Dual-witness text must be satisfied", auditReport.dualWitnessComparison.isDualWitnessSatisfied)
        assertTrue("Seal hash must be non-empty", auditReport.engineSealHash.isNotBlank())
    }
}
