package edu.ingsis.snippetmanager.external.asset

import reactor.core.publisher.Mono

interface AssetApi {
    fun getAsset(
        container: String,
        key: String,
    ): Mono<String>

    fun createAsset(
        container: String,
        key: String,
        content: String,
    ): Mono<Void>

    fun deleteAsset(
        container: String,
        key: String,
    ): Mono<Void>
}
