package com.universalwallet.loyalty.core.migration

import javax.inject.Inject

/**
 * Pure validator for migration chains. Confirms that a set of (from → to) steps
 * forms a gap-free, non-overlapping path from version 1 up to [targetVersion],
 * with each step advancing by exactly one. Unit-testable with plain integers.
 */
class MigrationValidator @Inject constructor() {

    fun validate(steps: List<Pair<Int, Int>>, targetVersion: Int): MigrationValidationReport {
        val issues = mutableListOf<String>()
        if (targetVersion < 1) issues.add("Target version must be >= 1")

        val sorted = steps.sortedBy { it.first }
        sorted.forEach { (from, to) ->
            if (to != from + 1) issues.add("Step $from→$to must advance by exactly 1")
        }
        // Continuity from 1..targetVersion
        var cursor = 1
        while (cursor < targetVersion) {
            val step = sorted.firstOrNull { it.first == cursor }
            if (step == null) {
                issues.add("Missing migration from version $cursor")
                break
            }
            cursor = step.second
        }
        if (cursor != targetVersion && targetVersion >= 1 && steps.isNotEmpty()) {
            if (issues.none { it.startsWith("Missing") }) {
                issues.add("Chain ends at $cursor but target is $targetVersion")
            }
        }
        return MigrationValidationReport(isValid = issues.isEmpty(), issues = issues)
    }
}
