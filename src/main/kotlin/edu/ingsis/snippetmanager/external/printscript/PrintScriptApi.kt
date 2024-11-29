package edu.ingsis.snippetmanager.external.printscript

import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.snippet.dto.ExecuteResultDto

interface PrintScriptApi {
    fun validate(content: String): ValidateResultDTO?

    fun execute(
        snippetId: Long,
        input: List<String>,
    ): ExecuteResultDto?
}
