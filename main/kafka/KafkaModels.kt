package kafka

import no.nav.aap.kafka.streams.Topic
import java.time.LocalDateTime
import java.time.ZoneId

data class KafkaResult(
    val topic: String,
    val key: String,
    val value: ByteArray?,
    val partition: Int,
    val offset: Long,
    val timestamp: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KafkaResult

        if (topic != other.topic) return false
        if (key != other.key) return false
        if (value != null) {
            if (other.value == null) return false
            if (!value.contentEquals(other.value)) return false
        } else if (other.value != null) return false
        if (partition != other.partition) return false
        if (offset != other.offset) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topic.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + (value?.contentHashCode() ?: 0)
        result = 31 * result + partition
        result = 31 * result + offset.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

data class KafkaRequest(
    val topic: Topic<ByteArray>,
    val direction: ResetPolicy,
    val partitions: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
    val fromEpochMillis: Long = LocalDateTime.now().minusMinutes(15).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)

data class SpecificRequest(
    val topic: Topic<ByteArray>,
    val partition: Int,
    val offset: Long = 0,
)

enum class ResetPolicy { EARLIEST, LATEST }
