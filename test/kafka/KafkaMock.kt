package kafka

import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.vanilla.KafkaConfig
import no.nav.aap.kafka.vanilla.KafkaFactory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import java.time.Duration
import java.util.concurrent.Future

internal class KafkaMock : KafkaFactory {
    private val recordsPerTopic = mutableMapOf<Topic<*>, MutableList<ConsumerRecord<String, *>>>()
    private val offsets = mutableMapOf<Int, Long>()

    override fun <V : Any> createConsumer(config: KafkaConfig, topic: Topic<V>) = ExtendedMockConsumer(topic)
    override fun <V : Any> createProducer(config: KafkaConfig, topic: Topic<V>) = ExtendedMockProducer(topic)

    inner class ExtendedMockProducer<V>(
        private val topic: Topic<V>,
    ) : MockProducer<String, V>(true, topic.keySerde.serializer(), topic.valueSerde.serializer()) {
        override fun send(record: ProducerRecord<String, V?>, callback: Callback): Future<RecordMetadata> {
            val producedRecordsForTopic = recordsPerTopic.getOrDefault(topic, mutableListOf())
            val partition = record.partition() ?: 11 // default to 11 when no partition specified
            val offset = offsets.getOrDefault(partition, -1) + 1 // start on 0 or increase by 1
            offsets[partition] = offset // save offset for partition

            // convert to consumer record for ExtendedMockConsumer
            val consumerRecord = ConsumerRecord(record.topic(), partition, offset, record.key(), record.value())
            producedRecordsForTopic.add(consumerRecord)
            recordsPerTopic[topic] = producedRecordsForTopic

            return super.send(record, callback)
        }
    }

    inner class ExtendedMockConsumer<V>(
        private val topic: Topic<V>,
    ) : MockConsumer<String, V>(OffsetResetStrategy.EARLIEST) {
        private val offsetByPartition = mutableMapOf<Int, Long>()

        override fun seek(partition: TopicPartition, offset: Long) {
            offsetByPartition += partition.partition() to offset
            super.seek(partition, offset)
        }


        override fun poll(timeout: Duration): ConsumerRecords<String, V> {
            // Find records after a given offset, or defaulted offset (earliest)
            val recordsAfterOffset = recordsPerTopic[topic]?.filter { record ->
                val seekedOffset = offsetByPartition.getOrDefault(record.partition(), 0)
                (record.offset() >= seekedOffset).also {
                    offsetByPartition += record.partition() to record.offset()
                }
            } ?: emptyList()

            // Group records by partition
            @Suppress("UNCHECKED_CAST")
            val recordsByPartition = recordsAfterOffset.groupBy { record ->
                TopicPartition(topic.name, record.partition())
            } as Map<TopicPartition, List<ConsumerRecord<String, V?>>>

            // Remove polled records
            val polledKeysToRemoveFromMockedRecords = recordsAfterOffset.map { it.key() }
            recordsPerTopic[topic]?.removeIf { record -> record.key() in polledKeysToRemoveFromMockedRecords }

            return ConsumerRecords<String, V>(recordsByPartition)
        }

        override fun seekToBeginning(partitions: MutableCollection<TopicPartition>) {
            partitions.onEach { offsetByPartition += it.partition() to 0 }
            super.seekToBeginning(partitions)
        }

        override fun seekToEnd(partitions: MutableCollection<TopicPartition>) {
            partitions.onEach {
                val endOffset = offsetByPartition.getOrDefault(it.partition(), 0)
                offsetByPartition += it.partition() to endOffset
            }
            super.seekToEnd(partitions)
        }
    }
}

