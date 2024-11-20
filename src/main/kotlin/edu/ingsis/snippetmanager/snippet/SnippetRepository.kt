package edu.ingsis.snippetmanager.snippet

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository : PagingAndSortingRepository<Snippet, Long> {
    fun findSnippetById(id: Long): Snippet?

    fun deleteById(id: Long)

    fun save(snippet: Snippet): Snippet

    fun deleteAll()
}
