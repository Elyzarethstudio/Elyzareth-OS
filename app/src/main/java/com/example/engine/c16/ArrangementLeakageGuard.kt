package com.example.engine.c16

import com.example.model.ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0
import com.example.model.SparseArrangementConstraints
import java.util.Locale

/**
 * Component 08: Arrangement Leakage Guard
 *
 * Preserves the existing locked ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1.0.
 * Invariant: Declared production constraints != Measured audio evidence.
 */
class ArrangementLeakageGuard : IArrangementLeakageGuard {

    private val lockedConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0

    override fun getArrangementConstraints(): SparseArrangementConstraints {
        return lockedConstraints
    }

    override fun validateLeakageDeclaration(stylePrompt: String): LeakageAuditResult {
        val lower = stylePrompt.lowercase(Locale.US)
        val flagged = mutableListOf<String>()

        lockedConstraints.percussiveSuppression.forEach { term ->
            if (lower.contains(term)) flagged.add(term)
        }
        lockedConstraints.syntheticAndOrchestralPadSuppression.forEach { term ->
            if (lower.contains(term)) flagged.add(term)
        }
        lockedConstraints.excludedInstrumentation.forEach { term ->
            if (lower.contains(term)) flagged.add(term)
        }
        lockedConstraints.prohibitedDynamics.forEach { term ->
            if (lower.contains(term)) flagged.add(term)
        }

        val isZeroLeakage = flagged.isEmpty()
        val notes = if (isZeroLeakage) {
            "Arrangement Leakage Audit: Compliant. Zero unauthorized percussive/synthetic elements in declared style envelope."
        } else {
            "Arrangement Leakage Warning: Declared style contains suppressed terms: ${flagged.joinToString()}"
        }

        return LeakageAuditResult(
            zeroLeakageCompliant = isZeroLeakage,
            flaggedTerms = flagged,
            notes = notes
        )
    }
}
