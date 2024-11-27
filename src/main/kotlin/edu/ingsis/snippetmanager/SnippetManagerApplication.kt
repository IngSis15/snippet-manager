package edu.ingsis.snippetmanager

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@SpringBootApplication
class SnippetManagerApplication {
    @PostConstruct
    fun init() {
        Hooks.enableAutomaticContextPropagation()
    }
}

fun main(args: Array<String>) {
    runApplication<SnippetManagerApplication>(*args)
}
