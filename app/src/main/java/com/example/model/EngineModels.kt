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
