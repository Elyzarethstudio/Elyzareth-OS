package com.example.engine.c16

import com.example.engine.ElyzarethTurboEngine
import java.util.Locale

/**
 * Component 04: Lyric Language Governor & Cliché / Contamination Rules
 *
 * Implements the verified constraints:
 * - Physical-object metaphor principle
 * - Metaphor-soup prohibition
 * - Abstract emotional declaration prohibition
 * - Decorative/production contamination prohibition
 * - Unauthorized production terminology exclusion
 * - Lyrics Corpus Curator binding (UUID: 205769ac) as reference/identity, NOT closed-vocabulary filter.
 *
 * Explicitly preserves unverified algorithms (ΔV, RA, LT, TF-IDF drift engine) as NOT_FOUND / SPEC.
 */
class LyricLanguageGovernor : ILyricLanguageGovernor {

    private val corpusReference = CorpusReferenceProfile(
        corpusUuid = "205769ac",
        corpusName = "Elyzareth 700+ Item Reference Corpus",
        bindingType = "IDENTITY_REFERENCE_ENVIRONMENT",
        isClosedVocabularyFilter = false // Crucial: Identity reference only, not closed-vocabulary starvation
    )

    override fun generateLanguageEnvelope(intent: UserCreativeIntent): LanguageConstraintEnvelope {
        return LanguageConstraintEnvelope(
            schemaVersion = "1.0.0",
            physicalObjectMetaphorMandate = true,
            metaphorSoupProhibition = true,
            abstractEmotionDeclarationProhibition = true,
            decorativeProductionContaminationProhibition = true,
            corpusReferenceBinding = corpusReference,
            deltaVScoreStatus = CapabilityStatus.NOT_FOUND_UNVERIFIED,
            rhymeAttractorScoreStatus = CapabilityStatus.NOT_FOUND_UNVERIFIED,
            lexicalTensionScoreStatus = CapabilityStatus.NOT_FOUND_UNVERIFIED,
            tfIdfDriftEngineStatus = CapabilityStatus.SPEC_UNIMPLEMENTED
        )
    }

    override fun auditLanguage(lyricText: String, envelope: LanguageConstraintEnvelope): LanguageAuditResult {
        val lower = lyricText.lowercase(Locale.US)

        // 1. Prohibited Generic Trope Detection
        val foundTropes = envelope.prohibitedGenericTropes.filter { lower.contains(it) }

        // 2. Unauthorized Production Terminology Detection
        val foundProdTerms = envelope.unauthorizedProductionTerms.filter { lower.contains(it) }

        // 3. Foreign / Injected Contamination Detection
        val contaminationAnomalies = ElyzarethTurboEngine.detectContamination(lyricText).toMutableList()
        if (foundProdTerms.isNotEmpty()) {
            contaminationAnomalies.add("Unauthorized production terms found: ${foundProdTerms.joinToString()}")
        }

        // 4. Metaphor Soup & Abstract Emotion diagnostics
        val diag = ElyzarethTurboEngine.evaluatePhysicalAnchors(lyricText)

        val isCompliant = foundTropes.isEmpty() && contaminationAnomalies.isEmpty() && !diag.metaphorSoupDetected

        val summary = buildString {
            if (isCompliant) {
                append("Language Audit: Compliant. Physical metaphor rules satisfied.")
            } else {
                append("Language Audit: Non-compliant. ")
                if (foundTropes.isNotEmpty()) append("[Tropes: ${foundTropes.size}] ")
                if (diag.metaphorSoupDetected) append("[Metaphor Soup Flagged] ")
                if (contaminationAnomalies.isNotEmpty()) append("[Contaminations: ${contaminationAnomalies.size}] ")
            }
        }

        return LanguageAuditResult(
            isCompliant = isCompliant,
            detectedTropes = foundTropes,
            metaphorSoupFlagged = diag.metaphorSoupDetected,
            unanchoredEmotionFlagged = diag.abstractEmotionUnanchoredDetected,
            contaminationAnomalies = contaminationAnomalies,
            summary = summary
        )
    }

    override fun getCorpusReferenceProfile(): CorpusReferenceProfile {
        return corpusReference
    }
}
