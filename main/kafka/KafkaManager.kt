package kafka

import Topics
import net.logstash.logback.argument.StructuredArguments.*
import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.vanilla.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

internal class KafkaManager(private val config: KafkaConfig) {
    private val secureLog: Logger = LoggerFactory.getLogger("secureLog")
    private val topicNameToTopic = listOf(Topics.søker, Topics.søknad).associateBy { it.name }

    internal fun produce(topic: Topic<String>, key: String, value: String?) =
        producer(topic.name).use { producer ->
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

    internal fun lookup(request: SpecificRequest): KafkaResult? = consumer(request.topic).use { consumer ->
        val partition = TopicPartition(request.topic, request.partition)
        consumer.assign(listOf(partition))
        consumer.seek(partition, request.offset)
        return consumer.poll(Duration.ofSeconds(1)).firstOrNull()?.toResult()
    }

    internal fun read(request: AllPartitionRequest, limit: Int = 60): List<KafkaResult> =
        consumer(request.topic).use { consumer ->
            val partitions = request.partitions.map { TopicPartition(request.topic, it) }
            consumer.assign(partitions)

            when (request.direction) {
                ResetPolicy.LATEST -> consumer.seekToEnd(partitions)
                ResetPolicy.EARLIEST -> consumer.seekToBeginning(partitions)
            }

            val results = mutableListOf<KafkaResult>()
            while (results.size < limit) {
                val records = consumer.poll(Duration.ofSeconds(1))
                if (records.isEmpty) break
                partitions.forEach { results.addAll(records.records(it).map { record -> record.toResult() }) }
            }

            return results
        }

    internal fun topicNames(): Set<String> = topicNameToTopic.keys

    private fun consumer(topic: String) = KafkaFactory.createConsumer(config, topicNameToTopic[topic]!!)
    private fun producer(topic: String) = KafkaFactory.createProducer(config, topicNameToTopic[topic]!!)

    private fun ConsumerRecord<String, String>.toResult(): KafkaResult = KafkaResult(
        topic = topic(),
        key = key(),
        value = value(),
        partition = partition(),
        offset = offset(),
        timestamp = timestamp(),
    )
}
