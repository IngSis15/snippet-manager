package edu.ingsis.snippetmanager.config.dto

data class FormattingConfigDto(
    var spaceBeforeColon: Boolean,
    var spaceAfterColon: Boolean,
    var spaceAroundAssignment: Boolean,
    var newLinesBeforePrintln: Int,
    var indentSpaces: Int,
)
