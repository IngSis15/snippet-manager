package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.test.dto.CreateTestDTO
import edu.ingsis.snippetmanager.test.dto.TestResponse
import edu.ingsis.snippetmanager.test.dto.UpdateTestDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tests")
class TestController(
    private val testService: TestService
) {

    @GetMapping("/{id}")
    fun getTest(@PathVariable id: Long): ResponseEntity<TestResponse> {
        val test = testService.getTestById(id)
        val response = TestResponse(
            id = test.id,
            expectedOutput = test.expectedOutput,
            userInput = test.userInput,
            environmentVariables = test.environmentVariables
        )
        return ResponseEntity.ok(response)
    }




    @PostMapping
    fun createTest(@RequestBody dto: CreateTestDTO): ResponseEntity<Test> {
        val test = testService.createTest(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(test)
    }

    @PutMapping("/{id}")
    fun updateTest(@PathVariable id: Long, @RequestBody dto: UpdateTestDTO): ResponseEntity<Test> {
        val test = testService.updateTest(id, dto)
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/{id}")
    fun deleteTest(@PathVariable id: Long): ResponseEntity<Void> {
        testService.deleteTest(id)
        return ResponseEntity.noContent().build()
    }
}
