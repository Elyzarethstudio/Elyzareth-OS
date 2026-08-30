package com.example.engine.c16

import com.example.model.*

/**
 * ELYZARETH OS v38.1 — C16 CANONICAL ENGINE CONTRACTS
 *
 * Defines the typed interface contracts for the 12 canonical C16 Engine components.
 * Core Architectural Principle:
 * USER CREATIVE INTENT -> C16 -> COMPONENT CONSTRAINTS -> CREATIVE SPECIFICATION ->
 * LYRIC / MUSIC GENERATOR -> GENERATED OUTPUT -> C16 / COMPONENT AUDIT ->
 * G1 -> G2 -> G3 -> G4 -> G5 -> DUAL-WITNESS EVIDENCE -> HUMAN GOVERNOR -> FREEZE
 */

/**
 * Capability Verification Status for Engine Algorithms.
 * Distinguishes verified and implemented capabilities from unverified/unimplemented specs.
 * Explicitly prevents fabricating heuristics for missing specifications.
 */
enum class CapabilityStatus {
    VERIFIED_IMPLEMENTED,
    PROVISIONALLY_APPROVED,
    SPEC_UNIMPLEMENTED,
    NOT_FOUND_UNVERIFIED
}

/**
 * Audit Status for Information Survival & Forensic Verification.
 */
enum class SurvivalAuditState {
    VERIFIED,
    IMPLEMENTED_UNVERIFIED,
    FAILED,
    SPEC
}

/**
 * Five Witness Pillars for Physical Grounding (Component 03).
 */
enum class WitnessPillar(val label: String, val description: String) {
    DOMESTIC("Domestic", "Everyday household and personal tactile items (e.g. table, coat, chair, coffee, cup, mantel)"),
    ENVIRONMENTAL("Environmental", "Natural landscape and architectural structures (e.g. railway, limestone, cedar, bridge, river, snow)"),
    RECOVERY("Recovery", "Artifacts of healing, endurance, and quiet survival (e.g. medicine bottle, worn quilt, walking stick)"),
    CULTURAL("Cultural", "Historical and artisanal human implements (e.g. silver coin, acoustic guitar, iron latch, lantern)"),
    TRANSCENDENTAL("Transcendental", "Physical objects bearing solemn symbolic weight (e.g. rusted key, pocket compass, amber stone)")
}

/**
 * 5-Room Acoustic Model Taxonomy (Component 09).
 * Classification based on acoustic/environmental physics, not genre.
 */
enum class AcousticRoom(val code: String, val label: String, val description: String) {
    ROOM_01_SHARED("ROOM_01_SHARED", "Shared Live Acoustic Space", "Natural room reflections with moderate diffusion and shared ambience"),
    ROOM_02_VACUUM("ROOM_02_VACUUM", "Anechoic / Vacuum Chamber", "Near-zero reflections, hyper-isolated dry signal with ultra-close proximity"),
    ROOM_03_CONTROL("ROOM_03_CONTROL", "Treated Control Room", "Critically damped studio acoustic environment with linear decay"),
    ROOM_04_MEMORY("ROOM_04_MEMORY", "Diffuse Resonant Chamber", "Warm decay envelope evoking temporal distance and spatial memory"),
    ROOM_05_RUSTIC("ROOM_05_RUSTIC", "Dry Parlor / Timber Room", "Intimate dry room with timber reflections and prominent negative space (Elyzareth baseline)"),
    ROOM_06_BLANK_RESERVED("ROOM_06_BLANK_RESERVED", "Reserved Acoustic Profile", "Unallocated room slot reserved for future calibrated environments")
}

/**
 * Information Priority Hierarchy for Information Survival (Component 07).
 * Anchors are highest-priority information.
 */
enum class InformationPriorityRank(val rankOrder: Int, val label: String) {
    ANCHORS(1, "Physical Witness Anchors (Highest Priority)"),
    STATE_MARKERS(2, "State & Lineage Markers"),
    NUMERIC_SERIES(3, "Numeric Series & Meter Counts"),
    MULTI_COMPONENT_ARRAYS(4, "Multi-Component Arrays & Rhyme/Decoration (Lowest Priority)")
}

/**
 * Provenance Boundary Categories (Component 06).
 */
enum class ProvenanceBoundary(val code: String, val description: String) {
    PR_001_CORPUS_LINEAGE("PR-001", "Binding to reference corpus identity (UUID: 205769ac)"),
    PR_002_MUTATION_BOUNDARY("PR-002", "Single-variable parameter variation boundary"),
    PR_003_ELDS_OS_HANDSHAKE("PR-003", "Diagnostic proposal to OS governance handshake"),
    PR_004_GOVERNOR_AUTHORIZATION("PR-004", "Explicit Human Governor sign-off before lock")
}

// -------------------------------------------------------------------------------------------------
// 12 COMPONENT INTERFACE CONTRACTS
// -------------------------------------------------------------------------------------------------

/** Component 01 Contract: C16 Turbo / Cognitive Orchestration */
interface IC16CognitiveOrchestrator {
    fun composeCreativeSpecification(intent: UserCreativeIntent): CreativeSpecification
    fun auditGeneratedDraft(draft: GeneratedDraft, spec: CreativeSpecification): C16AuditReport
}

/** Component 02 Contract: Creative Fingerprint + Collision Detection */
interface ICreativeFingerprintEngine {
    fun extractFingerprint(spec: CreativeSpecification): CreativeFingerprint
    fun detectCollision(draftLyric: String, fingerprint: CreativeFingerprint): CollisionAuditResult
    fun getCapabilityStatus(): CapabilityStatus
}

/** Component 03 Contract: Physical Anchor / Witness System */
interface IPhysicalWitnessSystem {
    fun establishWitnessContract(intent: UserCreativeIntent): PhysicalWitnessContract
    fun evaluateWitnesses(lyricText: String, contract: PhysicalWitnessContract): PhysicalWitnessEvaluation
}

/** Component 04 Contract: Lyric Language / Cliché / Contamination */
interface ILyricLanguageGovernor {
    fun generateLanguageEnvelope(intent: UserCreativeIntent): LanguageConstraintEnvelope
    fun auditLanguage(lyricText: String, envelope: LanguageConstraintEnvelope): LanguageAuditResult
    fun getCorpusReferenceProfile(): CorpusReferenceProfile
}

/** Component 05 Contract: Theme–Emotion–Style Coherence */
interface IThemeEmotionStyleEngine {
    fun composeThemeEmotionStyleEnvelope(intent: UserCreativeIntent): ThemeEmotionStyleEnvelope
    fun evaluateCoherence(theme: String, emotion: String, style: String, draftText: String): CoherenceAuditResult
}

/** Component 06 Contract: ONM / Provenance Registry */
interface IOnmProvenanceRegistry {
    fun createLineageRecord(specId: String, parentHash: String?, authorIntent: String): ProvenanceRecord
    fun validateMutationBoundary(originalHash: String, mutatedHash: String, boundary: ProvenanceBoundary): Boolean
}

/** Component 07 Contract: Information Survival Hierarchy */
interface IInformationSurvivalEvaluator {
    fun auditInformationSurvival(originalSpec: CreativeSpecification, generatedDraft: GeneratedDraft): InformationSurvivalReport
}

/** Component 08 Contract: Arrangement Leakage Guard */
interface IArrangementLeakageGuard {
    fun getArrangementConstraints(): SparseArrangementConstraints
    fun validateLeakageDeclaration(stylePrompt: String): LeakageAuditResult
}

/** Component 09 Contract: 5-Room Acoustic Model */
interface IFiveRoomAcousticModel {
    fun getRoomSpecification(room: AcousticRoom): RoomAcousticSpecification
}

/** Component 10 Contract: G1 / G2 / G3 Governance Rules */
interface IGovernanceRulesEngine {
    fun evaluateG1Witness(draft: GeneratedDraft, spec: CreativeSpecification): G1WitnessResult
    fun evaluateG2Diagnostic(draft: GeneratedDraft, spec: CreativeSpecification): G2DiagnosticResult
    fun evaluateG3Performance(draft: GeneratedDraft): G3PerformanceResult
}

/** Component 11 Contract: Dual-Witness + Forensic Audio Bridge */
interface IDualWitnessForensicBridge {
    fun buildDeclaredWitness(spec: CreativeSpecification, draft: GeneratedDraft): DeclaredWitnessRecord
    fun inspectMeasuredAudioWitness(audioBytes: ByteArray?): MeasuredWitnessRecord
    fun compareWitnessRecords(declared: DeclaredWitnessRecord, measured: MeasuredWitnessRecord): DualWitnessComparison
}

/** Component 12 Contract: 3.2.1.0 + Human Governor Protocol */
interface IHumanGovernorProtocol {
    fun evaluateProtocolStep(currentStep: GovernorProtocolStep, authorization: HumanGovernorAuthorization?): GovernorProtocolState
}
