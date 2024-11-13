package edu.ingsis.snippetmanager.config.configSchemas
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LintingSchemaDTO(
    @SerialName("identifier_format")
    val casingFormat: String? = null,
    @SerialName("mandatory-variable-or-literal-in-println")
    val expressionAllowedInPrint: Boolean,
    @SerialName("mandatory-variable-or-literal-in-readInput")
    val expressionAllowedInReadInput: Boolean,
)
