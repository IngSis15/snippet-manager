package edu.ingsis.snippetmanager.external.asset

import reactor.core.publisher.Mono

interface AssetApi {
    fun getAsset(
        container: String,
        key: String,
        correlationId: String,
    ): Mono<String>

    fun createAsset(
        container: String,
        key: String,
        content: String,
        correlationId: String,
    ): Mono<Void>

    fun deleteAsset(
        container: String,
        key: String,
        correlationId: String,
    ): Mono<Void>
}
