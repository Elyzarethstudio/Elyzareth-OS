package com.example.model

enum class NodeType {
    CORPUS_SOURCE,
    THEMATIC_FILTER,
    ELYZARETH_ENGINE_TRANSFORM,
    LYRIC_SYNTHESIZER,
    G1_LEXICAL_GUARD,
    G2_HARMONY_CHECK,
    G3_AXIOMATIC_SEAL,
    MASTER_OUTPUT_BUNDLE
}

enum class PipelineRunStatus {
    IDLE,
    EXECUTING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class PipelineNode(
    val id: String,
    val type: NodeType,
    val label: String,
    val description: String,
    val isEnabled: Boolean = true,
    val progress: Float = 0f,
    val status: VerificationState = VerificationState.PENDING,
    val outputMetric: String = "--"
)

data class IntegratedTrack(
    val trackName: String,
    val sourceTenant: String,
    val blocks: List<String>,
    val colorHex: Long
)

data class MasterIntegratedBundle(
    val id: String,
    val name: String,
    val timestamp: Long,
    val corpusSourceTitle: String,
    val lyricTitle: String,
    val g1Rating: String,
    val g2Coherence: String,
    val g3Hash: String,
    val synthesizedSummary: String,
    val fullMasterText: String
)
