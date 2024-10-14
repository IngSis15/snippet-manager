package edu.ingsis.snippetmanager.external.printscript

import edu.ingsis.snippetmanager.external.printscript.dto.ValidateDTO
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import reactor.core.publisher.Mono

interface PrintScriptApi {
    fun validate(content: ValidateDTO): Mono<ValidateResultDTO>
}
