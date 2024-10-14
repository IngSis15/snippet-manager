package edu.ingsis.snippetmanager

import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateDTO
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import reactor.core.publisher.Mono

class PrintScriptApiMock : PrintScriptApi {
    override fun validate(content: ValidateDTO): Mono<ValidateResultDTO> {
        return Mono.just(ValidateResultDTO(true, emptyList()))
    }
}
