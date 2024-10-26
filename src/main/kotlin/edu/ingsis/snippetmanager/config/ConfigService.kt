package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.dto.FormattingConfigDto
import edu.ingsis.snippetmanager.config.dto.LintingConfigDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ConfigService
    @Autowired
    constructor(
        private val lintingConfigRepository: LintingConfigRepository,
        private val formattingConfigRepository: FormattingConfigRepository
    )  {
        fun getLintingConfig(userId: String): LintingConfigDto {
            if (lintingConfigRepository.findConfigByUserId(userId) == null) {
                val config = LintingConfig(userId = userId, camelCase = true, expressionAllowedInPrint = true, expressionAllowedInReadInput = true)
                lintingConfigRepository.save(config)
                return lintingConfigToDto(config)
            }
            return lintingConfigToDto(lintingConfigRepository.findConfigByUserId(userId)!!)
        }

        fun getFormattingConfig(userId: String): FormattingConfigDto {
            if (formattingConfigRepository.findConfigByUserId(userId) == null) {
                val config = FormattingConfig(userId = userId, spaceBeforeColon = false, spaceAfterColon = false, spaceAroundAssignment = true, newLinesBeforePrintln = 0, indentSpaces = 4)
                formattingConfigRepository.save(config)
                return formattingConfigToDto(config)
            }
            return formattingConfigToDto(formattingConfigRepository.findConfigByUserId(userId)!!)
        }

        fun setLintingConfig(userId: String, config: LintingConfigDto): LintingConfigDto {
            val entity = lintingConfigRepository.findConfigByUserId(userId)!!
            entity.camelCase = config.camelCase
            entity.expressionAllowedInPrint = config.expressionAllowedInPrint
            entity.expressionAllowedInReadInput = config.expressionAllowedInReadInput
            lintingConfigRepository.save(entity)
            return lintingConfigToDto(entity)
        }

        fun setFormattingConfig(userId: String, config: FormattingConfigDto): FormattingConfigDto {
            val entity = formattingConfigRepository.findConfigByUserId(userId)!!
            entity.spaceBeforeColon = config.spaceBeforeColon
            entity.spaceAfterColon = config.spaceAfterColon
            entity.spaceAroundAssignment = config.spaceAroundAssignment
            entity.newLinesBeforePrintln = config.newLinesBeforePrintln
            entity.indentSpaces = config.indentSpaces
            formattingConfigRepository.save(entity)
            return formattingConfigToDto(entity)
        }

    fun lintingConfigToDto(config: LintingConfig): LintingConfigDto {
        return LintingConfigDto(
            camelCase = config.camelCase,
            expressionAllowedInPrint = config.expressionAllowedInPrint,
            expressionAllowedInReadInput = config.expressionAllowedInReadInput
        )
    }

    fun formattingConfigToDto(config: FormattingConfig): FormattingConfigDto {
        return FormattingConfigDto(
            spaceBeforeColon = config.spaceBeforeColon,
            spaceAfterColon = config.spaceAfterColon,
            spaceAroundAssignment = config.spaceAroundAssignment,
            newLinesBeforePrintln = config.newLinesBeforePrintln,
            indentSpaces = config.indentSpaces
        )
    }
}