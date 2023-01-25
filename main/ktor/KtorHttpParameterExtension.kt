package ktor

import io.ktor.http.*
import io.ktor.server.util.*
import kafka.ResetPolicy
import kafka.Topics
import no.nav.aap.kafka.streams.Topic

internal val Parameters.direction: ResetPolicy get() = getOrFail("direction").uppercase().let(::enumValueOf)
internal val Parameters.partition: Int get() = getOrFail("partition").toInt()
internal val Parameters.offset: Long get() = getOrFail("offset").toLong()
internal val Parameters.personident: String get() = getOrFail("personident")
internal val Parameters.topic: Topic<ByteArray> get() = Topics[getOrFail("topic")]
