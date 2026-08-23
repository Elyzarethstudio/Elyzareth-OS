package com.example.model

/**
 * ELYZARETH GOVERNANCE SPEC v1 DOMAIN MODELS
 * Authoritative implementation models for G1–G5 pipeline (G6 deferred).
 */

enum class WitnessStatus {
    UNKNOWN,
    IMMUTABLE_WITNESS,
    EXAMINED,
    SEALED,
    REJECTED
}

enum class G1PathType {
    TEXT_SCHEMA,
    PHYSICAL_AUDIO,
    DUAL_VERIFIED
}

enum class AudioRegistrationStatus {
    NOT_APPLICABLE_PENDING_AUDIO_RENDER,
    PHYSICAL_AUDIO_VERIFIED,
    PHYSICAL_AUDIO_FAILED
}

data class G1WitnessResult(
    val isValid: Boolean,
    val pathType: G1PathType,
    val textHash: String,
    val isSchemaValid: Boolean,
    val schemaValidationErrors: List<String> = emptyList(),
    val audioStatus: AudioRegistrationStatus = AudioRegistrationStatus.NOT_APPLICABLE_PENDING_AUDIO_RENDER,
    val audioWitnessCertificate: String? = null,
    val textWitnessCertificate: String,
    val registryTimestamp: Long = System.currentTimeMillis()
)

enum class G2DiagnosticBand(val label: String, val range: String) {
    COMPLIANT("G2-COMPLIANT", "90–100"),
    TOLERANT("G2-TOLERANT", "80–89"),
    DRIFT("G2-DRIFT", "70–79"),
    FAILURE("G2-FAILURE", "<70")
}

data class G2DiagnosticResult(
    val band: G2DiagnosticBand,
    val provisionalScore: Int, // 0..100
    val isProvisionalFormula: Boolean = true,
    val physicalAnchorCount: Int,
    val physicalAnchorsFound: List<String>,
    val prohibitedLexiconCount: Int,
    val prohibitedTermsFound: List<String>,
    val diagnosticSummary: String,
    val evidenceDetails: List<String>
)

data class G3PerformanceResult(
    val isAudioMeasured: Boolean = false,
    val isProfileBaselineMeasured: Boolean = false,
    val baselineProfile: String = "Acoustic Dark Folk (63–65 BPM baseline)",
    val vocalNaturalnessScore: Float? = null, // null when physical audio unavailable
    val formantStability: String? = null,     // null when physical audio unavailable
    val pitchTranspositionInterval: String? = null,
    val cadenceNaturalness: String? = null,
    val performanceArtifacts: List<String> = emptyList(),
    val diagnosticNotes: String = "Physical audio/vocal evidence unavailable (NOT MEASURED)"
)

data class G4AcousticObservation(
    val isAudioMeasured: Boolean = false,
    val isDeferred: Boolean = true,
    val isBlocking: Boolean = false,
    val dryRoomCharacter: String? = null,
    val negativeSpaceObservation: String? = null,
    val directToReverberantRelationship: String? = null,
    val t60QualitativeTrend: String? = null,
    val classMorphology: String? = null,
    val arrangementLeakage: String? = null,
    val statusNote: String = "G4 NOT MEASURED (Physical acoustic evidence unavailable)"
)

/**
 * Architectural Subsystem Classification
 *
 * ELDS-C / Elyzareth Lyrics Doctor System — Curation & Witness Domain:
 * - Focus: lyric diagnosis, corpus curation, textual/structural witness auditing.
 * - Used by: App 02 / Corpus Curator / The Sitting Room.
 * - Responsible for: lyric integrity, witness objects, evidence diagnosis, curation, disposition and freeze.
 * - Non-Mutation: ELDS-C is NOT an experimental mutation engine.
 *
 * ELDS-M / Mutation & Experimental Domain:
 * - Focus: controlled experimentation, acoustic/DSP investigation, parameter mutation and forensic research.
 * - Operates through: controlled experiments (e.g. one variable → one observation).
 * - Quarantine Destination: failed/experimental specimens are routed here through Quarantine.
 * - Immutability Rule: ELDS-M must never modify or overwrite the Immutable Witness Vault or frozen master releases.
 * - Governance Authority: Mutation results return as evidence/proposals; Elyzareth OS remains the governance authority.
 *
 * Critical Architectural Boundary:
 * ELDS-C = CURATION
 * ELDS-M = MUTATION
 * ELYZARETH OS = GOVERNANCE / AUTHORITY
 */
enum class EldsSubsystemDomain(val code: String, val label: String, val purpose: String) {
    ELDS_C("ELDS-C", "Curation & Witness Domain", "Lyric diagnosis, corpus curation, textual/structural witness auditing (App 02 / The Sitting Room)"),
    ELDS_M("ELDS-M", "Mutation & Experimental Domain", "Acoustic/DSP experimentation, parameter mutation, isolated quarantine sandbox (Non-App 02)")
}

enum class GovernanceDispositionChoice(val label: String, val routingTarget: String) {
    RELEASE_ACCEPT("RELEASE / ACCEPT", "ELYZARETH_FINAL/ (Release & Master Lock)"),
    MINOR_CURE("MINOR CURE", "Elyzareth Engine / App 01 (Single-variable localized cure)"),
    FULL_RECONSTRUCTION("FULL RECONSTRUCTION", "Elyzareth Engine / App 01 (Full architectural rebuild)"),
    PERMANENT_REJECT("PERMANENT REJECT", "Halted & Sealed (Immutable witness permanently preserved; zero deletion)"),
    PENDING_HUMAN_GOVERNOR("PENDING HUMAN DECISION", "Awaiting Human Governor Review (3.2.1.0 Protocol)"),
    QUARANTINE_ELDS_M_MUTATION("QUARANTINE / ELDS-M MUTATION", "ELDS-M Experimental Sandbox (Isolated from Witness Vault; zero overwrite)"),
    PURIFY_RECURATE("PURIFY / RE-CURATE", "Curatorial Re-alignment / G1 Re-examination")
}

data class G5Disposition(
    val chosenDisposition: GovernanceDispositionChoice = GovernanceDispositionChoice.PENDING_HUMAN_GOVERNOR,
    val isHumanGovernorAuthorized: Boolean = false,
    val governorNotes: String = "",
    val decisionTimestamp: Long = 0L,
    val protocolStep: String = "3.2.1.0 (Listen → Evaluate → Decide → Freeze)",
    val isAutomatedAI: Boolean = false // MUST always be false
)

/**
 * G4 Human Governor Explicit Authorization Boundary
 * AI/evaluation code must NEVER self-authorize final creative commitment.
 */
data class HumanGovernorAuthorization(
    val authorizationId: String,
    val governorIdentity: String, // e.g. "HUMAN_GOVERNOR" (never AI/SYSTEM)
    val specimenId: String,
    val expectedEvidenceHash: String, // SHA-256 of text + audio + schema at authorization time
    val dispositionChoice: GovernanceDispositionChoice,
    val timestamp: Long = System.currentTimeMillis(),
    val governorNotes: String = "",
    val isExplicitlyHumanAuthorized: Boolean = true,
    val isStale: Boolean = false,
    val isAutomatedAI: Boolean = false
)

sealed class AuthorizationValidationResult {
    data class Valid(val authorization: HumanGovernorAuthorization) : AuthorizationValidationResult()
    data class UnauthorizedMissingGovernor(val reason: String) : AuthorizationValidationResult()
    data class MismatchEvidenceChanged(val reason: String, val expectedHash: String, val actualHash: String) : AuthorizationValidationResult()
    data class StaleExpired(val reason: String) : AuthorizationValidationResult()
    data class AutomatedAiRejected(val reason: String) : AuthorizationValidationResult()
}

/**
 * G5 Master Release Manifest & Protection
 * Deterministic binding to ELYZARETH_FINAL/
 */
data class MasterReleaseManifest(
    val releaseId: String,
    val releaseTargetDirectory: String = "ELYZARETH_FINAL/",
    val specimenId: String,
    val versionId: String,
    val textHash: String,
    val physicalAudioHash: String?,
    val evidenceSchemaHash: String,
    val combinedManifestHash: String,
    val authorizationId: String,
    val g1Certificate: String,
    val g2DiagnosticBand: String,
    val g3PerformanceStatus: String,
    val humanGovernorStamp: String,
    val releaseTimestamp: Long = System.currentTimeMillis(),
    val isSealedAndFrozen: Boolean = true
)

sealed class MasterReleaseResult {
    data class Success(
        val manifest: MasterReleaseManifest,
        val committedSpecimen: SpecimenVersion
    ) : MasterReleaseResult()

    data class Rejected(
        val reason: String,
        val violationCode: String
    ) : MasterReleaseResult()
}

sealed class FreezeResult {
    data class Success(val specimenId: String, val manifestHash: String?) : FreezeResult()
    data class Rejected(val reason: String, val errorCode: String) : FreezeResult()
}

enum class G6Status(val label: String, val description: String) {
    NOT_SPECIFIED_DEFERRED("G6 DEFERRED / NOT SPECIFIED", "Excluded from active production governance. No storefront/catalog contract.")
}

data class AuditRecord(
    val id: String,
    val timestamp: String,
    val stage: String, // G1_WITNESS, G2_DIAGNOSTIC, G3_PERFORMANCE, G4_OBSERVATION, G5_HUMAN_DECISION
    val action: String,
    val evidenceHash: String,
    val detail: String,
    val actor: String // e.g. "SYSTEM_STATIC_CHECK", "HUMAN_GOVERNOR", "INGRESS_RECEIVER"
)

data class FullGovernancePackage(
    val g1Witness: G1WitnessResult,
    val g2Diagnostic: G2DiagnosticResult,
    val g3Performance: G3PerformanceResult,
    val g4Acoustic: G4AcousticObservation,
    val g5Disposition: G5Disposition,
    val g6Status: G6Status = G6Status.NOT_SPECIFIED_DEFERRED,
    val auditTrail: List<AuditRecord> = emptyList()
)

/**
 * App 02 Artifact Ingress Package (Local Drive / Google Drive)
 *
 * App 02 is an independent corpus-ingestion and forensic-curation application.
 * It ingests physical artifact packages containing:
 * - JSON witness evidence (schema/structural metadata)
 * - Raw audio binary (MP3/WAV/PCM byte payload)
 * - Canonical lyric/text artifact
 *
 * Zero-Inference Invariant:
 * Filenames and metadata headers are NEVER authoritative.
 * Physical payloads are byte-inspected and hashed before ELDS-C evaluation.
 *
 * App 01 is the creation/correction app and does NOT inherit folder/file ingestion.
 */
data class SpecimenArtifactPackage(
    val packageId: String,
    val title: String,
    val sourceOrigin: IngressSourceOrigin, // e.g. LOCAL_FOLDER, GOOGLE_DRIVE, LAPTOP, IMPORTED_CORPUS
    val declaredLocationOrPath: String? = null,
    val lyricTextBytes: ByteArray? = null,
    val rawLyricString: String? = null,
    val jsonWitnessBytes: ByteArray? = null,
    val rawWitnessString: String? = null,
    val audioBinaryBytes: ByteArray? = null,
    val audioFormatDeclared: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SpecimenArtifactPackage
        return packageId == other.packageId && sourceOrigin == other.sourceOrigin
    }

    override fun hashCode(): Int {
        return packageId.hashCode()
    }
}

/**
 * Reconciled Physical Specimen (Post-Reconciliation, Pre-Evaluation)
 */
data class ReconciledArtifactPackage(
    val packageId: String,
    val title: String,
    val sourceOrigin: IngressSourceOrigin,
    val resolvedLyricText: String,
    val textBinaryHash: String,
    val resolvedWitness: LyricEvidence,
    val witnessJsonHash: String,
    val isAudioProvided: Boolean,
    val resolvedAudioMetrics: AudioWitnessMetrics?,
    val audioBinaryHash: String?,
    val reconciliationWarnings: List<String> = emptyList(),
    val reconciliationTimestamp: Long = System.currentTimeMillis()
)

/**
 * Structured Cure Request (App 02/ELDS-C → Elyzareth OS → App 01)
 *
 * The Cure Loop Contract:
 * 1. App 02 / ELDS-C evaluates specimen and diagnoses defects (e.g. G2 anchor deficiency, drift).
 * 2. Elyzareth OS constructs and routes this StructuredCureRequest to App 01.
 * 3. App 01 creates the revised lyric/specimen artifact (creation/correction domain).
 * 4. Revised artifact package is subsequently ingested by App 02 for independent re-curation.
 */
data class StructuredCureRequest(
    val requestId: String,
    val sourceSpecimenId: String,
    val sourceVersionId: String,
    val title: String,
    val originalLyricText: String,
    val originalEvidence: LyricEvidence,
    val diagnosticSummary: String,
    val gateFlags: List<String>,
    val targetVariablesToHeal: List<String>,
    val cureRecommendation: String,
    val governorNotes: String = "",
    val routedBy: String = "ELYZARETH_OS",
    val sourceApp: String = "APP_02_CORPUS_CURATOR",
    val destinationApp: String = "APP_01_CREATION_ENGINE",
    val timestamp: Long = System.currentTimeMillis()
)

