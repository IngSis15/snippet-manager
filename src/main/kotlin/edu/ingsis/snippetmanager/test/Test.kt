package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.snippet.Snippet
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
data class Test(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: Snippet,
    @ElementCollection
    @Column(nullable = false)
    val expectedOutput: List<String>,
    @ElementCollection
    @Column(nullable = false)
    val userInput: List<String>,
)
