package com.insaner.fonecheck.domain.model

enum class DiagnosticCategoryId(
    val stableId: String,
) {
    DEVICE("device"),
    PERFORMANCE("performance"),
    SIM("sim"),
    DISPLAY("display"),
    AUDIO("audio"),
    CAMERA("camera"),
    SENSORS("sensors"),
    CONNECTIVITY("connectivity"),
    BATTERY("battery"),
    THERMAL("thermal"),
    STORAGE("storage"),
    VIBRATION("vibration"),
    BUTTONS("buttons"),
    BIOMETRICS("biometrics"),
}

object DiagnosticCatalog {
    val categories: List<DiagnosticCategoryId> = DiagnosticCategoryId.entries
}

typealias TestCategory = DiagnosticCategoryId
