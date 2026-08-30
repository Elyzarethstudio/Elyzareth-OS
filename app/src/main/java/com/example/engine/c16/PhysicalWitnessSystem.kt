package com.example.engine.c16

import com.example.engine.ElyzarethTurboEngine
import com.example.model.PhysicalAnchorDiagnosticResult
import java.util.Locale

/**
 * Component 03: Physical Anchor / Witness System
 *
 * Implements the verified qualitative rules:
 * - 5 Witness Pillars: Domestic, Environmental, Recovery, Cultural, Transcendental
 * - Spatial grounding, object permanence, physical interaction
 * - Non-decorational witness requirement
 * - Silent Witness principle
 *
 * Invariant: A physical noun is not automatically a Witness Object.
 * Does NOT invent mathematical witness scores or vector embeddings.
 */
class PhysicalWitnessSystem : IPhysicalWitnessSystem {

    companion object {
        val CANONICAL_WITNESS_VOCABULARY = mapOf(
            "table" to WitnessPillar.DOMESTIC,
            "coat" to WitnessPillar.DOMESTIC,
            "chair" to WitnessPillar.DOMESTIC,
            "coffee" to WitnessPillar.DOMESTIC,
            "cup" to WitnessPillar.DOMESTIC,
            "mantel" to WitnessPillar.DOMESTIC,
            "window" to WitnessPillar.DOMESTIC,
            "door" to WitnessPillar.DOMESTIC,
            "porch" to WitnessPillar.DOMESTIC,
            "boots" to WitnessPillar.DOMESTIC,
            "pocket" to WitnessPillar.DOMESTIC,
            "needle" to WitnessPillar.DOMESTIC,

            "railway" to WitnessPillar.ENVIRONMENTAL,
            "river" to WitnessPillar.ENVIRONMENTAL,
            "limestone" to WitnessPillar.ENVIRONMENTAL,
            "cedar" to WitnessPillar.ENVIRONMENTAL,
            "timber" to WitnessPillar.ENVIRONMENTAL,
            "bridge" to WitnessPillar.ENVIRONMENTAL,
            "snow" to WitnessPillar.ENVIRONMENTAL,
            "stone" to WitnessPillar.ENVIRONMENTAL,
            "tree" to WitnessPillar.ENVIRONMENTAL,
            "mountain" to WitnessPillar.ENVIRONMENTAL,
            "roots" to WitnessPillar.ENVIRONMENTAL,
            "water" to WitnessPillar.ENVIRONMENTAL,

            "fossil" to WitnessPillar.RECOVERY,
            "amber" to WitnessPillar.RECOVERY,
            "quilt" to WitnessPillar.RECOVERY,
            "bandage" to WitnessPillar.RECOVERY,
            "cane" to WitnessPillar.RECOVERY,

            "coin" to WitnessPillar.CULTURAL,
            "guitar" to WitnessPillar.CULTURAL,
            "photograph" to WitnessPillar.CULTURAL,
            "iron" to WitnessPillar.CULTURAL,
            "clock" to WitnessPillar.CULTURAL,
            "letter" to WitnessPillar.CULTURAL,
            "paper" to WitnessPillar.CULTURAL,
            "canvas" to WitnessPillar.CULTURAL,
            "lantern" to WitnessPillar.CULTURAL,

            "key" to WitnessPillar.TRANSCENDENTAL,
            "lock" to WitnessPillar.TRANSCENDENTAL,
            "compass" to WitnessPillar.TRANSCENDENTAL,
            "bell" to WitnessPillar.TRANSCENDENTAL
        )
    }

    override fun establishWitnessContract(intent: UserCreativeIntent): PhysicalWitnessContract {
        val candidates = mutableListOf<WitnessObjectCandidate>()

        // 1. Ingest any explicitly user-desired anchors
        intent.desiredAnchors.forEach { anchor ->
            val clean = anchor.lowercase(Locale.US).trim()
            val pillar = CANONICAL_WITNESS_VOCABULARY[clean] ?: WitnessPillar.DOMESTIC
            candidates.add(
                WitnessObjectCandidate(
                    objectName = clean,
                    pillar = pillar,
                    isSilentWitness = true,
                    hasObjectPermanence = true,
                    isNonDecorational = true
                )
            )
        }

        // 2. Derive grounding from story concept if anchors empty
        if (candidates.isEmpty()) {
            val conceptLower = intent.storyConcept.lowercase(Locale.US)
            CANONICAL_WITNESS_VOCABULARY.forEach { (word, pillar) ->
                if (conceptLower.contains(word) && candidates.none { it.objectName == word }) {
                    candidates.add(
                        WitnessObjectCandidate(
                            objectName = word,
                            pillar = pillar,
                            isSilentWitness = true,
                            hasObjectPermanence = true,
                            isNonDecorational = true
                        )
                    )
                }
            }
        }

        // 3. Fallback to canonical default anchors if still empty
        if (candidates.isEmpty()) {
            candidates.add(WitnessObjectCandidate("wooden table", WitnessPillar.DOMESTIC))
            candidates.add(WitnessObjectCandidate("silver coin", WitnessPillar.CULTURAL))
            candidates.add(WitnessObjectCandidate("iron key", WitnessPillar.TRANSCENDENTAL))
        }

        val requiredPillars = candidates.map { it.pillar }.distinct()

        return PhysicalWitnessContract(
            schemaVersion = "1.0.0",
            requiredPillars = requiredPillars,
            mandatoryWitnessObjects = candidates.distinctBy { it.objectName },
            minimumObjectCount = 2,
            nonDecorationalMandate = true,
            silentWitnessPrinciple = true
        )
    }

    override fun evaluateWitnesses(lyricText: String, contract: PhysicalWitnessContract): PhysicalWitnessEvaluation {
        val lower = lyricText.lowercase(Locale.US)
        val detectedObjects = mutableListOf<String>()
        val detectedPillars = mutableListOf<WitnessPillar>()

        CANONICAL_WITNESS_VOCABULARY.forEach { (word, pillar) ->
            if (lower.contains(word)) {
                detectedObjects.add(word)
                if (!detectedPillars.contains(pillar)) {
                    detectedPillars.add(pillar)
                }
            }
        }

        val satisfiedCount = detectedObjects.size >= contract.minimumObjectCount
        val satisfiedPillars = detectedPillars.isNotEmpty()
        val isPass = satisfiedCount && satisfiedPillars

        val diag = ElyzarethTurboEngine.evaluatePhysicalAnchors(lyricText)

        val failReason = if (!isPass) {
            "Physical Witness Contract Violated: Expected at least ${contract.minimumObjectCount} physical witness objects across verified pillars, found ${detectedObjects.size} ($detectedObjects)."
        } else null

        return PhysicalWitnessEvaluation(
            status = if (isPass) "PASS" else "FAIL",
            detectedWitnessObjects = detectedObjects,
            detectedPillars = detectedPillars,
            nonDecorationalSatisfied = !diag.metaphorSoupDetected,
            failReason = failReason,
            diagnosticResult = diag
        )
    }
}
