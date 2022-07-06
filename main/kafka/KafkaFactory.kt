package kafka

import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.vanilla.KafkaConfig
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer

internal object KafkaFactory {
    private const val prefix = "devtools"

    fun <V : Any> createConsumer(config: KafkaConfig, topic: Topic<V>): Consumer<String, V> = KafkaConsumer(
        config.consumerProperties("$prefix-${topic.name}-C", "$prefix-${topic.name}"),
        topic.keySerde.deserializer(),
        topic.valueSerde.deserializer(),
    )

    fun <V : Any> createProducer(config: KafkaConfig, topic: Topic<V>): Producer<String, V> = KafkaProducer(
        config.producerProperties("$prefix-${topic.name}-P"),
        topic.keySerde.serializer(),
        topic.valueSerde.serializer()
    )
}
