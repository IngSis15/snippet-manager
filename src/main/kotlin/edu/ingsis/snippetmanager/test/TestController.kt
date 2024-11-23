package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.test.dto.CreateTestDTO
import edu.ingsis.snippetmanager.test.dto.TestResponse
import edu.ingsis.snippetmanager.test.dto.UpdateTestDTO
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/tests")
class TestController(
    private val testService: TestService,
) {
    private val logger = LoggerFactory.getLogger(TestController::class.java)

    @GetMapping("/{id}")
    fun getTest(
        @PathVariable id: Long,
    ): ResponseEntity<TestResponse> {
        val test = testService.getTestById(id)
        val response =
            TestResponse(
                id = test.id,
                expectedOutput = test.expectedOutput,
                userInput = test.userInput,
                testName = test.name,
            )
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createTest(
        @RequestBody dto: CreateTestDTO,
    ): ResponseEntity<Test> {
        val test = testService.createTest(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(test)
    }

    @PutMapping("/{id}")
    fun updateTest(
        @PathVariable id: Long,
        @RequestBody dto: UpdateTestDTO,
    ): ResponseEntity<Test> {
        val test = testService.updateTest(id, dto)
        logger.info("Test with ID {} updated successfully", id)
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/{id}")
    fun deleteTest(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        testService.deleteTest(id)
        logger.info("Test with ID {} deleted successfully", id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/snippet/{snippetId}")
    fun getTestsBySnippetId(
        @PathVariable snippetId: Long,
    ): ResponseEntity<List<TestResponse>> {
        val tests = testService.getTestsBySnippetId(snippetId)
        val response =
            tests?.map {
                TestResponse(
                    id = it.id,
                    expectedOutput = it.expectedOutput,
                    userInput = it.userInput,
                    testName = it.name,
                )
            }
        logger.info("Returning {} tests for snippet ID {}", response?.size ?: 0, snippetId)
        return ResponseEntity.ok(response)
    }
}
