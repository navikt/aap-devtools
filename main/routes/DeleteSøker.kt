package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kafka.SøknadKafkaDto
import kafka.Topics
import kafka.tombstone
import org.apache.kafka.clients.producer.Producer

internal fun Route.deleteSøker(
    søkerProducer: Producer<String, ByteArray>,
    søknadProducer: Producer<String, SøknadKafkaDto>,
) {
    get("/delete/{personident}") {
        val personident = call.parameters.getOrFail("personident")

        søknadProducer.tombstone(Topics.søknad, personident)
        søkerProducer.tombstone(Topics.søker, personident)

        call.respondText("Søknad og søker med ident $personident slettes!")
    }

    get("/delete/{personident}/{partition}") {
        val personident = call.parameters.getOrFail("personident")
        val partition = call.parameters.getOrFail<Int>("partition")

        søknadProducer.tombstone(Topics.søknad, partition, personident)
        søkerProducer.tombstone(Topics.søker, partition, personident)

        call.respondText("Søknad og søker med ident $personident slettes på partisjon $partition")
    }
}