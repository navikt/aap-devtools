package kafka

import net.logstash.logback.argument.StructuredArguments
import no.nav.aap.kafka.streams.Topic
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val secureLog: Logger = LoggerFactory.getLogger("secureLog")

internal fun <V> Producer<String, V>.produce(topic: Topic<V>, key: String, value: V) {
    val partition = key.toInt().mod(12)
    produce(topic, partition, key, value)
}

internal fun <V> Producer<String, V>.tombstone(topic: Topic<V>, key: String) {
    val partition = key.toInt().mod(12)
    tombstone(topic, partition, key)
}

internal fun <V> Producer<String, V>.produce(topic: Topic<V>, partition: Int, key: String, value: V) {
    val record: ProducerRecord<String, V> = ProducerRecord(topic.name, partition, key, value)
    send(record) { meta, error ->
        if (error != null) {
            secureLog.error("Produserer til Topic feilet", error)
        } else {
            secureLog.trace(
                "Produserer til Topic",
                StructuredArguments.kv("key", key),
                StructuredArguments.kv("topic", topic.name),
                StructuredArguments.kv("partition", meta.partition()),
                StructuredArguments.kv("offset", meta.offset()),
            )
        }
    }
}

internal fun <V> Producer<String, V>.tombstone(topic: Topic<V>, partition: Int, key: String) {
    val record: ProducerRecord<String, V> = ProducerRecord(topic.name, partition, key, null)
    send(record) { meta, error ->
        if (error != null) {
            secureLog.error("Tombstoner Topic feilet", error)
        } else {
            secureLog.trace(
                "Tombstoner Topic",
                StructuredArguments.kv("key", key),
                StructuredArguments.kv("topic", topic.name),
                StructuredArguments.kv("partition", meta.partition()),
                StructuredArguments.kv("offset", meta.offset()),
            )
        }
    }
}