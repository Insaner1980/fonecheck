package com.insaner.fonecheck.domain.model

sealed interface TestStatus {
    data object Pass : TestStatus

    data class Fail(
        val reason: String? = null,
    ) : TestStatus

    data class Warning(
        val reason: String? = null,
    ) : TestStatus

    data class Info(
        val message: String,
    ) : TestStatus

    data object NotAvailable : TestStatus

    data object NotTested : TestStatus
}
