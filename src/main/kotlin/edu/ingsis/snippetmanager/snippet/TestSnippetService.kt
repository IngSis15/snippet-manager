package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.snippet.dto.TestResponseDto
import edu.ingsis.snippetmanager.test.TestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class TestSnippetService
    @Autowired
    constructor(
        private val testService: TestService,
        private val permissionService: PermissionService,
        private val printScriptService: PrintScriptApi,
    ) {
        fun runTest(
            testId: Long,
            jwt: Jwt,
        ): TestResponseDto {
            val snippetId = testService.getTestById(testId).snippet.id!!
            val canRead = permissionService.canRead(jwt, snippetId).block()
            if (!canRead!!) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
            }
            val test = testService.getTestById(testId)
            val actualOutput = printScriptService.execute(snippetId, test.userInput).map { it.result }
            val passed = actualOutput.block() == test.expectedOutput
            return TestResponseDto(
                passed,
                test.expectedOutput,
                actualOutput.block()!!,
            )
        }
    }
