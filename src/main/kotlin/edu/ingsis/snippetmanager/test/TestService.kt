package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.snippet.SnippetRepository
import edu.ingsis.snippetmanager.test.dto.CreateTestDTO
import edu.ingsis.snippetmanager.test.dto.UpdateTestDTO
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TestService(
    private val testRepository: TestRepository,
    private val snippetRepository: SnippetRepository,
) {
    private val logger = LoggerFactory.getLogger(TestService::class.java)

    fun getTestById(id: Long): Test {
        logger.info("Fetching test with ID: {}", id)
        return testRepository.findById(id)
            .orElseThrow {
                logger.error("Test with ID {} not found", id)
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found with id $id")
            }
    }

    @Transactional
    fun createTest(dto: CreateTestDTO): Test {
        logger.info("Creating test for snippet ID: {}", dto.snippetId)
        val snippet =
            snippetRepository.findSnippetById(dto.snippetId)
                ?: run {
                    logger.error("Snippet with ID {} not found", dto.snippetId)
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")
                }

        val test =
            Test(
                snippet = snippet,
                expectedOutput = dto.expectedOutput,
                userInput = dto.userInput,
                name = dto.name,
            )
        logger.debug("Test to be saved: {}", test)
        return testRepository.save(test).also {
            logger.info("Test created successfully with ID: {}", it.id)
        }
    }

    @Transactional
    fun updateTest(
        id: Long,
        dto: UpdateTestDTO,
    ): Test {
        logger.info("Updating test with ID: {}", id)
        val test =
            testRepository.findById(id)
                .orElseThrow {
                    logger.error("Test with ID {} not found", id)
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found")
                }

        val updatedTest =
            test.copy(
                expectedOutput = dto.expectedOutput,
                userInput = dto.userInput,
            )
        logger.debug("Updated test: {}", updatedTest)
        return testRepository.save(updatedTest).also {
            logger.info("Test with ID {} updated successfully", id)
        }
    }

    @Transactional
    fun deleteTest(id: Long) {
        logger.info("Deleting test with ID: {}", id)
        if (!testRepository.existsById(id)) {
            logger.error("Test with ID {} not found", id)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found")
        }
        testRepository.deleteById(id)
        logger.info("Test with ID {} deleted successfully", id)
    }

    fun getTestsBySnippetId(snippetId: Long): List<Test>? {
        logger.info("Fetching tests for snippet ID: {}", snippetId)
        return testRepository.findAllBySnippetId(snippetId).also {
            logger.info("Found {} tests for snippet ID {}", it?.size ?: 0, snippetId)
        }
    }
}
