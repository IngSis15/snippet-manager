package edu.ingsis.snippetmanager.external

import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.snippet.dto.ExecuteResultDto
import reactor.core.publisher.Mono

class PrintScriptApiMock : PrintScriptApi {
    override fun validate(content: String): Mono<ValidateResultDTO> {
        return Mono.just(ValidateResultDTO(true, emptyList()))
    }

    override fun execute(
        snippetId: Long,
        input: List<String>,
    ): Mono<ExecuteResultDto> {
        return Mono.just(ExecuteResultDto(emptyList()))
    }
}
