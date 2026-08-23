package com.example.model

data class CorpusItem(
    val id: String,
    val title: String,
    val authorOrSource: String,
    val era: String, // Ancient, Victorian, Cyberpunk, Neo-Classical, Modernist
    val excerpt: String,
    val fullText: String,
    val motifs: List<String>,
    val tags: List<String>,
    val lexicalDensity: Float, // 0.0 - 1.0
    val tokenCount: Int,
    val g1LexicalScore: Float,
    val g2HarmonyScore: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConcordanceEntry(
    val word: String,
    val count: Int,
    val occurrencesInCorpus: Int,
    val frequencyPercentage: Float
)
