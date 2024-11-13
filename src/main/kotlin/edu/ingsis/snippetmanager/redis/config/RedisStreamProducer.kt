package edu.ingsis.snippetmanager.redis.config

import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate

abstract class RedisStreamProducer(
    val streamKey: String,
    val redis: RedisTemplate<String, String>,
) {
    // We use Any as the upper bound of Value to make it non-nullable
    inline fun <reified Value : Any> emit(value: Value): RecordId? {
        val record =
            StreamRecords.newRecord()
                .ofObject(value)
                .withStreamKey(streamKey)

        return redis
            .opsForStream<String, Value>()
            .add(record)
    }
}
