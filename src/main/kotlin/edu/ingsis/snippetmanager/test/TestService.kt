package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.snippet.SnippetRepository
import edu.ingsis.snippetmanager.test.dto.CreateTestDTO
import edu.ingsis.snippetmanager.test.dto.UpdateTestDTO
import org.springframework.http.HttpStatus

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TestService(
    private val testRepository: TestRepository,
    private val snippetRepository: SnippetRepository
) {

    fun getTestById(id: Long): Test {
        return testRepository.findById(id)
            .map { it.apply { environmentVariables } }
            .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found with id $id") }
    }



    @Transactional
    fun createTest(dto: CreateTestDTO): Test {
        val snippet = snippetRepository.findById(dto.snippetId)
            .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found") }

        val test = Test(
            snippet = snippet,
            expectedOutput = dto.expectedOutput,
            userInput = dto.userInput,
            environmentVariables = dto.environmentVariables
        )

        return testRepository.save(test)
    }

    @Transactional
    fun updateTest(id: Long, dto: UpdateTestDTO): Test {
        val test = testRepository.findById(id)
            .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found") }

        val updatedTest = test.copy(
            expectedOutput = dto.expectedOutput,
            userInput = dto.userInput,
            environmentVariables = dto.environmentVariables
        )

        return testRepository.save(updatedTest)
    }

    @Transactional
    fun deleteTest(id: Long) {
        if (!testRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found")
        }
        testRepository.deleteById(id)
    }
}

