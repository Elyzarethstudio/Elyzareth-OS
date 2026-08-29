package com.example.model

enum class SpecimenDecision {
    ACCEPT,
    NEEDS_HEALING,
    NOT_ELIGIBLE,
    NOT_YET_EXAMINED
}

enum class GateStatus {
    PASS,
    FLAGGED,
    CURE_RECOMMENDED,
    FAIL,
    UNEXAMINED
}

enum class IngressSourceOrigin {
    LAPTOP,
    GOOGLE_DRIVE,
    LOCAL_FOLDER,
    IMPORTED_CORPUS,
    EXTERNAL_IMPORT
}

data class GateDiagnostic(
    val gateId: String,          // G1, G2, G3, G4, G5, G6
    val name: String,            // WITNESS GATE, PHYSICAL ANCHOR, PERFORMANCE, ACOUSTIC, HUMAN GOVERNOR, G6 DEFERRED
    val status: GateStatus,      // PASS, FLAGGED, CURE_RECOMMENDED, FAIL, UNEXAMINED
    val score: Float,           // 0.0 - 1.0
    val summary: String,
    val detailedEvidence: List<String>
)

data class AudioWitnessMetrics(
    val isMeasured: Boolean = true,
    val durationSeconds: Int = 227,
    val durationFormatted: String = "03:47",
    val decoderStatus: String = "PASS",
    val pcmStatus: String = "VERIFIED",
    val transientStatus: String = "VERIFIED",
    val fingerprintStatus: String = "VERIFIED",
    val sampleRateKhz: Float = 44.1f,
    val channels: Int = 2,
    val physicalFileHash: String = "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val acousticFingerprint: String = "FP-ACOUSTIC-9921-A",
    val peakDb: Float = -0.3f
)

data class SpecimenHistoryEntry(
    val timestamp: String,
    val action: String,
    val detail: String,
    val sourceOrigin: String
)

data class SpecimenVersion(
    val versionId: String,        // e.g. "v01", "v02", "v03"
    val specimenId: String,       // e.g. "SPEC-7729-V02"
    val timestamp: String,
    val sourceOrigin: IngressSourceOrigin = IngressSourceOrigin.IMPORTED_CORPUS,
    val sha256Hash: String = "sha256:8f4b23c91d7e2a6b4c50193e7f8a61b2c4e5d6f7a8b9c0d1e2f3a4b5c6d7e8f9",
    val lyricText: String,
    val wordCount: Int,
    val sectionCount: Int,
    val stanzaCount: Int = 2,
    val objectCount: Int,
    val structuralObservations: String = "Standard verse-chorus cadence with intact narrative flow.",
    val evidence: LyricEvidence,
    val audioWitness: AudioWitnessMetrics? = null,
    val gates: List<GateDiagnostic> = emptyList(),
    val decision: SpecimenDecision = SpecimenDecision.NOT_YET_EXAMINED,
    val decisionReason: String = "",
    val decisionEvidence: List<String> = emptyList(),
    val canHeal: Boolean = false,
    val historyTrail: List<SpecimenHistoryEntry> = emptyList(),
    // Governance Spec v1 fields
    val g1Witness: G1WitnessResult? = null,
    val g2Diagnostic: G2DiagnosticResult? = null,
    val g3Performance: G3PerformanceResult? = null,
    val g4Acoustic: G4AcousticObservation? = null,
    val g5Disposition: G5Disposition = G5Disposition(),
    val g6Status: G6Status = G6Status.NOT_SPECIFIED_DEFERRED,
    val governorAuthorization: HumanGovernorAuthorization? = null,
    val releaseManifest: MasterReleaseManifest? = null,
    val humanEarReview: HumanEarReview? = null
)

enum class HumanEarDisposition(val label: String, val badgeColor: Long, val description: String) {
    PENDING_REVIEW("PENDING REVIEW", 0xFF9E9E9E, "Specimen awaiting physical auditory review by Human Curator."),
    KEEP("KEEP", 0xFF00E676, "Auditory witness approved: Natural wording, correct pronunciation, intact phrasing."),
    CURE("CURE", 0xFFFFB300, "Auditory defect detected: Awkward wording, foreign injection, or phrasing anomalies."),
    REJECT("REJECT", 0xFFFF5252, "Unacceptable musical/linguistic delivery. Permanent reject; immutable witness preserved."),
    FREEZE("FREEZE", 0xFF00E5FF, "Specimen human-verified and locked into frozen immutable witness archive.")
}

data class HumanEarReview(
    val isListened: Boolean = false,
    val listeningDurationSeconds: Int = 0,
    val hasAwkwardWording: Boolean = false,
    val hasForeignLanguageInjection: Boolean = false,
    val hasPronunciationAnomalies: Boolean = false,
    val hasLyricAudioMismatch: Boolean = false,
    val hasUnnaturalSungPhrasing: Boolean = false,
    val hasPerformanceAnomaly: Boolean = false,
    val curatorNotes: String = "",
    val disposition: HumanEarDisposition = HumanEarDisposition.PENDING_REVIEW,
    val reviewerTimestamp: Long = 0L,
    val physicalAudioPlayedHash: String? = null,
    val isHumanWitnessWitnessed: Boolean = false
)

data class BaseComposition(
    val id: String,
    val title: String,
    val era: String,
    val authorOrSource: String,
    val versions: List<SpecimenVersion>,
    val selectedVersionId: String = versions.firstOrNull()?.versionId ?: "v01"
)

typealias SpecimenVersionNode = SpecimenVersion
