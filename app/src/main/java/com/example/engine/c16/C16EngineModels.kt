package com.example.engine.c16

import com.example.model.*
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

/**
 * ELYZARETH OS v38.1 — C16 ENGINE DOMAIN MODELS & ENVELOPES
 *
 * Encapsulates the complete typed data models for the 12-component C16 Engine:
 * 1. User Creative Intent & Creative Specification
 * 2. Creative Fingerprint & Binary vs. Creative Identity separation
 * 3. Physical Witness Contracts & 5-Pillar classification
 * 4. Language Constraint Envelope & Corpus Reference Profile
 * 5. Theme–Emotion–Style Coherence Envelopes
 * 6. ONM Provenance Records & PR-001..PR-004 boundary markers
 * 7. Information Survival Priority Hierarchy & Survival Audit Models
 * 8. Arrangement Leakage Envelopes
 * 9. 5-Room Acoustic Models & Measurement States
 * 10. G1–G3 Governance Results & Seals
 * 11. Dual-Witness Records (Declared vs. Measured)
 * 12. 3.2.1.0 Human Governor Protocol State
 */

/**
 * User Creative Intent supplied to C16 before any generation starts.
 */
data class UserCreativeIntent(
    val title: String,
    val storyConcept: String,
    val genre: String = "Acoustic Dark Folk",
    val stylePrompt: String = "warm acoustic fingerpicking, dry parlor vocal",
    val vocalTimbre: String = "Restrained Baritone",
    val desiredAnchors: List<String> = emptyList(),
    val targetBpm: Int = 64,
    val timeSignature: String = "4/4",
    val targetRoom: AcousticRoom = AcousticRoom.ROOM_05_RUSTIC,
    val existingLyricForCure: String? = null,
    val sourceCorpusReferenceId: String = "CORPUS-205769ac"
)

/**
 * Physical Witness Object candidate with qualitative 5-pillar grounding.
 * A physical noun is not automatically a witness object: requires permanence & spatial presence.
 */
data class WitnessObjectCandidate(
    val objectName: String,
    val pillar: WitnessPillar,
    val isSilentWitness: Boolean = true,
    val hasObjectPermanence: Boolean = true,
    val isNonDecorational: Boolean = true
)

/**
 * Component 03: Physical Witness Contract.
 */
data class PhysicalWitnessContract(
    val schemaVersion: String = "1.0.0",
    val requiredPillars: List<WitnessPillar> = listOf(WitnessPillar.DOMESTIC, WitnessPillar.ENVIRONMENTAL),
    val mandatoryWitnessObjects: List<WitnessObjectCandidate>,
    val minimumObjectCount: Int = 2,
    val nonDecorationalMandate: Boolean = true,
    val silentWitnessPrinciple: Boolean = true
)

/**
 * Component 04: Language Constraint Envelope.
 * Contains defensive lexical rules and marks unverified algorithms explicitly.
 */
data class LanguageConstraintEnvelope(
    val schemaVersion: String = "1.0.0",
    val physicalObjectMetaphorMandate: Boolean = true,
    val metaphorSoupProhibition: Boolean = true,
    val abstractEmotionDeclarationProhibition: Boolean = true,
    val decorativeProductionContaminationProhibition: Boolean = true,
    val unauthorizedProductionTerms: List<String> = listOf(
        "drop the bass", "autotune", "reverb wash", "synthetic riser", "sidechain compression", "heavy distortion"
    ),
    val prohibitedGenericTropes: List<String> = listOf(
        "neon tapestry", "symphony of stars", "echoes in the void",
        "tapestry of dreams", "whispers in the dark", "dance of shadows",
        "labyrinth of thoughts", "ocean of tears", "beacon of hope",
        "canvas of life", "symphony of silence", "threads of destiny",
        "shadows of yesterday", "shattered dreams", "wings of time",
        "ashes to ashes", "burning desire", "broken wings", "lost in the night"
    ),
    val corpusReferenceBinding: CorpusReferenceProfile = CorpusReferenceProfile(),
    // Explicitly unverified capabilities preserved as NOT FOUND / SPEC
    val deltaVScoreStatus: CapabilityStatus = CapabilityStatus.NOT_FOUND_UNVERIFIED,
    val rhymeAttractorScoreStatus: CapabilityStatus = CapabilityStatus.NOT_FOUND_UNVERIFIED,
    val lexicalTensionScoreStatus: CapabilityStatus = CapabilityStatus.NOT_FOUND_UNVERIFIED,
    val tfIdfDriftEngineStatus: CapabilityStatus = CapabilityStatus.SPEC_UNIMPLEMENTED
)

/**
 * Component 04 Reference: Corpus Identity / Reference Environment.
 * UUID: 205769ac. Serves as identity anchor source, NOT a closed-vocabulary starvation filter.
 */
data class CorpusReferenceProfile(
    val corpusUuid: String = "205769ac",
    val corpusName: String = "Elyzareth 700+ Item Reference Corpus",
    val bindingType: String = "IDENTITY_REFERENCE_ENVIRONMENT",
    val isClosedVocabularyFilter: Boolean = false // STRICT INVARIANT: False to prevent vocabulary starvation
)

/**
 * Component 05: Theme-Emotion-Style Envelope.
 * Relationship: THEME (physically anchored) -> Physical Witness -> EMOTION (emergent) -> contained through STYLE.
 */
data class ThemeEmotionStyleEnvelope(
    val theme: String,
    val emotionalProfile: String,
    val genreStyle: String,
    val tempoBpm: Int,
    val timeSignature: String,
    val acousticAtmosphere: String = "dry, sparse, intimate",
    val negativeSpaceMandate: Boolean = true,
    val vocalRealization: String = "restrained, close-mic, unhyped"
)

/**
 * Component 06: ONM & Provenance Record.
 */
data class ProvenanceRecord(
    val recordId: String,
    val specId: String,
    val parentProvenanceHash: String?,
    val currentProvenanceHash: String,
    val boundary: ProvenanceBoundary,
    val oneVariableOneObservationRule: Boolean = true,
    val eldsToOsHandshakeCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Component 07: Information Survival Spec & Hierarchy.
 */
data class InformationSurvivalSpecification(
    val priorityRanks: List<InformationPriorityRank> = listOf(
        InformationPriorityRank.ANCHORS,
        InformationPriorityRank.STATE_MARKERS,
        InformationPriorityRank.NUMERIC_SERIES,
        InformationPriorityRank.MULTI_COMPONENT_ARRAYS
    ),
    val anchorSurvivalMandatory: Boolean = true,
    val stateMarkerSurvivalMandatory: Boolean = true,
    // Explicitly unverified numerical survival algorithm status
    val numericalSurvivalFormulaStatus: CapabilityStatus = CapabilityStatus.NOT_FOUND_UNVERIFIED
)

/**
 * Component 09: Room Acoustic Specification.
 */
data class RoomAcousticSpecification(
    val room: AcousticRoom,
    val physicalDescription: String,
    val defaultMaxT60Seconds: Float?, // null if NOT MEASURED
    val defaultMaxWetRatioPercent: Float?, // null if NOT MEASURED
    val isT60Measured: Boolean = false,
    val isWetRatioMeasured: Boolean = false,
    val sparseArrangementConstraints: SparseArrangementConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0
)

/**
 * Component 02: Creative Fingerprint (Distinguished from SHA-256 Binary Hash).
 * SHA-256 = Binary Identity
 * Creative Fingerprint = Creative Identity
 */
data class CreativeFingerprint(
    val binarySha256: String,
    val creativeIdentityToken: String,
    val anchorSignature: String,
    val emotionalSignature: String,
    val acousticSignature: String,
    // Explicitly unverified vector/cosine similarity capabilities
    val vectorEmbeddingStatus: CapabilityStatus = CapabilityStatus.SPEC_UNIMPLEMENTED,
    val cosineSimilarityStatus: CapabilityStatus = CapabilityStatus.SPEC_UNIMPLEMENTED
)

/**
 * Master Creative Specification composed by C16 Engine.
 * Delivered downstream to the Generator Adapter.
 */
data class CreativeSpecification(
    val specId: String,
    val creativeDna: CreativeDnaProfile,
    val witnessContract: PhysicalWitnessContract,
    val languageEnvelope: LanguageConstraintEnvelope,
    val themeEmotionStyle: ThemeEmotionStyleEnvelope,
    val provenance: ProvenanceRecord,
    val informationSurvival: InformationSurvivalSpecification,
    val arrangementConstraints: SparseArrangementConstraints,
    val acousticRoom: RoomAcousticSpecification,
    val creativeFingerprint: CreativeFingerprint,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Downstream Generator Request containing strictly bounded envelopes.
 */
data class GeneratorRequest(
    val requestId: String,
    val title: String,
    val themeIntent: String,
    val requiredPhysicalAnchors: List<String>,
    val prohibitedTropes: List<String>,
    val genreStyle: String,
    val acousticArrangementGuidance: String,
    val tempoBpm: Int,
    val timeSignature: String,
    val vocalTimbre: String,
    val specProvenanceHash: String
)

/**
 * Generated Output Draft proposed by Generator Adapter.
 * Generator proposes expression; C16 audits it.
 */
data class GeneratedDraft(
    val draftId: String,
    val specId: String,
    val rawLyricText: String,
    val stanzas: List<Stanza>,
    val stylePrompt: String,
    val tempoBpm: Int,
    val timeSignature: String,
    val vocalTimbre: String,
    val isCureOutput: Boolean = false,
    val generatorTimestamp: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------------------------------------------
// AUDIT & FORENSIC MODELS
// -------------------------------------------------------------------------------------------------

data class CollisionAuditResult(
    val collisionDetected: Boolean,
    val matchingBaselineId: String? = null,
    val details: String,
    val status: CapabilityStatus = CapabilityStatus.VERIFIED_IMPLEMENTED
)

data class PhysicalWitnessEvaluation(
    val status: String, // "PASS" or "FAIL"
    val detectedWitnessObjects: List<String>,
    val detectedPillars: List<WitnessPillar>,
    val nonDecorationalSatisfied: Boolean,
    val failReason: String? = null,
    val diagnosticResult: PhysicalAnchorDiagnosticResult
)

data class LanguageAuditResult(
    val isCompliant: Boolean,
    val detectedTropes: List<String>,
    val metaphorSoupFlagged: Boolean,
    val unanchoredEmotionFlagged: Boolean,
    val contaminationAnomalies: List<String>,
    val summary: String
)

data class CoherenceAuditResult(
    val isHarmonized: Boolean, // Canonical Binary Evaluation (PASS/FAIL)
    val diagnosticNotes: String,
    val unverifiedScalarScore: Float? = null, // Quarantined / Non-canonical
    val isNumericalThresholdEnforced: Boolean = false // Quarantined / Non-canonical
)

data class SurvivalAuditItem(
    val label: String,
    val priorityRank: InformationPriorityRank,
    val state: SurvivalAuditState,
    val notes: String
)

data class InformationSurvivalReport(
    val items: List<SurvivalAuditItem>,
    val anchorsSurvived: Boolean,
    val stateMarkersSurvived: Boolean,
    val overallStatus: SurvivalAuditState,
    val summary: String
)

data class LeakageAuditResult(
    val zeroLeakageCompliant: Boolean,
    val flaggedTerms: List<String>,
    val notes: String
)

data class DeclaredWitnessRecord(
    val specId: String,
    val textHash: String,
    val declaredWitnessObjects: List<String>,
    val declaredAcousticRoom: String,
    val declaredBpm: Int,
    val declaredGenre: String,
    val provenanceHash: String
)

data class MeasuredWitnessRecord(
    val isAudioProvided: Boolean,
    val audioSha256: String?,
    val pcmDecodedValid: Boolean,
    val isMeasured: Boolean, // Explicit: false when audio is null (NOT_MEASURED != FAILED)
    val failureReason: String? = null
)

data class DualWitnessComparison(
    val declared: DeclaredWitnessRecord,
    val measured: MeasuredWitnessRecord,
    val isDualWitnessSatisfied: Boolean,
    val forensicStatusMessage: String
)

enum class GovernorProtocolStep(val order: Int, val label: String) {
    STEP_3_LISTEN(3, "3. LISTEN // Realize acoustic and textual specimen"),
    STEP_2_EVALUATE(2, "2. EVALUATE // Review G1-G5 audit and forensic telemetry"),
    STEP_1_DECIDE(1, "1. DECIDE // Curator selects disposition"),
    STEP_0_FREEZE(0, "0. FREEZE // Human Governor issues immutable lock")
}

data class GovernorProtocolState(
    val currentStep: GovernorProtocolStep,
    val isHumanGovernorAuthorized: Boolean,
    val currentDisposition: GovernanceDispositionChoice,
    val statusMessage: String
)

/**
 * Comprehensive C16 Post-Generation Audit Report.
 */
data class C16AuditReport(
    val auditId: String,
    val specId: String,
    val draftId: String,
    val isGovernedPass: Boolean,
    val collisionAudit: CollisionAuditResult,
    val witnessEvaluation: PhysicalWitnessEvaluation,
    val languageAudit: LanguageAuditResult,
    val coherenceAudit: CoherenceAuditResult,
    val informationSurvivalReport: InformationSurvivalReport,
    val leakageAudit: LeakageAuditResult,
    val g1Witness: G1WitnessResult,
    val g2Diagnostic: G2DiagnosticResult,
    val g3Performance: G3PerformanceResult,
    val dualWitnessComparison: DualWitnessComparison,
    val engineSealHash: String,
    val auditSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)
