package edu.ingsis.snippetmanager.config

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LintingConfigRepository : CrudRepository<LintingConfig, Long> {
    fun findConfigByUserId(id: String): LintingConfig?
}
