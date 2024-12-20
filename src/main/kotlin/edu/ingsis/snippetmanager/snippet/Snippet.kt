package edu.ingsis.snippetmanager.snippet

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Snippet(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null,
    val name: String,
    val description: String,
    val language: String,
    val compliance: Compliance,
    val extension: String,
)
