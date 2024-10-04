package edu.ingsis.snippetmanager.snippet

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository : CrudRepository<Snippet, Long> {
    fun findSnippetById(id: Long): Snippet?
}
