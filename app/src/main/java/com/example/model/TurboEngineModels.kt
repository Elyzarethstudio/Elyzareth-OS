package com.example.model

/**
 * Turbo Engine (App 01 Intelligence Layer) Data Models
 * Encapsulates the recovered 60-day Elyzareth Turbo Engine invariants:
 * - C16 Creative Intelligence Runtime / C15 Witness Ledger / Creative DNA Registry
 * - Creative Fingerprint Engine (CFE) & Collision Detection
 * - Physical Anchor Principle (tactile concrete objects)
 * - Information Survival Hierarchy (Witness Objects > Narrative Arc > Meter > Decoration)
 * - Object Narrative Mutation (ONM) & Provenance Rules
 * - Theme ↔ Emotional Profile ↔ Style Coherence Checking
 * - Cliché / Prohibited Trope Suppression
 * - Contamination & Unwanted-Expression Detection
 */

enum class TurboEngineMode(val label: String, val description: String) {
    GENERATE("GENERATE", "Engine first: Create from governed creative DNA & physical anchors"),
    CURE("CURE / POLISH", "Surgical single-variable repair preserving creative DNA & witness objects")
}

data class CreativeDnaProfile(
    val dnaId: String,
    val title: String,
    val narrativeIntent: String,
    val emotionalIdentity: String,
    val physicalWitnessAnchors: List<String>,
    val acousticAffinities: List<String>,
    val meterStressCadence: String = "4/4 Iambic",
    val prohibitedClichésPurged: List<String> = emptyList(),
    val provenanceHash: String = ""
)

/**
 * DIAGNOSTIC ONLY Physical Anchor validation result schema.
 * Purely diagnostic; does not rewrite or mutate lyrics.
 */
data class PhysicalAnchorDiagnosticResult(
    val schemaVersion: String = "1.0.0",
    val status: String, // "PASS" or "FAIL"
    val anchorObjects: List<String>,
    val anchorDensity: String, // e.g. "1.5 (3 objects / 2 stanzas)"
    val collisionFlags: List<String> = emptyList(),
    val metaphorSoupDetected: Boolean = false,
    val abstractEmotionUnanchoredDetected: Boolean = false,
    val failReason: String? = null,
    val diagnosticTimestamp: Long = System.currentTimeMillis()
)

/**
 * ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS v1.0
 * Non-destructive downstream production constraint specification extracted from Flow forensic evidence.
 * Declared production constraints != Measured audio evidence.
 */
data class SparseArrangementConstraints(
    val schemaVersion: String = "1.0.0",
    val constraintId: String = "ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1.0",
    val percussiveSuppression: List<String> = listOf(
        "kicks", "snares", "hi-hats", "shakers", "tambourines", "hand percussion", "percussive loops"
    ),
    val syntheticAndOrchestralPadSuppression: List<String> = listOf(
        "synth pads", "bowed string sections", "drones", "organ fills", "atmospheric synth washes"
    ),
    val coreAcousticRealization: String = "steel-string acoustic fingerpicking + subtle thumb-plucked bass movement",
    val excludedInstrumentation: List<String> = listOf(
        "electric guitars", "dense chordal strumming blocks", "sub-bass", "synth bass"
    ),
    val sectionalExpansionRule: String = "sectional expansion primarily through vocal-layer density rather than automatic instrumental expansion",
    val prohibitedDynamics: List<String> = listOf(
        "cinematic swells", "crescendo risers", "reverse cymbals", "expanding artificial reverb tails"
    ),
    val dynamicRestraintProfile: String = "restrained dynamics and high negative space",
    val zeroArrangementDrift: Boolean = true,
    val arrangementDriftTargetPercent: Float = 0.0f,
    val isMeasuredAudioEvidence: Boolean = false // Architectural Boundary: Declared Production Constraints != Measured Audio Evidence
)

val ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0 = SparseArrangementConstraints()

/**
 * ELYZARETH_RUSTIC_ACOUSTIC_v1
 * Non-destructive downstream production constraint / intent object.
 * Declared constraint != Measured audio evidence.
 */
data class ElyzarethAcousticConstraint(
    val schemaVersion: String = "1.0.0",
    val constraintId: String = "ELYZARETH_RUSTIC_ACOUSTIC_v1",
    val roomProfile: String = "Room 05 / Rustic / Dry Parlor",
    val maxT60Seconds: Float = 0.4f,
    val maxWetRatioPercent: Float = 15f,
    val vocalSpec: String = "close-mic male baritone",
    val instrumentationSpec: String = "steel-string acoustic fingerpicking",
    val prohibitedElements: List<String> = listOf("drums", "percussion", "synth pads", "strings"),
    val negativeSpaceProfile: String = "high negative-space profile",
    val dynamicRangeProfile: String = "restrained dynamics",
    val sectionalExpansionRule: String = "sectional expansion primarily through vocal-layer density",
    val arrangementDriftTargetPercent: Float = 0f,
    val sparseArrangementConstraints: SparseArrangementConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0,
    val isMeasuredAudioEvidence: Boolean = false // Explicit distinction: DECLARED CONSTRAINT != MEASURED AUDIO EVIDENCE
)

val ELYZARETH_RUSTIC_ACOUSTIC_v1 = ElyzarethAcousticConstraint()

data class TurboValidationReport(
    val isGoverned: Boolean,
    val c16DnaVerified: Boolean,
    val physicalAnchorCount: Int,
    val physicalAnchorsFound: List<String>,
    val prohibitedTropesDetected: List<String>,
    val themeStyleCoherenceScore: Float, // 0.0 to 1.0
    val coherenceDiagnosis: String,
    val contaminationDetected: Boolean,
    val collisionDetected: Boolean,
    val collisionDetails: String? = null,
    val informationSurvivalScore: Float, // 0.0 to 1.0
    val engineDiagnosticSummary: String,
    val physicalAnchorDiagnostic: PhysicalAnchorDiagnosticResult? = null,
    val turboTimestamp: Long = System.currentTimeMillis()
)

data class TurboSongOutput(
    val generatedSong: GeneratedSong,
    val creativeDna: CreativeDnaProfile,
    val validationReport: TurboValidationReport,
    val acousticConstraint: ElyzarethAcousticConstraint = ELYZARETH_RUSTIC_ACOUSTIC_v1,
    val isCureOutput: Boolean = false,
    val engineSeal: String = ""
)
