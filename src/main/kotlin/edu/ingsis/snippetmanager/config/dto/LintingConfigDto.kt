package edu.ingsis.snippetmanager.config.dto

data class LintingConfigDto (
    var camelCase: Boolean,
    var expressionAllowedInPrint: Boolean,
    var expressionAllowedInReadInput: Boolean,
)