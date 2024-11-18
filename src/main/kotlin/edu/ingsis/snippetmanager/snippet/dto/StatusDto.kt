package edu.ingsis.snippetmanager.snippet.dto

import edu.ingsis.snippetmanager.snippet.Compliance

data class StatusDto(val snippetId: Long, val compliance: Compliance)
