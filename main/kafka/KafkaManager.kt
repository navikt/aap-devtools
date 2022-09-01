package kafka

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.vanilla.KafkaConfig
import no.nav.aap.kafka.vanilla.KafkaFactory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.OffsetAndTimestamp
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

internal class KafkaManager(
    private val config: KafkaConfig,
    private val kafka: KafkaFactory,
) {
    private val secureLog: Logger = LoggerFactory.getLogger("secureLog")

    internal fun produce(topic: Topic<ByteArray>, key: String, value: ByteArray?) = producer(topic).use { producer ->
        producer.send(ProducerRecord(topic.name, key, value)) { meta, error ->
            if (error != null) secureLog.error("Failed to produce record", error)
            else secureLog.trace(
                if (value == null) "Tombstoner Topic" else "Produserer til Topic",
                kv("key", key),
                kv("topic", topic.name),
                kv("partition", meta.partition()),
                kv("offset", meta.offset()),
            )
        }
    }

    internal fun read(request: SpecificRequest): KafkaResult? = consumer(request.topic).use { consumer ->
        val partition = TopicPartition(request.topic.name, request.partition)
        consumer.assign(listOf(partition))
        consumer.seek(partition, request.offset)
        return consumer.poll(Duration.ofMillis(2000)).firstOrNull()?.toResult()
    }

    internal fun read(request: KafkaRequest, limit: Int): List<KafkaResult> = consumer(request.topic).use { consumer ->
        val partitions = request.partitions.map { TopicPartition(request.topic.name, it) }
        consumer.assign(partitions)

        fun resetToLatest() {
            val partitionsForEpochMs = partitions.associateWith { request.fromEpochMillis }

            val partitionsByOffsetAndTimestamp: MutableMap<TopicPartition, OffsetAndTimestamp?> =
                consumer.offsetsForTimes(partitionsForEpochMs)

            partitionsByOffsetAndTimestamp.onEach { (p, oat) -> if (oat != null) consumer.seek(p, oat.offset()) }
        }

        fun resetToEarliest() = consumer.seekToBeginning(partitions)

        when (request.direction) {
            ResetPolicy.LATEST -> resetToLatest()
            ResetPolicy.EARLIEST -> resetToEarliest()
        }

        val results = mutableListOf<KafkaResult>()
        while (results.size < limit) {
            val records = consumer.poll(Duration.ofMillis(2000))
            if (records.isEmpty) break
            partitions.forEach {
                val resultsForPartition = records.records(it).map { record -> record.toResult() }
                results.addAll(resultsForPartition)
            }
        }

        return results
    }

    private fun <V : Any> consumer(topic: Topic<V>) = kafka.createConsumer(config, topic)
    private fun <V : Any> producer(topic: Topic<V>) = kafka.createProducer(config, topic)

    private fun ConsumerRecord<String, ByteArray?>.toResult() = KafkaResult(
        topic = topic(),
        key = key(),
        value = value(),
        partition = partition(),
        offset = offset(),
        timestamp = timestamp(),
    )
}
