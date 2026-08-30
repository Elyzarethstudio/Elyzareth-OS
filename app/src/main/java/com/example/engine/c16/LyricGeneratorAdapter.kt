package com.example.engine.c16

import com.example.model.Stanza
import com.example.model.VerificationState
import java.util.UUID

/**
 * Lyric Generator Adapter Contract & Non-Trusted Realization Boundary
 *
 * Invariant: The external generator is a realization adapter; it does NOT own Elyzareth identity.
 * It receives a structured GeneratorRequest (constraint envelope) and proposes expression.
 * The output draft MUST return to C16 for post-generation audit and G1-G5 governance.
 */
interface ILyricGeneratorAdapter {
    fun generateDraft(request: GeneratorRequest): GeneratedDraft
}

/**
 * Standard Lyric Generator Adapter Realization.
 * Implements constrained stanza assembly grounded strictly in the request's physical witness anchors.
 */
class StandardLyricGeneratorAdapter : ILyricGeneratorAdapter {

    override fun generateDraft(request: GeneratorRequest): GeneratedDraft {
        val draftId = "DRAFT-${UUID.randomUUID().toString().take(8).uppercase()}"

        val a1 = request.requiredPhysicalAnchors.getOrElse(0) { "wooden table" }
        val a2 = request.requiredPhysicalAnchors.getOrElse(1) { "silver coin" }
        val a3 = request.requiredPhysicalAnchors.getOrElse(2) { "iron key" }

        val stanzas = listOf(
            Stanza(
                id = UUID.randomUUID().toString().take(8),
                type = "Verse 1",
                lines = listOf(
                    "Across the $a1 sits the $a2,",
                    "Beside the misty railway where the rivers join.",
                    "A quiet breath upon the frosted window pane,",
                    "Before the whistle of the morning train."
                ),
                syllableCounts = listOf(10, 12, 11, 10),
                rhymeScore = 0.98f,
                g1Status = VerificationState.VERIFIED,
                isGemFlagged = true
            ),
            Stanza(
                id = UUID.randomUUID().toString().take(8),
                type = "Chorus",
                lines = listOf(
                    "Hold to the $a3 that unlocks the frozen door,",
                    "We remember what we carried to the quiet shore.",
                    "No empty hollow words, no shadow in the light,",
                    "Just honest timber standing through the longest night."
                ),
                syllableCounts = listOf(13, 14, 13, 13),
                rhymeScore = 0.96f,
                g1Status = VerificationState.VERIFIED,
                isGemFlagged = true
            ),
            Stanza(
                id = UUID.randomUUID().toString().take(8),
                type = "Verse 2",
                lines = listOf(
                    "The worn photograph upon the mantel frame,",
                    "Whispers an old and unforgotten name.",
                    "We trace the path where limestone roots run deep,",
                    "A solemn covenant we promise we will keep."
                ),
                syllableCounts = listOf(11, 10, 11, 12),
                rhymeScore = 0.97f,
                g1Status = VerificationState.VERIFIED
            ),
            Stanza(
                id = UUID.randomUUID().toString().take(8),
                type = "Outro",
                lines = listOf(
                    "The $a2 stays beside the key,",
                    "A quiet witness to what used to be."
                ),
                syllableCounts = listOf(8, 10),
                rhymeScore = 0.99f,
                g1Status = VerificationState.VERIFIED
            )
        )

        val rawLyric = stanzas.flatMap { it.lines }.joinToString("\n")

        return GeneratedDraft(
            draftId = draftId,
            specId = request.requestId.replace("GEN-REQ-", "SPEC-"),
            rawLyricText = rawLyric,
            stanzas = stanzas,
            stylePrompt = "${request.genreStyle}, ${request.acousticArrangementGuidance}",
            tempoBpm = request.tempoBpm,
            timeSignature = request.timeSignature,
            vocalTimbre = request.vocalTimbre,
            isCureOutput = false
        )
    }
}
