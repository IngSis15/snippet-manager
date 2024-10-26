package edu.ingsis.snippetmanager.config

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class FormattingConfig (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val userId:String,
    var spaceBeforeColon: Boolean,
    var spaceAfterColon: Boolean,
    var spaceAroundAssignment: Boolean,
    var newLinesBeforePrintln: Int,
    var indentSpaces: Int,


)