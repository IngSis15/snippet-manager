package edu.ingsis.snippetmanager.external.printscript

import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.snippet.dto.ExecuteResultDto
import reactor.core.publisher.Mono

interface PrintScriptApi {
    fun validate(content: String): Mono<ValidateResultDTO>

    fun execute(
        snippetId: Long,
        input: List<String>,
    ): Mono<ExecuteResultDto>
}
