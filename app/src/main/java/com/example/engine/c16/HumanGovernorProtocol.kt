package com.example.engine.c16

import com.example.model.GovernanceDispositionChoice
import com.example.model.HumanGovernorAuthorization

/**
 * Component 12: 3.2.1.0 + Human Governor Protocol Lifecycle
 *
 * Preserves the invariant:
 * 3. LISTEN -> 2. EVALUATE -> 1. DECIDE -> 0. FREEZE
 *
 * Invariant: AI/Engine may analyze, propose and recommend; Human Governor retains final authority.
 * No automated AI mutation may silently promote itself to locked state.
 */
class HumanGovernorProtocol : IHumanGovernorProtocol {

    override fun evaluateProtocolStep(
        currentStep: GovernorProtocolStep,
        authorization: HumanGovernorAuthorization?
    ): GovernorProtocolState {
        val isAuthorized = authorization != null && authorization.isExplicitlyHumanAuthorized && !authorization.isAutomatedAI
        val disposition = authorization?.dispositionChoice ?: GovernanceDispositionChoice.PENDING_HUMAN_GOVERNOR

        val message = when (currentStep) {
            GovernorProtocolStep.STEP_3_LISTEN -> "Protocol Step 3: LISTEN — Ingress and acoustic/textual playback active."
            GovernorProtocolStep.STEP_2_EVALUATE -> "Protocol Step 2: EVALUATE — G1-G5 telemetry and forensic audit active."
            GovernorProtocolStep.STEP_1_DECIDE -> "Protocol Step 1: DECIDE — Curator evaluating recommendation. Awaiting governor decision."
            GovernorProtocolStep.STEP_0_FREEZE -> {
                if (isAuthorized) {
                    "Protocol Step 0: FREEZE — Human Governor (${authorization.governorIdentity}) authorized disposition: ${disposition.label}."
                } else {
                    "Protocol Step 0: BLOCKED — Human Governor authorization required before freeze. Automated AI authorization forbidden."
                }
            }
        }

        return GovernorProtocolState(
            currentStep = currentStep,
            isHumanGovernorAuthorized = isAuthorized,
            currentDisposition = disposition,
            statusMessage = message
        )
    }
}
