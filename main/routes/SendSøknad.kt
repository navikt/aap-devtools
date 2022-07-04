package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kafka.SøknadKafkaDto
import kafka.Topics
import kafka.produce
import org.apache.kafka.clients.producer.Producer

internal fun Route.sendSøknad(søknadProducer: Producer<String, SøknadKafkaDto>) {
    get("/søknad/{personident}") {
        val personident = call.parameters.getOrFail("personident")
        val søknad = SøknadKafkaDto()

        søknadProducer.produce(Topics.søknad, personident, søknad)

        call.respondText("Søknad $søknad mottatt!")
    }

    get("/søknad/{personident}/{partition}") {
        val personident = call.parameters.getOrFail("personident")
        val partition = call.parameters.getOrFail<Int>("partition")
        val søknad = SøknadKafkaDto()

        søknadProducer.produce(Topics.søknad, partition, personident, søknad)

        call.respondText("Søknad $søknad mottatt!")
    }
}
