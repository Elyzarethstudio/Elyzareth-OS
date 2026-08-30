package com.example.model

data class EngineTelemetry(
    val engineState: String = "NOMINAL // ONLINE",
    val activeThreads: Int = 12,
    val clockMhz: Int = 4200,
    val g1LexicalLoad: Float = 0.28f,
    val g2HarmonyLoad: Float = 0.44f,
    val g3AxiomLoad: Float = 0.15f,
    val totalProcessedTokens: Long = 184520L,
    val governancePassRate: Float = 99.82f,
    val axiomaticSealCount: Int = 412,
    val cadenceRigidity: Float = 0.85f,
    val temperature: Float = 0.72f,
    val governanceTolerance: Float = 0.95f
)

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val layer: String, // G1, G2, G3, OS_KERNEL, INTEGRATOR
    val message: String,
    val hashStamp: String,
    val status: VerificationState = VerificationState.VERIFIED
)

data class GovernanceAuditResult(
    val g1Passed: Boolean,
    val g1Details: String,
    val g2Passed: Boolean,
    val g2Details: String,
    val g3Passed: Boolean,
    val g3Details: String,
    val sealHash: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ArchiveFile(
    val id: String,
    val fileName: String,
    val category: String, // LYRICS, CORPUS, PIPELINE_BUNDLE, G3_CERTIFICATE
    val originTenant: String,
    val previewText: String,
    val fullText: String,
    val g3SealHash: String,
    val sizeKb: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    val fullContent: String get() = fullText
}

// =============================================================================
// ELYZARETH OS v38.2 — PRODUCTION VALIDATION DATA CONTRACTS
// =============================================================================

data class WitnessObservation(
    val specimenId: String,
    val witnessName: String,
    val audioHash: String,
    val dynamicRangeDb: Float,
    val perceivedWarmthScore: Float,
    val notes: String
)

data class IntegratedSongSpecimen(
    val specimenId: String,
    val title: String,
    val lyricHash: String,
    val audioHash: String,
    val dnaProfile: Any?,
    val witnessRecord: WitnessObservation?,
    val isReconciled: Boolean
)

data class GovernanceComplianceVerdict(
    val receiptId: String,
    val isApproved: Boolean,
    val status: String,
    val hasFatalViolations: Boolean,
    val violations: List<String> = emptyList()
)

data class ArchiveManifest(
    val archiveId: String,
    val specimenId: String,
    val governanceReceipt: String,
    val masterHash: String,
    val timestampUtc: String,
    val isSealed: Boolean
)

data class AudioExtractionTelemetry(
    val isSuccess: Boolean,
    val sampleRate: Int,
    val errorCode: String? = null,
    val errorMessage: String? = null
)

data class HashVerificationReceipt(
    val isMatch: Boolean,
    val status: String,
    val expectedHash: String,
    val actualHash: String
)

data class WorkspaceTransaction(
    val transactionId: String,
    val step: String,
    val specimenId: String,
    val isCommitted: Boolean,
    val isRolledBack: Boolean = false,
    val rollbackReason: String? = null
)

data class DeserializedForensicState(
    val specimenId: String,
    val metrics: Map<String, String>
)
