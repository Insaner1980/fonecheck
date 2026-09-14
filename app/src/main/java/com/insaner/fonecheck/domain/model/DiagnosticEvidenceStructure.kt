package com.insaner.fonecheck.domain.model

fun evidenceBelongsToCategory(
    categoryId: DiagnosticCategoryId,
    evidence: List<DiagnosticEvidence>,
): Boolean = evidence.all { it.categoryId == categoryId }

fun hasUniqueCheckIds(evidence: List<DiagnosticEvidence>): Boolean =
    evidence.map { it.checkId }.distinct().size == evidence.size
