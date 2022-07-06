package kafka

data class KafkaResult(
    val topic: String,
    val key: String,
    val value: String,
    val partition: Int,
    val offset: Long,
    val timestamp: Long,
)

data class AllPartitionRequest(
    val topic: String,
    val direction: ResetPolicy,
    val partitions: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
)

data class SpecificRequest(
    val topic: String,
    val direction: ResetPolicy,
    val partition: Int,
    val offset: Long = 0,
)

enum class ResetPolicy { EARLIEST, LATEST }
