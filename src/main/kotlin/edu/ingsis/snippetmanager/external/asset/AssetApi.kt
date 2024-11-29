package edu.ingsis.snippetmanager.external.asset

interface AssetApi {
    fun getAsset(
        container: String,
        key: String,
    ): String?

    fun createAsset(
        container: String,
        key: String,
        content: String,
    )

    fun deleteAsset(
        container: String,
        key: String,
    )
}
