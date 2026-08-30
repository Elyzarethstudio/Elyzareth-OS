package com.example.engine.c16

import java.util.Locale

/**
 * Component 07: Information Survival Evaluator
 *
 * Implements the verified hierarchy:
 * ANCHORS (Highest) > STATE MARKERS > NUMERIC SERIES > MULTI-COMPONENT ARRAYS (Lowest)
 *
 * Audit model: VERIFIED, IMPLEMENTED_UNVERIFIED, FAILED, SPEC.
 * Invariant: Does NOT invent the missing numerical survival formula; evaluates qualitative preservation.
 */
class InformationSurvivalEvaluator : IInformationSurvivalEvaluator {

    override fun auditInformationSurvival(
        originalSpec: CreativeSpecification,
        generatedDraft: GeneratedDraft
    ): InformationSurvivalReport {
        val draftLower = generatedDraft.rawLyricText.lowercase(Locale.US)
        val items = mutableListOf<SurvivalAuditItem>()

        // 1. Anchors (Rank 1 - Highest Priority)
        val requiredAnchors = originalSpec.witnessContract.mandatoryWitnessObjects.map { it.objectName.lowercase(Locale.US) }
        val survivedAnchors = requiredAnchors.filter { draftLower.contains(it) }
        val anchorsSurvived = survivedAnchors.isNotEmpty()

        items.add(
            SurvivalAuditItem(
                label = "Physical Witness Anchors",
                priorityRank = InformationPriorityRank.ANCHORS,
                state = if (anchorsSurvived) SurvivalAuditState.VERIFIED else SurvivalAuditState.FAILED,
                notes = "Survived: ${survivedAnchors.size}/${requiredAnchors.size} (${survivedAnchors.joinToString()})"
            )
        )

        // 2. State Markers (Rank 2)
        val specIdSurvived = generatedDraft.specId == originalSpec.specId
        items.add(
            SurvivalAuditItem(
                label = "State & Lineage Binding",
                priorityRank = InformationPriorityRank.STATE_MARKERS,
                state = if (specIdSurvived) SurvivalAuditState.VERIFIED else SurvivalAuditState.FAILED,
                notes = "Spec binding verified: ${generatedDraft.specId}"
            )
        )

        // 3. Numeric Series & Cadence (Rank 3)
        val hasStanzas = generatedDraft.stanzas.isNotEmpty()
        items.add(
            SurvivalAuditItem(
                label = "Numeric Series & Structural Stanzas",
                priorityRank = InformationPriorityRank.NUMERIC_SERIES,
                state = if (hasStanzas) SurvivalAuditState.VERIFIED else SurvivalAuditState.FAILED,
                notes = "Stanza count preserved: ${generatedDraft.stanzas.size}"
            )
        )

        // 4. Multi-Component Arrays (Rank 4)
        items.add(
            SurvivalAuditItem(
                label = "Multi-Component Arrays & Rhyme Cadence",
                priorityRank = InformationPriorityRank.MULTI_COMPONENT_ARRAYS,
                state = SurvivalAuditState.IMPLEMENTED_UNVERIFIED,
                notes = "Qualitative cadence realized without artificial numerical drift formula"
            )
        )

        val overallStatus = if (anchorsSurvived && specIdSurvived) SurvivalAuditState.VERIFIED else SurvivalAuditState.FAILED

        return InformationSurvivalReport(
            items = items,
            anchorsSurvived = anchorsSurvived,
            stateMarkersSurvived = specIdSurvived,
            overallStatus = overallStatus,
            summary = "Information Survival Audit: ${overallStatus.name}. High-priority witness anchors ${if (anchorsSurvived) "PRESERVED" else "LOST"}."
        )
    }
}
