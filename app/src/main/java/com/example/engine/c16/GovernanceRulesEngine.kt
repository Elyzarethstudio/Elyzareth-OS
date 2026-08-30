package com.example.engine.c16

import com.example.engine.ElyzarethGovernanceEngine
import com.example.model.*
import java.security.MessageDigest

/**
 * Component 10: G1 / G2 / G3 Governance Rules Engine
 *
 * Preserves the authoritative, locked G1-G3 governance rules and SHA-256 seal hashing.
 * Integrates directly with ElyzarethGovernanceEngine without weakening any gate checks.
 */
class GovernanceRulesEngine : IGovernanceRulesEngine {

    companion object {
        fun sha256(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02x".format(it) }
        }
    }

    override fun evaluateG1Witness(draft: GeneratedDraft, spec: CreativeSpecification): G1WitnessResult {
        val evidence = LyricEvidence(
            theme = spec.creativeDna.title,
            narrativeArc = spec.creativeDna.narrativeIntent,
            emotionalProfile = spec.creativeDna.emotionalIdentity,
            witnessObjects = spec.witnessContract.mandatoryWitnessObjects.map { it.objectName },
            temporalContext = "Present / Historical Grounding",
            energyProfile = "Restrained 63-65 BPM",
            languageCharacteristics = "Concrete Physical Grounded",
            creativeSignals = listOf("Physical Witness", "Negative Space"),
            suggestedSonicVocabulary = spec.creativeDna.acousticAffinities
        )
        return ElyzarethGovernanceEngine.evaluateG1Witness(
            rawLyric = draft.rawLyricText,
            title = spec.creativeDna.title,
            evidence = evidence,
            audioMetrics = null
        )
    }

    override fun evaluateG2Diagnostic(draft: GeneratedDraft, spec: CreativeSpecification): G2DiagnosticResult {
        val evidence = LyricEvidence(
            theme = spec.creativeDna.title,
            narrativeArc = spec.creativeDna.narrativeIntent,
            emotionalProfile = spec.creativeDna.emotionalIdentity,
            witnessObjects = spec.witnessContract.mandatoryWitnessObjects.map { it.objectName },
            temporalContext = "Present / Historical Grounding",
            energyProfile = "Restrained 63-65 BPM",
            languageCharacteristics = "Concrete Physical Grounded",
            creativeSignals = listOf("Physical Witness", "Negative Space"),
            suggestedSonicVocabulary = spec.creativeDna.acousticAffinities
        )
        return ElyzarethGovernanceEngine.evaluateG2PhysicalAnchor(
            rawLyric = draft.rawLyricText,
            evidence = evidence
        )
    }

    override fun evaluateG3Performance(draft: GeneratedDraft): G3PerformanceResult {
        return ElyzarethGovernanceEngine.evaluateG3PerformanceCalibration(
            rawLyric = draft.rawLyricText,
            audioMetrics = null
        )
    }
}
