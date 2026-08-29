package com.example.model

enum class ArtifactCategory {
    LYRIC_TEXT,
    STRUCTURED_SCHEMA,
    AUDIO_STREAM,
    METADATA_RECORD,
    UNKNOWN
}

enum class IngestionDiscoveryState {
    DISCOVERED,
    PARSED,
    VERIFIED,
    UNVERIFIED,
    FAILED,
    NOT_MEASURED
}

enum class IngestionScanStatus {
    IDLE,
    SCANNING,
    COMPLETED,
    FAILED
}

data class DiscoveredArtifactRecord(
    val id: String,
    val documentUri: String,
    val fileName: String,
    val relativePath: String,
    val fileExtension: String,
    val fileSizeBytes: Long,
    val lastModified: Long,
    val mimeType: String,
    val sha256Hash: String, // Deterministic SHA-256 over raw stream
    val category: ArtifactCategory,
    val discoveryState: IngestionDiscoveryState = IngestionDiscoveryState.DISCOVERED,
    val isParsedSuccessfully: Boolean = false,
    val detectedBaseTitle: String? = null,
    val detectedVersionLabel: String? = null,
    val detectedLanguage: String = "Uncertain",
    val lineCount: Int = 0,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val snippetText: String? = null,
    val parseErrorMessage: String? = null,
    val audioDurationSeconds: Int? = null,
    val audioFormat: String? = null,
    val isOrphan: Boolean = false,
    val isDuplicateCandidate: Boolean = false,
    val duplicateWithId: String? = null,
    val requiresHumanReview: Boolean = false,
    val humanReviewReason: String? = null
)

data class DiscoveredBaseTitleGroup(
    val baseId: String,
    val title: String,
    val relativeFolder: String,
    val artifacts: List<DiscoveredArtifactRecord>,
    val lyricCount: Int,
    val audioCount: Int,
    val schemaCount: Int,
    val primaryLanguage: String,
    val isCompletePackage: Boolean,
    val missingComponents: List<String>,
    val duplicateCandidates: List<String>,
    val requiresHumanReview: Boolean,
    val humanReviewReason: String?,
    val firstDiscoveredTimestamp: Long = System.currentTimeMillis()
)

data class CorpusReviewItem(
    val id: String,
    val title: String,
    val artifactFileName: String,
    val reason: String,
    val severity: ReviewSeverity,
    val recommendedAction: String
)

enum class ReviewSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class CorpusInventoryReport(
    val scanTimestamp: Long = System.currentTimeMillis(),
    val sourceRootUri: String = "",
    val sourceRootDisplayName: String = "No directory selected",
    val scanStatus: IngestionScanStatus = IngestionScanStatus.IDLE,
    val scanStatusMessage: String = "Awaiting SAF directory selection for read-only ingestion dry run.",
    val totalFilesDiscovered: Int = 0,
    val baseTitlesDiscovered: Int = 0,
    val versionsDiscovered: Int = 0,
    val successfullyParsed: Int = 0,
    val unparsedCount: Int = 0,
    val duplicateCandidatesCount: Int = 0,
    val orphanArtifactsCount: Int = 0,
    val missingExpectedComponentsCount: Int = 0,
    val languageStats: Map<String, Int> = emptyMap(),
    val evidenceStats: Map<String, Int> = emptyMap(),
    val humanReviewItems: List<CorpusReviewItem> = emptyList(),
    val baseTitleGroups: List<DiscoveredBaseTitleGroup> = emptyList(),
    val orphanArtifacts: List<DiscoveredArtifactRecord> = emptyList(),
    val duplicateArtifacts: List<DiscoveredArtifactRecord> = emptyList(),
    val allArtifacts: List<DiscoveredArtifactRecord> = emptyList()
)
