package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kafka.SøknadKafkaDto
import kafka.Topics
import kafka.produce
import org.apache.kafka.clients.producer.Producer
import java.time.LocalDate

internal fun Route.sendSøknad(søknadProducer: Producer<String, SøknadKafkaDto>) {
    get("/søknad/{personident}") {
        val personident = call.parameters.getOrFail("personident")
        val søknad = SøknadKafkaDto()

        søknadProducer.produce(Topics.søknad, personident, søknad)

        call.respondText("Søknad $søknad mottatt!")
    }

    get("/søknad/{personident}/{alder}") {
        val personident = call.parameters.getOrFail("personident")
        val alder = call.parameters.getOrFail<Long>("alder")
        val søknad = SøknadKafkaDto(fødselsdato = LocalDate.now().minusYears(alder))

        søknadProducer.produce(Topics.søknad, personident, søknad)

        call.respondText("Søknad $søknad mottatt!")
    }
}
