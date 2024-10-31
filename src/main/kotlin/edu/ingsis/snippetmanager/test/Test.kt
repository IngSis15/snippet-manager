package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.snippet.Snippet
import jakarta.persistence.*

@Entity
data class Test(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: Snippet,
    @Column(nullable = false)
    val expectedOutput: String,
    @Column(nullable = false)
    val userInput: String,
    @ElementCollection
    val environmentVariables: Map<String, String> = emptyMap(),
)
