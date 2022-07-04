package kafka

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.aap.kafka.streams.Topic
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val secureLog: Logger = LoggerFactory.getLogger("secureLog")

internal fun <V> Producer<String, V>.tombstone(topic: Topic<V>, key: String) {
    produce(topic, null, key, null)
}

internal fun <V> Producer<String, V>.tombstone(topic: Topic<V>, partition: Int, key: String) {
    produce(topic, partition, key, null)
}

internal fun <V> Producer<String, V>.produce(topic: Topic<V>, key: String, value: V) {
    produce(topic, null, key, value)
}

internal fun <V> Producer<String, V>.produce(topic: Topic<V>, partition: Int?, key: String, value: V?) {
    val record = partition?.let { ProducerRecord(topic.name, partition, key, value) }
        ?: ProducerRecord(topic.name, key, value)
    val msg = if (value == null) "Tombstoner Topic" else "Produserer til Topic"

    send(record) { meta, error ->
        if (error != null) {
            secureLog.error("$msg feilet", error)
        } else {
            secureLog.trace(
                msg,
                kv("key", key),
                kv("topic", topic.name),
                kv("partition", meta.partition()),
                kv("offset", meta.offset()),
            )
        }
    }
}
