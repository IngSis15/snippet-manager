package edu.ingsis.snippetmanager.external

import edu.ingsis.snippetmanager.external.asset.AssetApi
import reactor.core.publisher.Mono

class AssetMockApi : AssetApi {
    val assets = mutableMapOf<String, String>()

    override fun getAsset(
        container: String,
        key: String,
    ): Mono<String> {
        return Mono.just(assets[key] ?: "")
    }

    override fun createAsset(
        container: String,
        key: String,
        content: String,
    ): Mono<Void> {
        assets[key] = content
        return Mono.empty()
    }

    override fun deleteAsset(
        container: String,
        key: String,
    ): Mono<Void> {
        assets.remove(key)
        return Mono.empty()
    }
}
