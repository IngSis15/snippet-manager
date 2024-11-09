package edu.ingsis.snippetmanager.config.configSchemas
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FormattingSchemaDTO(
    @SerialName("enforce-spacing-before-colon-in-declaration")
    val spaceBeforeColon: Boolean,
    @SerialName("enforce-spacing-after-colon-in-declaration")
    val spaceAfterColon: Boolean,
    @SerialName("enforce-no-spacing-around-equals")
    val noSpaceAroundAssignment: Boolean,
    @SerialName("newLinesBeforePrintln")
    val newLinesBeforePrintln: Int,
    @SerialName("indent-inside-if")
    val indentSpaces: Int,
)
