package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.models.LintingConfig
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LintingConfigRepository : CrudRepository<LintingConfig, Long> {
    fun findConfigByUserId(id: String): LintingConfig?
}
