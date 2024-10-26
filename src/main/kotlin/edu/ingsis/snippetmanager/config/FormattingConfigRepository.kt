package edu.ingsis.snippetmanager.config

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FormattingConfigRepository: CrudRepository<FormattingConfig, Long> {
    fun findConfigByUserId(id: String): FormattingConfig?
}