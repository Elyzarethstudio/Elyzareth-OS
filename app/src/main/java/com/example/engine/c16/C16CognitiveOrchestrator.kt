package com.example.engine.c16

import com.example.engine.ElyzarethTurboEngine
import com.example.model.*
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

/**
 * Component 01: C16 Turbo / Cognitive Orchestration Engine
 *
 * Core Orchestrator responsible for:
 * 1. Composing the complete CreativeSpecification from user intent across all 12 components.
 * 2. Creating downstream GeneratorRequests for realization adapters.
 * 3. Auditing generated or cured drafts through the 12-component governance pipeline.
 *
 * Core Architectural Principle:
 * USER CREATIVE INTENT -> C16 -> COMPONENT CONSTRAINTS -> CREATIVE SPECIFICATION ->
 * LYRIC / MUSIC GENERATOR -> GENERATED OUTPUT -> C16 / COMPONENT AUDIT ->
 * G1 -> G2 -> G3 -> G4 -> G5 -> DUAL-WITNESS EVIDENCE -> HUMAN GOVERNOR -> FREEZE
 */
class C16CognitiveOrchestrator(
    private val fingerprintEngine: ICreativeFingerprintEngine = CreativeFingerprintEngine(),
    private val witnessSystem: IPhysicalWitnessSystem = PhysicalWitnessSystem(),
    private val languageGovernor: ILyricLanguageGovernor = LyricLanguageGovernor(),
    private val themeEmotionStyleEngine: IThemeEmotionStyleEngine = ThemeEmotionStyleEngine(),
    private val provenanceRegistry: IOnmProvenanceRegistry = OnmProvenanceRegistry(),
    private val infoSurvivalEvaluator: IInformationSurvivalEvaluator = InformationSurvivalEvaluator(),
    private val leakageGuard: IArrangementLeakageGuard = ArrangementLeakageGuard(),
    private val acousticModel: IFiveRoomAcousticModel = FiveRoomAcousticModel(),
    private val governanceRulesEngine: IGovernanceRulesEngine = GovernanceRulesEngine(),
    private val dualWitnessBridge: IDualWitnessForensicBridge = DualWitnessForensicBridge(),
    private val humanGovernorProtocol: IHumanGovernorProtocol = HumanGovernorProtocol()
) : IC16CognitiveOrchestrator {

    companion object {
        fun sha256(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02x".format(it) }
        }
    }

    override fun composeCreativeSpecification(intent: UserCreativeIntent): CreativeSpecification {
        val specId = "SPEC-${UUID.randomUUID().toString().take(8).uppercase(Locale.US)}"

        // 1. Establish Physical Witness Contract (Component 03)
        val witnessContract = witnessSystem.establishWitnessContract(intent)

        // 2. Build Creative DNA (Component 01/02)
        val dna = ElyzarethTurboEngine.buildCreativeDna(
            title = intent.title,
            storyConcept = intent.storyConcept,
            existingLyric = intent.existingLyricForCure ?: "",
            genre = intent.genre,
            evidence = null
        ).copy(
            physicalWitnessAnchors = witnessContract.mandatoryWitnessObjects.map { it.objectName }
        )

        // 3. Generate Language Constraint Envelope (Component 04)
        val langEnvelope = languageGovernor.generateLanguageEnvelope(intent)

        // 4. Compose Theme-Emotion-Style Envelope (Component 05)
        val tesEnvelope = themeEmotionStyleEngine.composeThemeEmotionStyleEnvelope(intent)

        // 5. Create Lineage & Provenance Record (Component 06)
        val provenance = provenanceRegistry.createLineageRecord(
            specId = specId,
            parentHash = null,
            authorIntent = intent.storyConcept
        )

        // 6. Information Survival Specification (Component 07)
        val infoSurvivalSpec = InformationSurvivalSpecification()

        // 7. Arrangement Constraints (Component 08)
        val arrangementConstraints = leakageGuard.getArrangementConstraints()

        // 8. Room Acoustic Specification (Component 09)
        val roomSpec = acousticModel.getRoomSpecification(intent.targetRoom)

        // Temporary spec for fingerprint extraction
        val dummyFingerprint = CreativeFingerprint(
            binarySha256 = "",
            creativeIdentityToken = "",
            anchorSignature = "",
            emotionalSignature = "",
            acousticSignature = ""
        )

        val preliminarySpec = CreativeSpecification(
            specId = specId,
            creativeDna = dna,
            witnessContract = witnessContract,
            languageEnvelope = langEnvelope,
            themeEmotionStyle = tesEnvelope,
            provenance = provenance,
            informationSurvival = infoSurvivalSpec,
            arrangementConstraints = arrangementConstraints,
            acousticRoom = roomSpec,
            creativeFingerprint = dummyFingerprint
        )

        // 9. Extract Creative Fingerprint (Component 02)
        val resolvedFingerprint = fingerprintEngine.extractFingerprint(preliminarySpec)

        return preliminarySpec.copy(creativeFingerprint = resolvedFingerprint)
    }

    /**
     * Converts master specification into a bounded downstream GeneratorRequest.
     */
    fun createGeneratorRequest(spec: CreativeSpecification): GeneratorRequest {
        return GeneratorRequest(
            requestId = "GEN-REQ-${spec.specId.takeLast(6)}",
            title = spec.creativeDna.title,
            themeIntent = spec.creativeDna.narrativeIntent,
            requiredPhysicalAnchors = spec.witnessContract.mandatoryWitnessObjects.map { it.objectName },
            prohibitedTropes = spec.languageEnvelope.prohibitedGenericTropes,
            genreStyle = spec.themeEmotionStyle.genreStyle,
            acousticArrangementGuidance = spec.arrangementConstraints.coreAcousticRealization,
            tempoBpm = spec.themeEmotionStyle.tempoBpm,
            timeSignature = spec.themeEmotionStyle.timeSignature,
            vocalTimbre = spec.themeEmotionStyle.vocalRealization,
            specProvenanceHash = spec.provenance.currentProvenanceHash
        )
    }

    override fun auditGeneratedDraft(draft: GeneratedDraft, spec: CreativeSpecification): C16AuditReport {
        val auditId = "AUDIT-${UUID.randomUUID().toString().take(8).uppercase(Locale.US)}"

        // 1. Collision Audit (Component 02)
        val collisionAudit = fingerprintEngine.detectCollision(draft.rawLyricText, spec.creativeFingerprint)

        // 2. Physical Witness Evaluation (Component 03)
        val witnessEvaluation = witnessSystem.evaluateWitnesses(draft.rawLyricText, spec.witnessContract)

        // 3. Language & Contamination Audit (Component 04)
        val languageAudit = languageGovernor.auditLanguage(draft.rawLyricText, spec.languageEnvelope)

        // 4. Coherence Audit (Component 05)
        val coherenceAudit = themeEmotionStyleEngine.evaluateCoherence(
            theme = spec.themeEmotionStyle.theme,
            emotion = spec.themeEmotionStyle.emotionalProfile,
            style = draft.stylePrompt,
            draftText = draft.rawLyricText
        )

        // 5. Information Survival Audit (Component 07)
        val survivalReport = infoSurvivalEvaluator.auditInformationSurvival(spec, draft)

        // 6. Arrangement Leakage Audit (Component 08)
        val leakageAudit = leakageGuard.validateLeakageDeclaration(draft.stylePrompt)

        // 7. G1-G3 Governance Evaluations (Component 10)
        val g1Result = governanceRulesEngine.evaluateG1Witness(draft, spec)
        val g2Result = governanceRulesEngine.evaluateG2Diagnostic(draft, spec)
        val g3Result = governanceRulesEngine.evaluateG3Performance(draft)

        // 8. Dual-Witness Forensic Bridge (Component 11)
        val declaredWitness = dualWitnessBridge.buildDeclaredWitness(spec, draft)
        val measuredWitness = dualWitnessBridge.inspectMeasuredAudioWitness(null)
        val dualWitnessComparison = dualWitnessBridge.compareWitnessRecords(declaredWitness, measuredWitness)

        // Overall Governed Pass Status
        val isGovernedPass = witnessEvaluation.status == "PASS" &&
                languageAudit.isCompliant &&
                coherenceAudit.isHarmonized &&
                survivalReport.anchorsSurvived &&
                g1Result.isValid

        val rawAuditSeal = "C16_SEAL::$auditId::${spec.specId}::${draft.draftId}::$isGovernedPass::${System.currentTimeMillis()}"
        val sealHash = sha256(rawAuditSeal)

        val summary = buildString {
            append("C16 Audit: ${if (isGovernedPass) "PASSED // GOVERNED" else "REQUIRES CURATION"}. ")
            append("Witness: ${witnessEvaluation.status} (${witnessEvaluation.detectedWitnessObjects.size} objects). ")
            append("Language: ${if (languageAudit.isCompliant) "Compliant" else "Flagged"}. ")
            append("Survival: ${survivalReport.overallStatus.name}. ")
            append("G1: ${if (g1Result.isValid) "Valid" else "Invalid"}. ")
            append("G2 Band: ${g2Result.band.label}.")
        }

        return C16AuditReport(
            auditId = auditId,
            specId = spec.specId,
            draftId = draft.draftId,
            isGovernedPass = isGovernedPass,
            collisionAudit = collisionAudit,
            witnessEvaluation = witnessEvaluation,
            languageAudit = languageAudit,
            coherenceAudit = coherenceAudit,
            informationSurvivalReport = survivalReport,
            leakageAudit = leakageAudit,
            g1Witness = g1Result,
            g2Diagnostic = g2Result,
            g3Performance = g3Result,
            dualWitnessComparison = dualWitnessComparison,
            engineSealHash = sealHash,
            auditSummary = summary
        )
    }
}
