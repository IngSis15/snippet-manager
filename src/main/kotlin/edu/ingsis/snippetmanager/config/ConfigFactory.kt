package edu.ingsis.snippetmanager.config

object ConfigFactory {
    fun defaultLintingRules(): String {
        return """
            {
              "identifier_format": "camel case", 
              "mandatory-variable-or-literal-in-println": true,
              "mandatory-variable-or-literal-in-readInput": true
            }
            """
    }

    fun defaultFormattingRules(): String {
        return """
            {
              "enforce-spacing-before-colon-in-declaration": false,
              "enforce-spacing-after-colon-in-declaration": false,
              "enforce-no-spacing-around-equals": true,
              "newLinesBeforePrintln": 0,
              "indent-inside-if": 4
            }
            """
    }
}
